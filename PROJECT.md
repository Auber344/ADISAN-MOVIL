# 📦 Proyecto ADISAN

**ADISAN** es una aplicación móvil nativa para Android orientada a la logística y distribución comercial. Permite gestionar de manera integral ventas, pedidos, compras, inventario, ofertas, el catálogo de productos y la base de clientes.

---

## 🛠️ Stack Tecnológico

*   **Plataforma:** Android Nativo
*   **Lenguaje:** Java Puro (Sin Kotlin)
*   **Interfaz de Usuario:** Android XML Layouts + Material Design 3 (Sin Jetpack Compose)
*   **SDK:** Min SDK 24 / Target SDK 37
*   **Backend:** Firebase, sin servidor propio
    *   **Firebase Authentication** (correo y contraseña)
    *   **Cloud Firestore** (base de datos, región `southamerica-east1`)
*   **Proyecto Firebase:** `adisan-app-pe` (plan Spark gratuito)

---

## ⚙️ Reglas de Negocio y Arquitectura

*   **Autenticación:** el usuario entra con su correo y contraseña de Firebase Auth. Su perfil y rol viven en `usuarios/{uid}` de Firestore. Tras el login se guardan `token` (uid), `usuario`, `rol` e `id` en `SharedPreferences("AdisanPrefs")`, que es lo que leen las pantallas.
*   **Capa de datos:** `ApiClient.getApiService()` devuelve `FirebaseApiService`, que implementa la interfaz `ApiService` sobre Firestore. Las pantallas siguen usando `Call` / `Callback` / `Response`; `FirebaseCall` adapta las tareas de Firebase a esos tipos.
*   **Control de acceso:** como no hay servidor, los permisos por rol se aplican en las reglas de Firestore (`firebase/firestore.rules`).
*   **Stock consistente:** pedidos, compras, ajustes de inventario y cancelaciones se ejecutan en transacciones de Firestore, que validan el stock y registran cada movimiento en `movimientos_inventario`.
*   **Modelo Unificado de Ventas:** no hay "ventas" separadas de los "pedidos". Toda transacción comercial es un documento de `pedidos` con sus detalles embebidos. Las ventas cerradas tienen estado `entregado` (o `completado`).
*   **Venta Directa de Mostrador:** venta rápida en el local. El `cliente_id` es nulo, el pedido nace con estado `entregado` y la interfaz lo presenta como "Venta Mostrador".
*   **Cancelación:** al cancelar un pedido las unidades vuelven al inventario (movimiento `REVERSIÓN`). Los pedidos no se borran.
*   **Interfaz Dinámica Basada en Roles:** la interfaz adapta etiquetas y permisos según el rol (`cliente`, `vendedor`, `chofer`, `admin`).
*   **Carrito de Compras en Memoria:** Singleton thread-safe `CarritoManager.java`.

---

## 🗄️ Colecciones de Firestore

| Colección | Contenido |
| :--- | :--- |
| `usuarios/{uid}` | Perfil, rol y datos de contacto. Clientes sin cuenta de acceso usan un id automático. |
| `productos/{id}` | Catálogo: precios por paquete y unidad, unidades por paquete y `stock_unidades`. |
| `categorias/{id}` · `proveedores/{id}` | Datos maestros del catálogo. |
| `pedidos/{id}` | Cabecera del pedido o venta con `detalles` embebidos y estado (`pendiente`, `aceptado`, `en_camino`, `entregado`, `cancelado`). |
| `compras/{id}` | Ingresos de mercadería con `detalles` embebidos. |
| `ofertas/{id}` | Combos de productos con precio final y descuento. |
| `movimientos_inventario` | Historial de cada cambio de stock (venta, compra, ajuste, reversión). |
| `contadores/{coleccion}` | Último id numérico usado; la app sigue trabajando con ids enteros. |

Los campos usan los mismos nombres `snake_case` que tenía la antigua API REST, así los modelos con `@SerializedName` se leen sin cambios.

---

## 🔥 Operación de Firebase

| Tarea | Comando |
| :--- | :--- |
| Publicar reglas e índices | `firebase deploy --only firestore` |
| Cargar datos de ejemplo | `node firebase/seed/seed.js` (agrega `--force` para reescribirlos) |
| Compilar la app | `./gradlew app:assembleDebug` (requiere JDK 17+) |
| Probar sin tocar datos reales | `firebase emulators:start --only auth,firestore`, luego `node firebase/seed/seed.js --emulador` y compilar con `./gradlew app:assembleDebug -PfirebaseEmulador=true` |

El script de datos crea usuarios de prueba (admin, vendedor, chofer, cliente) y guarda sus contraseñas en `firebase/seed/credenciales-prueba.txt`, que no se sube al repositorio.

---

## 📱 Navegación y Vistas Principales

La aplicación se estructura sobre un **Bottom Navigation Bar** (`bottom_nav_menu.xml`) con 4 pilares:

1.  **Dashboard** (`DashboardActivity`): Panel de métricas en tiempo real.
2.  **Productos** (`ProductosActivity`): Exploración del catálogo y gestión de inventario.
3.  **Pedidos** (`PedidosActivity`): Historial logístico y ventas.
4.  **Más**: Menú de configuración extendida y futuras integraciones.

---
*Documento generado a partir de las directrices principales (AGENTS.md) del repositorio.*
