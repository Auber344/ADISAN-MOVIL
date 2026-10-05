# 📦 Proyecto ADISAN

**ADISAN** es una aplicación móvil nativa para Android orientada a la logística y distribución comercial. Permite gestionar de manera integral ventas, pedidos, el catálogo de productos y la base de clientes.

---

## 🛠️ Stack Tecnológico

*   **Plataforma:** Android Nativo
*   **Lenguaje:** Java Puro (Sin Kotlin)
*   **Interfaz de Usuario:** Android XML Layouts + Material Design 3 (Sin Jetpack Compose)
*   **SDK:** Min SDK 24 / Target SDK 34+
*   **Consumo de API:** Retrofit 2 + Gson Converter + OkHttp 3
*   **Backend:** Node.js + Express + MySQL 8.0 (Despliegue local en `http://10.0.2.2:3000/`)

---

## ⚙️ Reglas de Negocio y Arquitectura

*   **Autenticación y Seguridad:** Basada en JWT (JSON Web Token). El token se almacena localmente en `SharedPreferences("AdisanPrefs")` y se inyecta de forma automática en cada petición HTTP mediante un `Interceptor` de OkHttp configurado en `ApiClient.java`.
*   **Modelo Unificado de Ventas:** La base de datos no separa las "ventas" de los "pedidos". Toda transacción comercial se procesa mediante la tabla `pedidos` y `detalle_pedido`. Las ventas cerradas se identifican por tener el estado `entregado` (o `completado`).
*   **Venta Directa de Mostrador:** Permite realizar ventas rápidas en el local. En este caso, el `cliente_id` es nulo, el pedido nace con estado `entregado` de inmediato, y la interfaz lo presenta bajo el nombre de "Venta Mostrador".
*   **Interfaz Dinámica Basada en Roles:** La interfaz adapta sus etiquetas y permisos de acuerdo al rol del usuario en sesión (`CLIENTE`, `VENDEDOR`, `ADMIN`).
*   **Carrito de Compras en Memoria:** Se gestiona mediante el patrón de diseño *Singleton* a través de la clase `CarritoManager.java`, lo cual garantiza un estado seguro para los hilos (thread-safe) y persistencia en memoria durante el flujo entre vistas.

---

## 🗄️ Estructura de Base de Datos Principal

*   **`usuarios`**: Gestión de credenciales, roles (`admin`, `vendedor`, `chofer`, `cliente`) y datos de contacto.
*   **`productos`**: Definición base del catálogo de productos y su relación con proveedores.
*   **`presentaciones`**: Establece los precios de compra y venta, así como los niveles de factor y el **stock actual** de los productos.
*   **`pedidos`**: Cabecera de operaciones financieras y logísticas, almacenando totales y estados (`pendiente`, `aceptado`, `en_camino`, `entregado`, `cancelado`).
*   **`detalle_pedido`**: Líneas de artículos específicos por cada pedido.

---

## 🔌 Endpoints y API REST

| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| **POST** | `/api/login` | Autentica al usuario y devuelve el JWT y rol. |
| **GET** | `/api/dashboard` | Retorna métricas clave (ventas, pedidos, stock bajo). |
| **GET** | `/api/productos` | Recupera el catálogo y los niveles de stock. |
| **POST** / **PUT** / **DELETE** | `/api/productos` | CRUD completo para manejo de inventario (Solo Admin). |
| **GET** | `/api/pedidos` | Historial de transacciones, pedidos y ventas de mostrador. |
| **POST** | `/api/pedidos` | Registra una orden/venta y descuenta stock. |
| **PUT** | `/api/pedidos/:id` | Actualización de seguimiento (ej. de `pendiente` a `entregado`). |
| **GET** / **POST** | `/api/usuarios` | Búsqueda, filtrado y creación de clientes. |

---

## 📱 Navegación y Vistas Principales

La aplicación se estructura sobre un **Bottom Navigation Bar** (`bottom_nav_menu.xml`) limpio y funcional con 4 pilares:

1.  **Dashboard** (`DashboardActivity`): Panel de métricas en tiempo real.
2.  **Productos** (`ProductosActivity`): Exploración del catálogo y gestión de inventario.
3.  **Pedidos** (`PedidosActivity`): Historial logístico y ventas.
4.  **Más**: Menú de configuración extendida y futuras integraciones.

---
*Documento generado a partir de las directrices principales (AGENTS.md) del repositorio.*