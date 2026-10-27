# 🤖 AGENTS.md — Developer & AI Agent Guidelines for ADISAN Android

This document serves as the authoritative technical reference and operational guide for AI agents and human developers working on the **ADISAN** native Android codebase.

---

## 📌 1. Project Overview & Technical Stack

| Domain | Technology / Specification |
| :--- | :--- |
| **App Name** | ADISAN (Logistics & Commercial Distribution App) |
| **Language** | **Pure Java** (STRICT RULE: NO Kotlin allowed) |
| **UI Stack** | **Android XML Layouts + Material Design 3** (STRICT RULE: NO Jetpack Compose) |
| **Min SDK / Target SDK** | Min SDK 24 / Target SDK 37 |
| **Backend** | Firebase only, no custom server: Firebase Authentication (email + password) and Cloud Firestore (`southamerica-east1`) |
| **Firebase Project** | `adisan-app-pe` (Spark plan). Config in `app/google-services.json`, `firebase.json`, `.firebaserc` |
| **Data Layer** | `ApiClient.getApiService()` returns `FirebaseApiService`, which implements the `ApiService` interface on top of Firestore |
| **Call Adapter** | `FirebaseCall<T>` exposes Firebase `Task`s as `retrofit2.Call<T>`, so screens keep using `enqueue` / `Callback` / `Response`. Retrofit is only a dependency for those types |
| **Session** | After login, `token` (Firebase uid), `usuario`, `rol`, `id` are saved in `SharedPreferences("AdisanPrefs")` |
| **Access Control** | Firestore Security Rules in `firebase/firestore.rules` (there is no server to enforce roles) |
| **Build System** | Gradle (`./gradlew app:assembleDebug`, requires JDK 17+) |

---

## 🏛️ 2. Core Architectural Principles & Business Rules

### 1. No Independent `ventas` Collection
- **Crucial Rule**: There is NO separate `ventas` collection.
- **Unified Order Model**: All commercial operations (orders and direct sales) are documents in `pedidos`, with their line items embedded in `detalles`.
- **Completed Sale Definition**: An order with `estado = 'entregado'` (or `'completado'`) constitutes a completed sale for financial metrics and Dashboard calculations.

### 2. Direct Sales (Venta Directa de Mostrador)
- Direct over-the-counter sales do **not** require a client.
- When creating a direct sale:
  - `pedidos.cliente_id` = `null`
  - `pedidos.estado` = `'entregado'`
  - `pedidos.cliente` = `"Venta Mostrador"`

### 1b. Stock Changes Always Go Through Transactions
- Orders, purchases, inventory adjustments and cancellations run in Firestore transactions inside `FirebaseApiService`: they validate stock, update `productos.stock_unidades` and write a document in `movimientos_inventario`.
- Never write `stock_unidades` directly from a screen; call `ajustarInventario` instead.
- Cancelling an order (`estado = 'cancelado'`) returns its units to stock (`REVERSIÓN` movement). Orders are never deleted.

### 3. Role-Based Dynamic UI Text
Read `rol` from `SharedPreferences` (`"AdisanPrefs"` -> `"rol"`):
- **`CLIENTE`**:
  - Catalog Header Title: `"Comprar productos"`
  - Orders Header Title: `"Mis pedidos"`
- **`VENDEDOR` / `ADMIN`**:
  - Catalog Header Title: `"Vender productos"`
  - Orders Header Title: `"Pedidos"`

### 4. In-Memory Shopping Cart
- `CarritoManager.java` is a thread-safe Singleton managing `ItemCarrito` objects in memory.
- The cart persists across activity transitions (`ProductosActivity` ➔ `CarritoActivity` ➔ `PedidosActivity`).

---

## 🗄️ 3. Firestore Collections

Field names keep the snake_case names of the former REST API so the `@SerializedName` models deserialize unchanged. Money is stored as numbers and converted to 2-decimal strings when mapped to models.

| Collection | Key fields |
| :--- | :--- |
| `usuarios/{uid}` | `id` (int), `uid`, `nombres`, `apellidos`, `dni`, `ruc`, `telefono`, `correo`, `usuario`, `direccion`, `rol` (`admin`/`vendedor`/`chofer`/`cliente`), `activo`. Doc id = Firebase Auth uid; clients without app access use an auto id and `uid = null`. No passwords are stored. |
| `productos/{id}` | `id`, `nombre`, `categoria_id`, `proveedor_id`, `unidades_por_paquete`, `precio_compra_paquete`, `precio_venta_paquete`, `precio_venta_unidad`, `stock_unidades`, `activo` (deleting sets `activo = 0`) |
| `categorias/{id}` | `id`, `nombre`, `factor` |
| `proveedores/{id}` | `id`, `nombre_empresa`, `ruc`, `telefono`, `correo`, `direccion` |
| `pedidos/{id}` | `id`, `cliente_id`, `cliente_uid`, `cliente`, `vendedor_uid`, `total`, `metodo_pago`, `estado`, `fecha`, `fecha_estimada_entrega`, `detalles[]`, `unidades_por_producto{}` (used to restock on cancel) |
| `compras/{id}` | `id`, `proveedor_id`, `proveedor_nombre`, `usuario_id`, `total`, `fecha`, `detalles[]` |
| `ofertas/{id}` | `id`, `nombre`, `precio_normal`, `precio_final`, `descuento`, `activo`, `items[]` |
| `movimientos_inventario/{auto}` | `producto_id`, `tipo` (`VENTA`, `COMPRA`, `REVERSIÓN`, `entrada`, `salida`), `cantidad_unidades`, `stock_anterior`, `stock_nuevo`, `motivo`, `usuario_uid`, `pedido_id`, `compra_id`, `fecha` |
| `contadores/{coleccion}` | `ultimo`: last numeric id issued (ids stay `int` in the app) |

---

## 🔌 4. ApiService Operations

`ApiService` is still the contract every screen uses; `FirebaseApiService` implements each method on Firestore.

| Area | Methods | Notes |
| :--- | :--- | :--- |
| Auth | `login` | `LoginRequest.usuario` is the email. Reads the role from `usuarios/{uid}` |
| Dashboard | `getDashboardData` | Computed client-side from products, orders and a user count |
| Users | `getUsuarios`, `buscarUsuarios`, `crearUsuario`, `editarUsuario`, `eliminarUsuario`, `resetPasswordUsuario` | Creating with email + password also creates the Auth account (secondary `FirebaseApp`, so the admin stays signed in). Reset sends Firebase's password-reset email |
| Catalog | `getProductos*`, `crearProducto`, `editarProducto`, `eliminarProducto`, `getCategorias*`, `crearCategoria`, `getProveedores*`, `crearProveedor` | `editarProducto` never changes stock |
| Orders | `getPedidos*`, `getPedidoById*`, `crearPedidoNuevo`, `editarPedido` | Prices come from the catalog; offers expand into their products |
| Inventory | `getInventario*`, `getMovimientosInventario*`, `ajustarInventario` | `tipo` = `entrada` / `salida` in units |
| Purchases | `getCompras*`, `crearCompra` | Adds stock and updates the purchase price |
| Offers | `getOfertas*`, `getOfertasCatalogo*`, `crearOferta`, `editarOferta`, `eliminarOferta` | Catalog returns active offers only |

Errors reach screens as HTTP-style responses: business errors (e.g. insufficient stock) are `400` with `{"success":false,"message":...}` in `errorBody()`, denied rules are `403`, an expired session is `401`, and no connectivity goes to `onFailure`. Use `RespuestaApi.mensajeError(response, fallback)` to show the message.

---

## 📁 5. Directory Structure & Key Classes

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/adisan_app_android/
│   ├── AdisanApp.java               # Application: system-bar insets for edge-to-edge screens
│   ├── ApiClient.java               # Returns FirebaseApiService + shared Gson
│   ├── ApiService.java              # Data contract used by every screen
│   ├── FirebaseApiService.java      # ApiService on Firebase Auth + Firestore (business logic)
│   ├── FirebaseCall.java            # Adapts Firebase Tasks to retrofit2.Call
│   ├── RespuestaApi.java            # Reads error messages from responses
│   ├── LoginActivity.java           # Firebase Auth login, password reset, remember session
│   ├── LoginRequest.java / Response # Auth models
│   ├── DashboardActivity.java       # Real-time metrics dashboard
│   ├── DashboardResponse.java       # Dashboard JSON deserialization model
│   ├── ProductosActivity.java       # Product catalog & CRUD management
│   ├── Producto.java / Response     # Product domain models
│   ├── ProductoAdapter.java         # RecyclerView adapter for catalog items
│   ├── ItemCarrito.java             # Shopping cart item model
│   ├── CarritoManager.java          # In-memory Singleton cart manager
│   ├── CarritoActivity.java         # Checkout & Cart management screen
│   ├── PedidosActivity.java         # Order tracking & client order creation
│   ├── Pedido.java / Response       # Order domain models
│   ├── DetallePedido.java           # Order detail line item model
│   ├── PedidoAdapter.java           # RecyclerView adapter for order history
│   ├── Usuario.java / Response      # User / Client domain models
│   └── MainActivity.java            # App entry point
└── res/
    ├── drawable/                    # Icons & Spinner outlined backgrounds
    ├── layout/                      # Activity and Dialog XML layouts
    └── menu/bottom_nav_menu.xml     # Clean 4-item bottom navigation bar

firebase/
├── firestore.rules                  # Role-based access control (deploy: firebase deploy --only firestore)
├── firestore.indexes.json
└── seed/seed.js                     # Sample data + test users (node firebase/seed/seed.js)
```

---

## 🧭 6. Bottom Navigation Hierarchy

The main bottom navigation bar (`bottom_nav_menu.xml`) contains 4 items:

1. **Dashboard** (`DashboardActivity`)
2. **Productos** (`ProductosActivity`)
3. **Pedidos** (`PedidosActivity`)
4. **Más** (Settings & future features)

---

## 🛠️ 7. Development Guidelines for AI Agents

When implementing new features or fixing bugs in this repository:

1. **Maintain Pure Java + XML Stack**:
   - Do NOT introduce Kotlin files (`.kt`) or Jetpack Compose UI code.
   - Keep all layout definitions in standard Android XML under `res/layout/`.

2. **Strict Concurrency Control**:
   - Always read files using `read_file` before editing.
   - Use surgical `replace_file_content` or `multi_replace_file_content` for file edits.
   - Never use shell commands (`sed`, `echo >`, `cat << EOF`) to modify source code files.

3. **GSON Deserialization Safety**:
   - When mapping fields with `@SerializedName`, ensure that multiple Java fields DO NOT claim the same primary JSON property name to prevent `IllegalArgumentException` conflicts in Retrofit.
   - When handling fields that can be JSON Arrays or Numbers/Strings, use `List<Type>` or flexible getters (`Double.parseDouble(field.toString())`).

4. **Adding Data Operations**:
   - Add the method to `ApiService` and implement it in `FirebaseApiService`; do not reintroduce HTTP endpoints.
   - Any operation that changes stock must run in a Firestore transaction and write a `movimientos_inventario` document.
   - If a new collection or access pattern is added, update `firebase/firestore.rules` and deploy it.

5. **Build Verification**:
   - Always run `./gradlew app:assembleDebug` using `gradle_build` after completing code modifications.
   - Verify that the build finishes with **`BUILD SUCCESSFUL`** and **0 compilation errors**.

---

*Last Updated: October 2026*
