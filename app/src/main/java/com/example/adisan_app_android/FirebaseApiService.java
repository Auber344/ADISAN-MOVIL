package com.example.adisan_app_android;

import android.text.TextUtils;

import com.example.adisan_app_android.FirebaseCall.ErrorNegocio;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import retrofit2.Call;

/**
 * Implementación de ApiService sobre Firebase (Auth + Firestore), sin backend propio.
 *
 * Colecciones (los campos usan los mismos nombres snake_case que tenía la API REST,
 * así los modelos con @SerializedName se siguen leyendo igual):
 *   usuarios/{uid}            perfil y rol; el id del documento es el uid de Firebase Auth
 *                             (clientes sin cuenta de acceso usan un id automático)
 *   productos/{id}            catálogo, precios y stock_unidades
 *   categorias/{id}, proveedores/{id}
 *   pedidos/{id}              ventas y pedidos con sus detalles embebidos
 *   compras/{id}              ingresos de mercadería con detalles embebidos
 *   ofertas/{id}              combos con sus items embebidos
 *   movimientos_inventario/*  historial de cada cambio de stock
 *   contadores/{coleccion}    último id numérico usado (los ids siguen siendo int en la app)
 *
 * Las operaciones que tocan stock (pedidos, compras, ajustes, cancelaciones) se hacen en
 * transacciones de Firestore para que dos ventas simultáneas no dejen el stock negativo.
 */
public class FirebaseApiService implements ApiService {

    static final String USUARIOS = "usuarios";
    static final String PROVEEDORES = "proveedores";
    static final String CATEGORIAS = "categorias";
    static final String PRODUCTOS = "productos";
    static final String PEDIDOS = "pedidos";
    static final String COMPRAS = "compras";
    static final String OFERTAS = "ofertas";
    static final String MOVIMIENTOS = "movimientos_inventario";
    static final String CONTADORES = "contadores";

    /** Mismo umbral que usan InventarioActivity e InventarioAdapter para marcar stock bajo. */
    private static final int STOCK_BAJO = 10;

    private static final List<String> ESTADOS_PEDIDO =
            Arrays.asList("pendiente", "aceptado", "en_camino", "entregado", "cancelado");

    /** Campos de dinero: se envían a los modelos como texto con 2 decimales. */
    private static final Set<String> CAMPOS_DINERO = new HashSet<>(Arrays.asList(
            "total", "precio", "subtotal", "precio_compra_paquete", "precio_venta_paquete",
            "precio_venta_unidad", "precio_paquete_snapshot", "precio_unidad_snapshot",
            "precio_normal", "precio_final", "descuento", "totalVentas", "utilidad"));

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Gson gson = ApiClient.gson();

    // ==========================================
    // AUTENTICACIÓN
    // ==========================================

    /** El campo "usuario" del request es el correo con el que se creó la cuenta. */
    @Override
    public Call<LoginResponse> login(LoginRequest loginRequest) {
        return llamada(() -> {
            String correo = loginRequest.getUsuario() != null ? loginRequest.getUsuario().trim() : "";
            String password = loginRequest.getPassword() != null ? loginRequest.getPassword() : "";
            return auth.signInWithEmailAndPassword(correo, password)
                    .continueWithTask(t -> db.collection(USUARIOS).document(t.getResult().getUser().getUid()).get())
                    .continueWith(t -> {
                        DocumentSnapshot doc = t.getResult();
                        if (!doc.exists() || largo(doc.get("activo"), 1) != 1) {
                            auth.signOut();
                            throw new ErrorNegocio(403, "Tu cuenta no está habilitada. Contacta al administrador.");
                        }
                        JsonObject r = new JsonObject();
                        r.addProperty("success", true);
                        r.addProperty("token", doc.getId());
                        r.addProperty("id", largo(doc.get("id")));
                        r.addProperty("usuario", texto(doc.get("nombres")).isEmpty() ? texto(doc.get("usuario")) : texto(doc.get("nombres")));
                        r.addProperty("rol", texto(doc.get("rol")));
                        r.addProperty("message", "Bienvenido");
                        return gson.fromJson(r, LoginResponse.class);
                    });
        });
    }

    /**
     * Crea la cuenta de Auth (queda con la sesión iniciada) y su perfil con rol "cliente".
     * Si el perfil no se puede guardar se borra la cuenta, para que el correo pueda volver a usarse.
     */
    @Override
    public Call<LoginResponse> registrarCliente(Usuario usuario) {
        return llamada(() -> {
            JsonObject b = cuerpo(usuario);
            Map<String, Object> datos = datosUsuario(b);
            datos.put("rol", "cliente");
            String correo = texto(datos.get("correo"));
            String password = campoTexto(b, "password");
            if (texto(datos.get("nombres")).isEmpty()) throw new ErrorNegocio(400, "Ingresa tus nombres");
            if (texto(datos.get("dni")).isEmpty()) throw new ErrorNegocio(400, "Ingresa tu DNI");
            if (correo.isEmpty()) throw new ErrorNegocio(400, "Ingresa tu correo");
            if (password.length() < 6) throw new ErrorNegocio(400, "La contraseña debe tener al menos 6 caracteres");
            if (texto(datos.get("usuario")).isEmpty()) datos.put("usuario", correo);

            return auth.createUserWithEmailAndPassword(correo, password).continueWithTask(tCuenta -> {
                FirebaseUser nuevo = tCuenta.getResult().getUser();
                DocumentReference ref = db.collection(USUARIOS).document(nuevo.getUid());
                datos.put("uid", nuevo.getUid());
                datos.put("activo", 1);
                datos.put("created_at", Timestamp.now());
                return db.runTransaction(tx -> {
                    long id = siguienteId(tx, USUARIOS);
                    datos.put("id", id);
                    tx.set(ref, datos);
                    guardarContador(tx, USUARIOS, id);
                    return datos;
                }).continueWithTask(tPerfil -> {
                    if (tPerfil.isSuccessful()) return tPerfil;
                    Exception error = tPerfil.getException();
                    return nuevo.delete().continueWithTask(tBorrar -> Tasks.<Map<String, Object>>forException(error));
                });
            }).continueWith(t -> {
                Map<String, Object> perfil = t.getResult();
                JsonObject r = new JsonObject();
                r.addProperty("success", true);
                r.addProperty("token", texto(perfil.get("uid")));
                r.addProperty("id", largo(perfil.get("id")));
                r.addProperty("usuario", texto(perfil.get("nombres")));
                r.addProperty("rol", "cliente");
                r.addProperty("message", "Cuenta creada");
                return gson.fromJson(r, LoginResponse.class);
            });
        });
    }

    @Override
    public Call<DashboardResponse> getDashboardData(String token) {
        return llamada(() -> perfilActual().continueWithTask(tPerfil -> {
            Map<String, Object> perfil = tPerfil.getResult();
            Task<List<Map<String, Object>>> tProductos = listar(db.collection(PRODUCTOS).whereEqualTo("activo", 1));
            Task<List<Map<String, Object>>> tPedidos = listarPedidosPara(perfil);
            Task<Long> tUsuarios = esPersonal(perfil)
                    ? db.collection(USUARIOS).count().get(AggregateSource.SERVER).continueWith(t -> t.getResult().getCount())
                    : Tasks.forResult(0L);
            return Tasks.whenAllComplete(tProductos, tPedidos, tUsuarios).continueWith(t -> {
                List<Map<String, Object>> productos = tProductos.getResult();
                List<Map<String, Object>> pedidos = tPedidos.getResult();

                Map<String, Map<String, Object>> productoPorId = new HashMap<>();
                List<Map<String, Object>> stockBajo = new ArrayList<>();
                for (Map<String, Object> p : productos) {
                    productoPorId.put(String.valueOf(largo(p.get("id"))), p);
                    if (largo(p.get("stock_unidades")) <= STOCK_BAJO) {
                        Map<String, Object> item = new LinkedHashMap<>();
                        item.put("producto_id", p.get("id"));
                        item.put("nombre", p.get("nombre"));
                        item.put("stock_unidades", p.get("stock_unidades"));
                        stockBajo.add(item);
                    }
                }

                double ventas = 0;
                double costo = 0;
                Map<String, Integer> porEstado = new LinkedHashMap<>();
                for (Map<String, Object> pedido : pedidos) {
                    String estado = texto(pedido.get("estado"));
                    porEstado.put(estado, porEstado.containsKey(estado) ? porEstado.get(estado) + 1 : 1);
                    if (!"entregado".equals(estado) && !"completado".equals(estado)) continue;
                    ventas += decimal(pedido.get("total"));
                    Map<String, Object> unidades = mapa(pedido.get("unidades_por_producto"));
                    for (Map.Entry<String, Object> e : unidades.entrySet()) {
                        Map<String, Object> p = productoPorId.get(e.getKey());
                        if (p == null) continue;
                        double costoUnidad = decimal(p.get("precio_compra_paquete")) / Math.max(1, largo(p.get("unidades_por_paquete")));
                        costo += costoUnidad * largo(e.getValue());
                    }
                }

                List<Map<String, Object>> pedidosPorEstado = new ArrayList<>();
                for (Map.Entry<String, Integer> e : porEstado.entrySet()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("estado", e.getKey());
                    item.put("cantidad", e.getValue());
                    pedidosPorEstado.add(item);
                }

                Map<String, Object> r = new LinkedHashMap<>();
                r.put("success", true);
                r.put("totalProductos", productos.size());
                r.put("totalUsuarios", tUsuarios.isSuccessful() ? tUsuarios.getResult() : 0L);
                r.put("totalPedidos", pedidos.size());
                r.put("totalVentas", redondear(ventas));
                r.put("utilidad", redondear(ventas - costo));
                r.put("stockBajo", stockBajo);
                r.put("pedidosPorEstado", pedidosPorEstado);
                return modelo(r, DashboardResponse.class);
            });
        }));
    }

    // ==========================================
    // CLIENTES / USUARIOS
    // ==========================================

    @Override
    public Call<UsuarioResponse> getUsuarios() {
        return llamada(() -> listarUsuarios().continueWith(t -> respuesta(UsuarioResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<Usuario> getUsuarioById(int id) {
        return llamada(() -> buscarPorId(USUARIOS, id).continueWith(t -> modelo(t.getResult(), Usuario.class)));
    }

    @Override
    public Call<UsuarioResponse> buscarUsuarios(String search) {
        return llamada(() -> listarUsuarios().continueWith(t -> {
            String q = search != null ? search.trim().toLowerCase(Locale.ROOT) : "";
            List<Map<String, Object>> filtrados = new ArrayList<>();
            for (Map<String, Object> u : t.getResult()) {
                String texto = (texto(u.get("nombres")) + " " + texto(u.get("apellidos")) + " " + texto(u.get("dni")) + " "
                        + texto(u.get("usuario")) + " " + texto(u.get("correo"))).toLowerCase(Locale.ROOT);
                if (q.isEmpty() || texto.contains(q)) filtrados.add(u);
            }
            return respuesta(UsuarioResponse.class, filtrados, null);
        }));
    }

    @Override
    public Call<List<Usuario>> getUsuariosDirectList() {
        return llamada(() -> listarUsuarios().continueWith(t -> modelos(t.getResult(), Usuario.class)));
    }

    /**
     * Si el usuario trae correo y contraseña se le crea una cuenta de acceso en Firebase Auth;
     * si no (por ejemplo un cliente de mostrador) queda solo como registro para asociarle pedidos.
     */
    @Override
    public Call<UsuarioResponse> crearUsuario(Usuario usuario) {
        return llamada(() -> {
            JsonObject b = cuerpo(usuario);
            Map<String, Object> datos = datosUsuario(b);
            String correo = texto(datos.get("correo"));
            String password = campoTexto(b, "password");
            if (texto(datos.get("nombres")).isEmpty()) throw new ErrorNegocio(400, "Ingresa los nombres");
            if (texto(datos.get("dni")).isEmpty()) throw new ErrorNegocio(400, "Ingresa el DNI");
            boolean conAcceso = !correo.isEmpty() && !password.isEmpty();
            if (conAcceso && password.length() < 6) {
                throw new ErrorNegocio(400, "La contraseña debe tener al menos 6 caracteres");
            }

            return verificarUnico(USUARIOS, "dni", datos.get("dni"), null, "Ya existe un usuario con ese DNI")
                    .continueWithTask(t -> {
                        t.getResult();
                        String nombreUsuario = texto(datos.get("usuario"));
                        return nombreUsuario.isEmpty() ? Tasks.<Void>forResult(null)
                                : verificarUnico(USUARIOS, "usuario", nombreUsuario, null, "Ese nombre de usuario ya está en uso");
                    })
                    .continueWithTask(t -> {
                        t.getResult();
                        return conAcceso ? crearCuentaAcceso(correo, password) : Tasks.forResult((String) null);
                    })
                    .continueWithTask(t -> {
                        String uid = t.getResult();
                        DocumentReference ref = uid != null
                                ? db.collection(USUARIOS).document(uid)
                                : db.collection(USUARIOS).document();
                        datos.put("uid", uid);
                        datos.put("activo", 1);
                        datos.put("created_at", Timestamp.now());
                        return db.runTransaction(tx -> {
                            long id = siguienteId(tx, USUARIOS);
                            datos.put("id", id);
                            tx.set(ref, datos);
                            guardarContador(tx, USUARIOS, id);
                            return datos;
                        });
                    })
                    .continueWith(t -> respuesta(UsuarioResponse.class, Collections.singletonList(t.getResult()),
                            conAcceso ? "Usuario creado con acceso a la app" : "Cliente registrado"));
        });
    }

    @Override
    public Call<UsuarioResponse> editarUsuario(int id, Usuario usuario) {
        return llamada(() -> {
            Map<String, Object> cambios = datosUsuario(cuerpo(usuario));
            return buscarDocPorId(USUARIOS, id).continueWithTask(t -> {
                DocumentSnapshot doc = t.getResult();
                return verificarUnico(USUARIOS, "dni", cambios.get("dni"), doc.getId(), "Ya existe un usuario con ese DNI")
                        .continueWithTask(t2 -> {
                            t2.getResult();
                            return doc.getReference().update(cambios);
                        })
                        .continueWith(t2 -> {
                            t2.getResult();
                            Map<String, Object> actualizado = datos(doc);
                            actualizado.putAll(cambios);
                            return respuesta(UsuarioResponse.class, Collections.singletonList(actualizado), "Usuario actualizado");
                        });
            });
        });
    }

    /** Borra el perfil: sin perfil la cuenta de Auth ya no puede entrar a la app. */
    @Override
    public Call<UsuarioResponse> eliminarUsuario(int id) {
        return llamada(() -> buscarDocPorId(USUARIOS, id).continueWithTask(t -> {
            DocumentSnapshot doc = t.getResult();
            FirebaseUser actual = auth.getCurrentUser();
            if (actual != null && actual.getUid().equals(doc.getId())) {
                throw new ErrorNegocio(400, "No puedes eliminar tu propia cuenta");
            }
            return doc.getReference().delete();
        }).continueWith(t -> {
            t.getResult();
            return respuesta(UsuarioResponse.class, new ArrayList<>(), "Usuario eliminado");
        }));
    }

    /**
     * Sin un servidor no se puede fijar la contraseña de otra cuenta, así que se envía al
     * usuario el correo oficial de Firebase para que él mismo elija una nueva.
     */
    @Override
    public Call<UsuarioResponse> resetPasswordUsuario(int id, ResetPasswordRequest request) {
        return llamada(() -> buscarPorId(USUARIOS, id).continueWithTask(t -> {
            Map<String, Object> u = t.getResult();
            String correo = texto(u.get("correo"));
            if (u.get("uid") == null || correo.isEmpty()) {
                throw new ErrorNegocio(400, "Este usuario no tiene una cuenta de acceso con correo");
            }
            return auth.sendPasswordResetEmail(correo).continueWith(t2 -> {
                t2.getResult();
                return respuesta(UsuarioResponse.class, new ArrayList<>(),
                        "Se envió un correo a " + correo + " para que cree una nueva contraseña");
            });
        }));
    }

    // ==========================================
    // PROVEEDORES
    // ==========================================

    @Override
    public Call<ProveedorResponse> getProveedores() {
        return llamada(() -> listarProveedores().continueWith(t -> respuesta(ProveedorResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<List<Proveedor>> getProveedoresDirectList() {
        return llamada(() -> listarProveedores().continueWith(t -> modelos(t.getResult(), Proveedor.class)));
    }

    @Override
    public Call<ProveedorResponse> crearProveedor(Proveedor proveedor) {
        return llamada(() -> {
            JsonObject b = cuerpo(proveedor);
            String nombre = campoTexto(b, "nombreEmpresa");
            if (nombre.isEmpty()) nombre = campoTexto(b, "nombre_empresa");
            if (nombre.isEmpty()) throw new ErrorNegocio(400, "Ingresa el nombre de la empresa");
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("nombre_empresa", nombre);
            datos.put("ruc", campoTexto(b, "ruc"));
            datos.put("telefono", campoTexto(b, "telefono"));
            datos.put("correo", campoTexto(b, "correo"));
            datos.put("direccion", campoTexto(b, "direccion"));
            return crearConId(PROVEEDORES, datos).continueWith(t ->
                    respuesta(ProveedorResponse.class, Collections.singletonList(conNombreEmpresa(t.getResult())), "Proveedor registrado"));
        });
    }

    // ==========================================
    // CATEGORÍAS
    // ==========================================

    @Override
    public Call<List<Categoria>> getCategorias() {
        return llamada(() -> listarOrdenado(CATEGORIAS, "nombre").continueWith(t -> modelos(t.getResult(), Categoria.class)));
    }

    @Override
    public Call<JsonElement> getCategoriasElement() {
        return llamada(() -> listarOrdenado(CATEGORIAS, "nombre").continueWith(t -> envoltorio(t.getResult())));
    }

    @Override
    public Call<CategoriaResponse> getCategoriasResponse() {
        return llamada(() -> listarOrdenado(CATEGORIAS, "nombre").continueWith(t -> respuesta(CategoriaResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<CategoriaResponse> crearCategoria(Categoria categoria) {
        return llamada(() -> {
            JsonObject b = cuerpo(categoria);
            String nombre = campoTexto(b, "nombre");
            if (nombre.isEmpty()) throw new ErrorNegocio(400, "Ingresa el nombre de la categoría");
            Map<String, Object> datos = new LinkedHashMap<>();
            datos.put("nombre", nombre);
            datos.put("factor", Math.max(1, campoLargo(b, "factor")));
            return verificarUnico(CATEGORIAS, "nombre", nombre, null, "Esa categoría ya existe")
                    .continueWithTask(t -> {
                        t.getResult();
                        return crearConId(CATEGORIAS, datos);
                    })
                    .continueWith(t -> respuesta(CategoriaResponse.class, Collections.singletonList(t.getResult()), "Categoría creada"));
        });
    }

    // ==========================================
    // CRUD PRODUCTOS
    // ==========================================

    @Override
    public Call<ProductoResponse> getProductos() {
        return llamada(() -> listarProductos().continueWith(t -> respuesta(ProductoResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<JsonElement> getProductosElement() {
        return llamada(() -> listarProductos().continueWith(t -> envoltorio(t.getResult())));
    }

    @Override
    public Call<List<Producto>> getProductosDirectList() {
        return llamada(() -> listarProductos().continueWith(t -> modelos(t.getResult(), Producto.class)));
    }

    @Override
    public Call<Producto> getProductoById(int id) {
        return llamada(() -> obtener(PRODUCTOS, id).continueWith(t -> modelo(t.getResult(), Producto.class)));
    }

    @Override
    public Call<ProductoResponse> crearProducto(Producto producto) {
        return llamada(() -> perfilActual().continueWithTask(tPerfil -> {
            Map<String, Object> perfil = tPerfil.getResult();
            JsonObject b = cuerpo(producto);
            Map<String, Object> datos = datosProducto(b);
            long stockInicial = Math.max(0, campoLargo(b, "stock_unidades"));
            datos.put("stock_unidades", stockInicial);
            datos.put("activo", 1);
            datos.put("created_at", Timestamp.now());
            return db.runTransaction(tx -> {
                long id = siguienteId(tx, PRODUCTOS);
                datos.put("id", id);
                tx.set(db.collection(PRODUCTOS).document(String.valueOf(id)), datos);
                if (stockInicial > 0) {
                    registrarMovimiento(tx, perfil, id, texto(datos.get("nombre")), "entrada", stockInicial,
                            0, stockInicial, "Stock inicial", null, null);
                }
                guardarContador(tx, PRODUCTOS, id);
                return datos;
            });
        }).continueWith(t -> respuesta(ProductoResponse.class, Collections.singletonList(t.getResult()), "Producto creado")));
    }

    /** El stock no se edita aquí: solo cambia con ventas, compras o ajustes de inventario. */
    @Override
    public Call<ProductoResponse> editarProducto(int id, Producto producto) {
        return llamada(() -> {
            Map<String, Object> cambios = datosProducto(cuerpo(producto));
            cambios.put("activo", producto.getActivo() == 0 ? 0 : 1);
            DocumentReference ref = db.collection(PRODUCTOS).document(String.valueOf(id));
            return ref.update(cambios)
                    .continueWithTask(t -> {
                        t.getResult();
                        return obtener(PRODUCTOS, id);
                    })
                    .continueWith(t -> respuesta(ProductoResponse.class, Collections.singletonList(t.getResult()), "Producto actualizado"));
        });
    }

    /** Baja lógica: el producto deja de listarse pero sigue en el historial de pedidos y compras. */
    @Override
    public Call<ProductoResponse> eliminarProducto(int id) {
        return llamada(() -> db.collection(PRODUCTOS).document(String.valueOf(id)).update("activo", 0)
                .continueWith(t -> {
                    t.getResult();
                    return respuesta(ProductoResponse.class, new ArrayList<>(), "Producto eliminado");
                }));
    }

    // ==========================================
    // CRUD PEDIDOS / OPERACIONES DE VENTA
    // ==========================================

    @Override
    public Call<PedidoResponse> getPedidos() {
        return llamada(() -> perfilActual()
                .continueWithTask(t -> listarPedidosPara(t.getResult()))
                .continueWith(t -> respuesta(PedidoResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<List<Pedido>> getPedidosDirectList() {
        return llamada(() -> perfilActual()
                .continueWithTask(t -> listarPedidosPara(t.getResult()))
                .continueWith(t -> modelos(t.getResult(), Pedido.class)));
    }

    @Override
    public Call<Pedido> getPedidoById(int id) {
        return llamada(() -> obtener(PEDIDOS, id).continueWith(t -> modelo(t.getResult(), Pedido.class)));
    }

    @Override
    public Call<JsonElement> getPedidoByIdJson(int id) {
        return llamada(() -> obtener(PEDIDOS, id).continueWith(t -> aJson(t.getResult())));
    }

    /** No se usa en la app: los pedidos se crean con crearPedidoNuevo. */
    @Override
    public Call<PedidoResponse> crearPedido(Pedido pedido) {
        return llamada(() -> Tasks.<PedidoResponse>forException(new ErrorNegocio(400, "Usa crearPedidoNuevo para registrar pedidos")));
    }

    /**
     * Registra una venta o pedido en una transacción: calcula precios desde el catálogo,
     * expande las ofertas en sus productos, valida y descuenta el stock y guarda los movimientos.
     * Sin cliente (venta de mostrador) el pedido nace "entregado"; con cliente queda "pendiente".
     */
    @Override
    public Call<PedidoResponse> crearPedidoNuevo(PedidoRequest request) {
        return llamada(() -> {
            JsonObject b = cuerpo(request);
            JsonArray items = b.has("items") && b.get("items").isJsonArray() ? b.getAsJsonArray("items") : new JsonArray();
            if (items.size() == 0) throw new ErrorNegocio(400, "El pedido no tiene productos");
            String metodoPago = campoTexto(b, "metodo_pago");
            Long clienteId = campoLargoONull(b, "cliente_id");

            return perfilActual().continueWithTask(tPerfil -> {
                Map<String, Object> perfil = tPerfil.getResult();
                Task<Map<String, Object>> tCliente;
                if ("cliente".equals(rol(perfil))) {
                    tCliente = Tasks.forResult(perfil);
                } else if (clienteId != null && clienteId > 0) {
                    tCliente = buscarPorId(USUARIOS, clienteId);
                } else {
                    tCliente = Tasks.forResult(null);
                }
                return tCliente.continueWithTask(tc -> {
                    Map<String, Object> cliente = tc.getResult();
                    return db.runTransaction(tx -> registrarPedido(tx, perfil, cliente,
                            metodoPago.isEmpty() ? "Efectivo" : metodoPago, items));
                });
            }).continueWith(t -> respuesta(PedidoResponse.class, Collections.singletonList(t.getResult()), "Pedido registrado"));
        });
    }

    /** Cambia el estado. Al cancelar devuelve al inventario las unidades del pedido. */
    @Override
    public Call<PedidoResponse> editarPedido(int id, Pedido pedido) {
        return llamada(() -> {
            String nuevoEstado = pedido.getEstado() != null ? pedido.getEstado().trim().toLowerCase(Locale.ROOT) : "";
            if (!ESTADOS_PEDIDO.contains(nuevoEstado)) throw new ErrorNegocio(400, "Estado de pedido no válido");
            String fechaEntrega = pedido.getFechaEstimadaEntrega();
            DocumentReference ref = db.collection(PEDIDOS).document(String.valueOf(id));

            return perfilActual().continueWithTask(tPerfil -> {
                Map<String, Object> perfil = tPerfil.getResult();
                return db.runTransaction(tx -> {
                    DocumentSnapshot snap = tx.get(ref);
                    if (!snap.exists()) throw new ErrorNegocio(404, "El pedido no existe");
                    String anterior = texto(snap.get("estado"));
                    Map<String, Object> resultado = datos(snap);
                    if (nuevoEstado.equals(anterior) && fechaEntrega == null) return resultado;
                    if ("cancelado".equals(anterior)) throw new ErrorNegocio(400, "El pedido ya fue cancelado");

                    Map<String, Object> cambios = new HashMap<>();
                    cambios.put("estado", nuevoEstado);
                    cambios.put("actualizado_en", Timestamp.now());
                    if (fechaEntrega != null) cambios.put("fecha_estimada_entrega", fechaEntrega);

                    if ("cancelado".equals(nuevoEstado)) {
                        Map<String, Object> unidades = mapa(snap.get("unidades_por_producto"));
                        Map<String, DocumentSnapshot> productos = new LinkedHashMap<>();
                        for (String productoId : unidades.keySet()) {
                            productos.put(productoId, tx.get(db.collection(PRODUCTOS).document(productoId)));
                        }
                        for (Map.Entry<String, DocumentSnapshot> e : productos.entrySet()) {
                            DocumentSnapshot sp = e.getValue();
                            if (!sp.exists()) continue;
                            long devolver = largo(unidades.get(e.getKey()));
                            long stock = largo(sp.get("stock_unidades"));
                            tx.update(sp.getReference(), "stock_unidades", stock + devolver);
                            registrarMovimiento(tx, perfil, largo(sp.get("id")), texto(sp.get("nombre")), "REVERSIÓN",
                                    devolver, stock, stock + devolver, "Pedido #" + id + " cancelado", (long) id, null);
                        }
                    }
                    tx.update(ref, cambios);
                    resultado.putAll(cambios);
                    return resultado;
                });
            }).continueWith(t -> respuesta(PedidoResponse.class, Collections.singletonList(t.getResult()), "Pedido actualizado"));
        });
    }

    /** Los pedidos no se borran para no perder el historial de ventas; se cancelan. */
    @Override
    public Call<PedidoResponse> eliminarPedido(int id) {
        return llamada(() -> Tasks.<PedidoResponse>forException(new ErrorNegocio(400, "Los pedidos no se eliminan; cámbialos a cancelado")));
    }

    // ==========================================
    // INVENTARIO
    // ==========================================

    @Override
    public Call<InventarioResponse> getInventario() {
        return llamada(() -> listarInventario().continueWith(t -> respuesta(InventarioResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<List<Inventario>> getInventarioDirectList() {
        return llamada(() -> listarInventario().continueWith(t -> modelos(t.getResult(), Inventario.class)));
    }

    @Override
    public Call<Inventario> getInventarioByProductoId(int productoId) {
        return llamada(() -> listarInventario().continueWith(t -> {
            for (Map<String, Object> item : t.getResult()) {
                if (largo(item.get("producto_id")) == productoId) return modelo(item, Inventario.class);
            }
            throw new ErrorNegocio(404, "El producto no existe");
        }));
    }

    @Override
    public Call<MovimientoInventarioResponse> getMovimientosInventario(int productoId) {
        return llamada(() -> listarMovimientos(productoId).continueWith(t -> respuesta(MovimientoInventarioResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<List<MovimientoInventario>> getMovimientosInventarioDirectList(int productoId) {
        return llamada(() -> listarMovimientos(productoId).continueWith(t -> modelos(t.getResult(), MovimientoInventario.class)));
    }

    /** tipo "entrada" suma y "salida" resta cantidad_unidades; la salida no puede dejar stock negativo. */
    @Override
    public Call<InventarioResponse> ajustarInventario(int productoId, InventarioAjusteRequest request) {
        return llamada(() -> {
            JsonObject b = cuerpo(request);
            String tipo = campoTexto(b, "tipo").toLowerCase(Locale.ROOT);
            long cantidad = campoLargo(b, "cantidad_unidades");
            String motivo = campoTexto(b, "motivo");
            if (cantidad <= 0) throw new ErrorNegocio(400, "La cantidad debe ser mayor a 0");
            if (!"entrada".equals(tipo) && !"salida".equals(tipo)) throw new ErrorNegocio(400, "Tipo de ajuste no válido");
            DocumentReference ref = db.collection(PRODUCTOS).document(String.valueOf(productoId));

            return perfilActual().continueWithTask(tPerfil -> {
                Map<String, Object> perfil = tPerfil.getResult();
                return db.runTransaction(tx -> {
                    DocumentSnapshot sp = tx.get(ref);
                    if (!sp.exists()) throw new ErrorNegocio(404, "El producto no existe");
                    long stock = largo(sp.get("stock_unidades"));
                    long nuevo = "entrada".equals(tipo) ? stock + cantidad : stock - cantidad;
                    if (nuevo < 0) {
                        throw new ErrorNegocio(400, "La salida supera el stock disponible (" + stock + " unidades)");
                    }
                    tx.update(ref, "stock_unidades", nuevo);
                    registrarMovimiento(tx, perfil, productoId, texto(sp.get("nombre")), tipo, cantidad, stock, nuevo,
                            motivo.isEmpty() ? "Ajuste manual" : motivo, null, null);
                    Map<String, Object> producto = datos(sp);
                    producto.put("stock_unidades", nuevo);
                    return producto;
                });
            }).continueWith(t -> respuesta(InventarioResponse.class,
                    Collections.singletonList(itemInventario(t.getResult(), "")), "Inventario actualizado"));
        });
    }

    // ==========================================
    // COMPRAS
    // ==========================================

    @Override
    public Call<CompraResponse> getCompras() {
        return llamada(() -> listarPorIdDesc(COMPRAS).continueWith(t -> respuesta(CompraResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<List<Compra>> getComprasDirectList() {
        return llamada(() -> listarPorIdDesc(COMPRAS).continueWith(t -> modelos(t.getResult(), Compra.class)));
    }

    @Override
    public Call<Compra> getCompraById(int id) {
        return llamada(() -> obtener(COMPRAS, id).continueWith(t -> modelo(t.getResult(), Compra.class)));
    }

    /** Registra el ingreso de mercadería: suma stock, actualiza el precio de compra y guarda movimientos. */
    @Override
    public Call<CompraResponse> crearCompra(CompraRequest request) {
        return llamada(() -> {
            JsonObject b = cuerpo(request);
            long proveedorId = campoLargo(b, "proveedor_id");
            JsonArray detalles = b.has("detalles") && b.get("detalles").isJsonArray() ? b.getAsJsonArray("detalles") : new JsonArray();
            if (detalles.size() == 0) throw new ErrorNegocio(400, "Agrega al menos un producto a la compra");

            return perfilActual().continueWithTask(tPerfil -> {
                Map<String, Object> perfil = tPerfil.getResult();
                return db.runTransaction(tx -> {
                    long id = siguienteId(tx, COMPRAS);
                    DocumentSnapshot proveedor = tx.get(db.collection(PROVEEDORES).document(String.valueOf(proveedorId)));
                    if (!proveedor.exists()) throw new ErrorNegocio(400, "Selecciona un proveedor válido");

                    Map<String, DocumentSnapshot> productos = new LinkedHashMap<>();
                    for (JsonElement el : detalles) {
                        String productoId = String.valueOf(campoLargo(el.getAsJsonObject(), "producto_id"));
                        if (!productos.containsKey(productoId)) {
                            DocumentSnapshot sp = tx.get(db.collection(PRODUCTOS).document(productoId));
                            if (!sp.exists()) throw new ErrorNegocio(400, "El producto #" + productoId + " no existe");
                            productos.put(productoId, sp);
                        }
                    }

                    Map<String, Long> stockActual = new HashMap<>();
                    for (Map.Entry<String, DocumentSnapshot> e : productos.entrySet()) {
                        stockActual.put(e.getKey(), largo(e.getValue().get("stock_unidades")));
                    }

                    List<Map<String, Object>> lineas = new ArrayList<>();
                    double total = 0;
                    for (JsonElement el : detalles) {
                        JsonObject it = el.getAsJsonObject();
                        String productoId = String.valueOf(campoLargo(it, "producto_id"));
                        DocumentSnapshot sp = productos.get(productoId);
                        long factor = Math.max(1, largo(sp.get("unidades_por_paquete")));
                        long paquetes = Math.max(0, campoLargo(it, "cantidad_paquetes"));
                        long sueltas = Math.max(0, campoLargo(it, "cantidad_unidades"));
                        long unidades = paquetes * factor + sueltas;
                        if (unidades <= 0) throw new ErrorNegocio(400, "Cantidad inválida para " + texto(sp.get("nombre")));
                        double precioPaquete = campoDecimal(it, "precio_compra_paquete");
                        double subtotal = redondear(paquetes * precioPaquete + sueltas * (precioPaquete / factor));
                        total += subtotal;

                        Map<String, Object> linea = new LinkedHashMap<>();
                        linea.put("id", lineas.size() + 1);
                        linea.put("compra_id", id);
                        linea.put("producto_id", largo(sp.get("id")));
                        linea.put("producto_nombre", texto(sp.get("nombre")));
                        linea.put("cantidad_paquetes", paquetes);
                        linea.put("cantidad_unidades", sueltas);
                        linea.put("unidades_por_paquete_snapshot", factor);
                        linea.put("unidades_ingresadas", unidades);
                        linea.put("precio_compra_paquete", precioPaquete);
                        linea.put("subtotal", subtotal);
                        lineas.add(linea);

                        long antes = stockActual.get(productoId);
                        stockActual.put(productoId, antes + unidades);
                        Map<String, Object> cambios = new HashMap<>();
                        cambios.put("stock_unidades", antes + unidades);
                        if (precioPaquete > 0) cambios.put("precio_compra_paquete", precioPaquete);
                        tx.update(sp.getReference(), cambios);
                        Map<String, Object> extra = new HashMap<>();
                        extra.put("compra_id", id);
                        extra.put("proveedor_nombre", texto(proveedor.get("nombre_empresa")));
                        registrarMovimiento(tx, perfil, largo(sp.get("id")), texto(sp.get("nombre")), "COMPRA", unidades,
                                antes, antes + unidades, "Compra #" + id, null, extra);
                    }

                    Map<String, Object> compra = new LinkedHashMap<>();
                    compra.put("id", id);
                    compra.put("proveedor_id", proveedorId);
                    compra.put("proveedor_nombre", texto(proveedor.get("nombre_empresa")));
                    compra.put("usuario_id", largo(perfil.get("id")));
                    compra.put("usuario_uid", perfil.get("uid"));
                    compra.put("total", redondear(total));
                    compra.put("fecha", Timestamp.now());
                    compra.put("detalles", lineas);
                    tx.set(db.collection(COMPRAS).document(String.valueOf(id)), compra);
                    guardarContador(tx, COMPRAS, id);
                    return compra;
                });
            }).continueWith(t -> respuesta(CompraResponse.class, Collections.singletonList(t.getResult()), "Compra registrada"));
        });
    }

    // ==========================================
    // OFERTAS
    // ==========================================

    @Override
    public Call<OfertaResponse> getOfertas() {
        return llamada(() -> listarOfertas(false).continueWith(t -> respuesta(OfertaResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<List<Oferta>> getOfertasDirectList() {
        return llamada(() -> listarOfertas(false).continueWith(t -> modelos(t.getResult(), Oferta.class)));
    }

    @Override
    public Call<OfertaResponse> getOfertasCatalogo() {
        return llamada(() -> listarOfertas(true).continueWith(t -> respuesta(OfertaResponse.class, t.getResult(), null)));
    }

    @Override
    public Call<List<Oferta>> getOfertasCatalogoDirectList() {
        return llamada(() -> listarOfertas(true).continueWith(t -> modelos(t.getResult(), Oferta.class)));
    }

    @Override
    public Call<OfertaResponse> crearOferta(OfertaRequest request) {
        return llamada(() -> armarOferta(cuerpo(request))
                .continueWithTask(t -> crearConId(OFERTAS, t.getResult()))
                .continueWith(t -> respuesta(OfertaResponse.class, Collections.singletonList(t.getResult()), "Oferta creada")));
    }

    @Override
    public Call<OfertaResponse> editarOferta(int id, OfertaRequest request) {
        return llamada(() -> armarOferta(cuerpo(request)).continueWithTask(t -> {
            Map<String, Object> datos = t.getResult();
            datos.put("id", id);
            return db.collection(OFERTAS).document(String.valueOf(id)).update(datos).continueWith(t2 -> {
                t2.getResult();
                return respuesta(OfertaResponse.class, Collections.singletonList(datos), "Oferta actualizada");
            });
        }));
    }

    /** La pantalla lo presenta como "Desactivar": la oferta deja el catálogo pero se conserva. */
    @Override
    public Call<OfertaResponse> eliminarOferta(int id) {
        return llamada(() -> db.collection(OFERTAS).document(String.valueOf(id)).update("activo", 0).continueWith(t -> {
            t.getResult();
            return respuesta(OfertaResponse.class, new ArrayList<>(), "Oferta desactivada");
        }));
    }

    // ==========================================
    // LÓGICA DE NEGOCIO
    // ==========================================

    private Map<String, Object> registrarPedido(Transaction tx, Map<String, Object> perfil, Map<String, Object> cliente,
                                                String metodoPago, JsonArray items) throws FirebaseFirestoreException {
        // Firestore exige hacer todas las lecturas antes de la primera escritura
        long id = siguienteId(tx, PEDIDOS);
        Map<String, DocumentSnapshot> productos = new LinkedHashMap<>();
        Map<String, DocumentSnapshot> ofertas = new HashMap<>();
        for (JsonElement el : items) {
            JsonObject it = el.getAsJsonObject();
            if (esItemOferta(it)) {
                String ofertaId = String.valueOf(campoLargo(it, "oferta_id"));
                DocumentSnapshot so = ofertas.containsKey(ofertaId) ? ofertas.get(ofertaId)
                        : tx.get(db.collection(OFERTAS).document(ofertaId));
                if (!so.exists() || largo(so.get("activo"), 1) != 1) {
                    throw new ErrorNegocio(400, "La oferta #" + ofertaId + " ya no está disponible");
                }
                ofertas.put(ofertaId, so);
                for (Map<String, Object> comp : listaDeMapas(so.get("items"))) {
                    leerProducto(tx, productos, String.valueOf(largo(comp.get("producto_id"))));
                }
            } else {
                leerProducto(tx, productos, String.valueOf(campoLargo(it, "producto_id")));
            }
        }

        Map<String, Long> unidadesPorProducto = new LinkedHashMap<>();
        List<Map<String, Object>> detalles = new ArrayList<>();
        double total = 0;
        for (JsonElement el : items) {
            JsonObject it = el.getAsJsonObject();
            Map<String, Object> d = new LinkedHashMap<>();
            d.put("id", detalles.size() + 1);
            d.put("pedido_id", id);
            if (esItemOferta(it)) {
                DocumentSnapshot so = ofertas.get(String.valueOf(campoLargo(it, "oferta_id")));
                long cantidad = Math.max(1, campoLargo(it, "cantidad_ofertas"));
                double precio = decimal(so.get("precio_final"));
                double subtotal = redondear(precio * cantidad);
                for (Map<String, Object> comp : listaDeMapas(so.get("items"))) {
                    DocumentSnapshot sp = productos.get(String.valueOf(largo(comp.get("producto_id"))));
                    long factor = Math.max(1, largo(sp.get("unidades_por_paquete")));
                    long unidades = (largo(comp.get("cantidad_paquetes")) * factor + largo(comp.get("cantidad_unidades"))) * cantidad;
                    sumar(unidadesPorProducto, sp.getId(), unidades);
                }
                d.put("tipo", "OFERTA");
                d.put("oferta_id", largo(so.get("id")));
                d.put("nombre_oferta", texto(so.get("nombre")));
                d.put("cantidad_ofertas", cantidad);
                d.put("cantidad", cantidad);
                d.put("precio", precio);
                d.put("subtotal", subtotal);
                total += subtotal;
            } else {
                DocumentSnapshot sp = productos.get(String.valueOf(campoLargo(it, "producto_id")));
                String nombre = texto(sp.get("nombre"));
                String tipoVenta = campoTexto(it, "tipo_venta").toUpperCase(Locale.ROOT);
                if (tipoVenta.isEmpty()) tipoVenta = "PAQUETE";
                long paquetes = Math.max(0, campoLargo(it, "cantidad_paquetes"));
                long sueltas = Math.max(0, campoLargo(it, "cantidad_unidades"));
                if ("PAQUETE".equals(tipoVenta)) sueltas = 0;
                if ("UNIDAD".equals(tipoVenta)) paquetes = 0;
                if (paquetes + sueltas <= 0) throw new ErrorNegocio(400, "Cantidad inválida para " + nombre);
                long factor = Math.max(1, largo(sp.get("unidades_por_paquete")));
                double precioPaquete = decimal(sp.get("precio_venta_paquete"));
                double precioUnidad = decimal(sp.get("precio_venta_unidad"));
                double subtotal = redondear(paquetes * precioPaquete + sueltas * precioUnidad);
                long unidades = paquetes * factor + sueltas;
                sumar(unidadesPorProducto, sp.getId(), unidades);

                d.put("tipo", "PRODUCTO");
                d.put("producto_id", largo(sp.get("id")));
                d.put("producto_nombre", nombre);
                d.put("nombre_producto_snapshot", nombre);
                d.put("tipo_venta", tipoVenta);
                d.put("cantidad_paquetes", paquetes);
                d.put("cantidad_unidades", sueltas);
                d.put("unidades_por_paquete_snapshot", factor);
                d.put("unidades_totales", unidades);
                d.put("precio_paquete_snapshot", precioPaquete);
                d.put("precio_unidad_snapshot", precioUnidad);
                d.put("cantidad", "PAQUETE".equals(tipoVenta) ? paquetes : ("UNIDAD".equals(tipoVenta) ? sueltas : unidades));
                d.put("precio", "UNIDAD".equals(tipoVenta) ? precioUnidad : precioPaquete);
                d.put("subtotal", subtotal);
                total += subtotal;
            }
            detalles.add(d);
        }

        for (Map.Entry<String, Long> e : unidadesPorProducto.entrySet()) {
            DocumentSnapshot sp = productos.get(e.getKey());
            long stock = largo(sp.get("stock_unidades"));
            if (stock < e.getValue()) {
                throw new ErrorNegocio(400, "Stock insuficiente de " + texto(sp.get("nombre")) + ": hay " + stock
                        + " unidades y el pedido necesita " + e.getValue());
            }
        }

        String nombreCliente = cliente != null
                ? (texto(cliente.get("nombres")) + " " + texto(cliente.get("apellidos"))).trim()
                : "Venta Mostrador";

        Map<String, Object> pedido = new LinkedHashMap<>();
        pedido.put("id", id);
        pedido.put("cliente_id", cliente != null ? largo(cliente.get("id")) : null);
        pedido.put("cliente_uid", cliente != null ? cliente.get("uid") : null);
        pedido.put("cliente", nombreCliente);
        pedido.put("vendedor_uid", perfil.get("uid"));
        pedido.put("vendedor_nombre", (texto(perfil.get("nombres")) + " " + texto(perfil.get("apellidos"))).trim());
        pedido.put("total", redondear(total));
        pedido.put("metodo_pago", metodoPago);
        pedido.put("estado", cliente == null ? "entregado" : "pendiente");
        pedido.put("fecha", Timestamp.now());
        pedido.put("fecha_estimada_entrega", null);
        pedido.put("detalles", detalles);
        pedido.put("unidades_por_producto", new HashMap<String, Object>(unidadesPorProducto));
        tx.set(db.collection(PEDIDOS).document(String.valueOf(id)), pedido);

        for (Map.Entry<String, Long> e : unidadesPorProducto.entrySet()) {
            DocumentSnapshot sp = productos.get(e.getKey());
            long stock = largo(sp.get("stock_unidades"));
            tx.update(sp.getReference(), "stock_unidades", stock - e.getValue());
            registrarMovimiento(tx, perfil, largo(sp.get("id")), texto(sp.get("nombre")), "VENTA", e.getValue(),
                    stock, stock - e.getValue(), "Pedido #" + id, id, Collections.singletonMap("cliente_nombre", nombreCliente));
        }
        guardarContador(tx, PEDIDOS, id);
        return pedido;
    }

    private void leerProducto(Transaction tx, Map<String, DocumentSnapshot> productos, String productoId)
            throws FirebaseFirestoreException {
        if (productos.containsKey(productoId)) return;
        DocumentSnapshot sp = tx.get(db.collection(PRODUCTOS).document(productoId));
        if (!sp.exists() || largo(sp.get("activo"), 1) != 1) {
            throw new ErrorNegocio(400, "El producto #" + productoId + " ya no está disponible");
        }
        productos.put(productoId, sp);
    }

    /** Guarda el movimiento de stock dentro de la transacción; extra agrega datos como compra_id o cliente_nombre. */
    private void registrarMovimiento(Transaction tx, Map<String, Object> perfil, long productoId, String productoNombre,
                                     String tipo, long cantidad, long stockAnterior, long stockNuevo,
                                     String motivo, Long pedidoId, Map<String, Object> extra) {
        Map<String, Object> mov = new LinkedHashMap<>();
        mov.put("producto_id", productoId);
        mov.put("producto_nombre", productoNombre);
        mov.put("tipo", tipo);
        mov.put("cantidad_unidades", cantidad);
        mov.put("stock_anterior", stockAnterior);
        mov.put("stock_nuevo", stockNuevo);
        mov.put("motivo", motivo);
        mov.put("usuario_id", largo(perfil.get("id")));
        mov.put("usuario_uid", perfil.get("uid"));
        mov.put("usuario_nombre", (texto(perfil.get("nombres")) + " " + texto(perfil.get("apellidos"))).trim());
        mov.put("pedido_id", pedidoId);
        mov.put("fecha", Timestamp.now());
        if (extra != null) mov.putAll(extra);
        tx.set(db.collection(MOVIMIENTOS).document(), mov);
    }

    /** Valida los productos de la oferta y calcula el precio normal (suma de sus componentes). */
    private Task<Map<String, Object>> armarOferta(JsonObject b) {
        String nombre = campoTexto(b, "nombre");
        JsonArray items = b.has("items") && b.get("items").isJsonArray() ? b.getAsJsonArray("items") : new JsonArray();
        if (nombre.isEmpty()) return Tasks.forException(new ErrorNegocio(400, "Ingresa el nombre de la oferta"));
        if (items.size() == 0) return Tasks.forException(new ErrorNegocio(400, "Agrega productos a la oferta"));

        List<Task<DocumentSnapshot>> lecturas = new ArrayList<>();
        for (JsonElement el : items) {
            JsonObject it = el.getAsJsonObject();
            long productoId = campoLargo(it, "product_id");
            if (productoId <= 0) productoId = campoLargo(it, "producto_id");
            lecturas.add(db.collection(PRODUCTOS).document(String.valueOf(productoId)).get());
        }
        return Tasks.whenAllSuccess(lecturas).continueWith(t -> {
            List<Map<String, Object>> componentes = new ArrayList<>();
            double precioNormal = 0;
            for (int i = 0; i < items.size(); i++) {
                JsonObject it = items.get(i).getAsJsonObject();
                DocumentSnapshot sp = lecturas.get(i).getResult();
                if (!sp.exists()) throw new ErrorNegocio(400, "Uno de los productos de la oferta no existe");
                String tipoVenta = campoTexto(it, "tipo_venta").toUpperCase(Locale.ROOT);
                long paquetes = Math.max(0, campoLargo(it, "cantidad_paquetes"));
                long sueltas = Math.max(0, campoLargo(it, "cantidad_unidades"));
                precioNormal += paquetes * decimal(sp.get("precio_venta_paquete")) + sueltas * decimal(sp.get("precio_venta_unidad"));

                Map<String, Object> comp = new LinkedHashMap<>();
                comp.put("id", i + 1);
                comp.put("producto_id", largo(sp.get("id")));
                comp.put("producto_nombre", texto(sp.get("nombre")));
                comp.put("tipo_venta", tipoVenta.isEmpty() ? "PAQUETE" : tipoVenta);
                comp.put("cantidad_paquetes", paquetes);
                comp.put("cantidad_unidades", sueltas);
                componentes.add(comp);
            }
            double precioFinal = campoDecimal(b, "precio_final");
            if (precioFinal <= 0) throw new ErrorNegocio(400, "Ingresa el precio final de la oferta");
            double descuento = campoDecimal(b, "descuento");
            if (descuento <= 0 && precioNormal > 0) descuento = Math.max(0, (1 - precioFinal / precioNormal) * 100);

            Map<String, Object> oferta = new LinkedHashMap<>();
            oferta.put("nombre", nombre);
            oferta.put("precio_normal", redondear(precioNormal));
            oferta.put("precio_final", redondear(precioFinal));
            oferta.put("descuento", redondear(descuento));
            oferta.put("activo", campoLargo(b, "activo") == 0 ? 0 : 1);
            oferta.put("created_at", Timestamp.now());
            oferta.put("items", componentes);
            return oferta;
        });
    }

    // ==========================================
    // CONSULTAS
    // ==========================================

    private Task<Map<String, Object>> perfilActual() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            return Tasks.forException(new ErrorNegocio(401, "Tu sesión expiró. Vuelve a iniciar sesión."));
        }
        return db.collection(USUARIOS).document(user.getUid()).get().continueWith(t -> {
            DocumentSnapshot doc = t.getResult();
            if (!doc.exists()) throw new ErrorNegocio(403, "Tu cuenta no está habilitada");
            Map<String, Object> perfil = datos(doc);
            perfil.put("uid", doc.getId());
            return perfil;
        });
    }

    private Task<List<Map<String, Object>>> listarPedidosPara(Map<String, Object> perfil) {
        Query query = "cliente".equals(rol(perfil))
                ? db.collection(PEDIDOS).whereEqualTo("cliente_uid", perfil.get("uid"))
                : db.collection(PEDIDOS);
        return listar(query).continueWith(t -> ordenarPorIdDesc(t.getResult()));
    }

    private Task<List<Map<String, Object>>> listarUsuarios() {
        return listarOrdenado(USUARIOS, "nombres");
    }

    private Task<List<Map<String, Object>>> listarProveedores() {
        return listarOrdenado(PROVEEDORES, "nombre_empresa").continueWith(t -> {
            for (Map<String, Object> p : t.getResult()) conNombreEmpresa(p);
            return t.getResult();
        });
    }

    /** Productos activos con el nombre de su categoría y proveedor (para no mostrar solo ids). */
    private Task<List<Map<String, Object>>> listarProductos() {
        Task<List<Map<String, Object>>> tProductos = listar(db.collection(PRODUCTOS).whereEqualTo("activo", 1));
        Task<List<Map<String, Object>>> tCategorias = listar(db.collection(CATEGORIAS));
        Task<List<Map<String, Object>>> tProveedores = listar(db.collection(PROVEEDORES));
        return Tasks.whenAllComplete(tProductos, tCategorias, tProveedores).continueWith(t -> {
            List<Map<String, Object>> productos = tProductos.getResult();
            Map<Long, Object> categorias = new HashMap<>();
            Map<Long, Object> proveedores = new HashMap<>();
            if (tCategorias.isSuccessful()) {
                for (Map<String, Object> c : tCategorias.getResult()) categorias.put(largo(c.get("id")), c.get("nombre"));
            }
            if (tProveedores.isSuccessful()) {
                for (Map<String, Object> p : tProveedores.getResult()) proveedores.put(largo(p.get("id")), p.get("nombre_empresa"));
            }
            for (Map<String, Object> p : productos) {
                p.put("categoria_nombre", categorias.get(largo(p.get("categoria_id"))));
                p.put("proveedor_nombre", proveedores.get(largo(p.get("proveedor_id"))));
            }
            return ordenarPorTexto(productos, "nombre");
        });
    }

    private Task<List<Map<String, Object>>> listarOfertas(boolean soloActivas) {
        Query query = soloActivas ? db.collection(OFERTAS).whereEqualTo("activo", 1) : db.collection(OFERTAS);
        return listar(query).continueWith(t -> ordenarPorIdDesc(t.getResult()));
    }

    private Task<List<Map<String, Object>>> listarInventario() {
        Task<List<Map<String, Object>>> tProductos = listarProductos();
        Task<List<Map<String, Object>>> tCategorias = listar(db.collection(CATEGORIAS));
        return Tasks.whenAllSuccess(tProductos, tCategorias).continueWith(t -> {
            Map<Long, String> categorias = new HashMap<>();
            for (Map<String, Object> c : tCategorias.getResult()) categorias.put(largo(c.get("id")), texto(c.get("nombre")));
            List<Map<String, Object>> lista = new ArrayList<>();
            for (Map<String, Object> p : tProductos.getResult()) {
                String categoria = categorias.get(largo(p.get("categoria_id")));
                lista.add(itemInventario(p, categoria != null ? categoria : ""));
            }
            return lista;
        });
    }

    private Task<List<Map<String, Object>>> listarMovimientos(int productoId) {
        return listar(db.collection(MOVIMIENTOS).whereEqualTo("producto_id", productoId)).continueWith(t -> {
            List<Map<String, Object>> lista = t.getResult();
            Collections.sort(lista, (a, b) -> compararFechas(b.get("fecha"), a.get("fecha")));
            for (int i = 0; i < lista.size(); i++) lista.get(i).put("id", lista.size() - i);
            return lista;
        });
    }

    private Task<List<Map<String, Object>>> listarPorIdDesc(String coleccion) {
        return listar(db.collection(coleccion)).continueWith(t -> ordenarPorIdDesc(t.getResult()));
    }

    private Task<List<Map<String, Object>>> listarOrdenado(String coleccion, String campoTexto) {
        return listar(db.collection(coleccion)).continueWith(t -> ordenarPorTexto(t.getResult(), campoTexto));
    }

    private Task<List<Map<String, Object>>> listar(Query query) {
        return query.get().continueWith(t -> {
            List<Map<String, Object>> lista = new ArrayList<>();
            for (DocumentSnapshot d : t.getResult()) lista.add(datos(d));
            return lista;
        });
    }

    private Task<Map<String, Object>> obtener(String coleccion, long id) {
        return db.collection(coleccion).document(String.valueOf(id)).get().continueWith(t -> {
            DocumentSnapshot d = t.getResult();
            if (!d.exists()) throw new ErrorNegocio(404, "El registro #" + id + " no existe");
            return datos(d);
        });
    }

    /** Para usuarios, cuyo documento usa el uid y el id numérico es un campo. */
    private Task<DocumentSnapshot> buscarDocPorId(String coleccion, long id) {
        return db.collection(coleccion).whereEqualTo("id", id).limit(1).get().continueWith(t -> {
            if (t.getResult().isEmpty()) throw new ErrorNegocio(404, "El registro #" + id + " no existe");
            return t.getResult().getDocuments().get(0);
        });
    }

    private Task<Map<String, Object>> buscarPorId(String coleccion, long id) {
        return buscarDocPorId(coleccion, id).continueWith(t -> datos(t.getResult()));
    }

    private Task<Void> verificarUnico(String coleccion, String campo, Object valor, String excluirDocId, String mensaje) {
        if (valor == null || texto(valor).isEmpty()) return Tasks.forResult(null);
        return db.collection(coleccion).whereEqualTo(campo, valor).limit(2).get().continueWith(t -> {
            for (DocumentSnapshot d : t.getResult()) {
                if (!d.getId().equals(excluirDocId)) throw new ErrorNegocio(400, mensaje);
            }
            return null;
        });
    }

    private Task<Map<String, Object>> crearConId(String coleccion, Map<String, Object> datos) {
        if (!datos.containsKey("created_at")) datos.put("created_at", Timestamp.now());
        return db.runTransaction(tx -> {
            long id = siguienteId(tx, coleccion);
            datos.put("id", id);
            tx.set(db.collection(coleccion).document(String.valueOf(id)), datos);
            guardarContador(tx, coleccion, id);
            return datos;
        });
    }

    private long siguienteId(Transaction tx, String coleccion) throws FirebaseFirestoreException {
        return largo(tx.get(db.collection(CONTADORES).document(coleccion)).get("ultimo")) + 1;
    }

    private void guardarContador(Transaction tx, String coleccion, long id) {
        tx.set(db.collection(CONTADORES).document(coleccion), Collections.singletonMap("ultimo", id));
    }

    /**
     * Crea la cuenta en una instancia secundaria de FirebaseAuth para no cerrar la sesión
     * del administrador que está registrando al usuario.
     */
    private Task<String> crearCuentaAcceso(String correo, String password) {
        FirebaseApp principal = FirebaseApp.getInstance();
        FirebaseApp registro;
        try {
            registro = FirebaseApp.getInstance("registro");
        } catch (IllegalStateException e) {
            registro = FirebaseApp.initializeApp(principal.getApplicationContext(), principal.getOptions(), "registro");
        }
        FirebaseAuth authRegistro = FirebaseAuth.getInstance(registro);
        return authRegistro.createUserWithEmailAndPassword(correo, password).continueWith(t -> {
            String uid = t.getResult().getUser().getUid();
            authRegistro.signOut();
            return uid;
        });
    }

    // ==========================================
    // CONVERSIÓN DE DATOS
    // ==========================================

    private Map<String, Object> datosUsuario(JsonObject b) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("nombres", campoTexto(b, "nombres"));
        d.put("apellidos", campoTexto(b, "apellidos"));
        d.put("dni", campoTexto(b, "dni"));
        d.put("ruc", campoTexto(b, "ruc"));
        d.put("telefono", campoTexto(b, "telefono"));
        d.put("correo", campoTexto(b, "correo").toLowerCase(Locale.ROOT));
        d.put("usuario", campoTexto(b, "usuario"));
        d.put("direccion", campoTexto(b, "direccion"));
        String rol = campoTexto(b, "rol").toLowerCase(Locale.ROOT);
        d.put("rol", rol.isEmpty() ? "cliente" : rol);
        return d;
    }

    private Map<String, Object> datosProducto(JsonObject b) {
        String nombre = campoTexto(b, "nombre");
        if (nombre.isEmpty()) throw new ErrorNegocio(400, "Ingresa el nombre del producto");
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("nombre", nombre);
        long categoria = campoLargo(b, "categoria_id");
        d.put("categoria_id", categoria > 0 ? categoria : Math.max(1, campoLargo(b, "producto_categoria_id")));
        d.put("proveedor_id", Math.max(1, campoLargo(b, "proveedor_id")));
        d.put("unidades_por_paquete", Math.max(1, campoLargo(b, "unidades_por_paquete")));
        d.put("precio_compra_paquete", redondear(campoDecimal(b, "precio_compra_paquete")));
        d.put("precio_venta_paquete", redondear(campoDecimal(b, "precio_venta_paquete")));
        d.put("precio_venta_unidad", redondear(campoDecimal(b, "precio_venta_unidad")));
        return d;
    }

    private Map<String, Object> itemInventario(Map<String, Object> p, String categoria) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("id", p.get("id"));
        item.put("producto_id", p.get("id"));
        item.put("producto_nombre", p.get("nombre"));
        item.put("stock_unidades", p.get("stock_unidades"));
        item.put("unidades_por_paquete", p.get("unidades_por_paquete"));
        item.put("categoria", categoria);
        return item;
    }

    private Map<String, Object> conNombreEmpresa(Map<String, Object> proveedor) {
        proveedor.put("nombreEmpresa", proveedor.get("nombre_empresa"));
        return proveedor;
    }

    private <T> FirebaseCall<T> llamada(FirebaseCall.Operacion<T> operacion) {
        return new FirebaseCall<>(operacion);
    }

    private static Map<String, Object> datos(DocumentSnapshot d) {
        Map<String, Object> m = d.getData();
        return m != null ? new LinkedHashMap<>(m) : new LinkedHashMap<>();
    }

    private JsonObject cuerpo(Object request) {
        JsonElement el = gson.toJsonTree(request);
        return el != null && el.isJsonObject() ? el.getAsJsonObject() : new JsonObject();
    }

    private <R> R respuesta(Class<R> clase, Object data, String mensaje) {
        JsonObject o = new JsonObject();
        o.addProperty("success", true);
        o.add("data", aJson(data));
        if (mensaje != null) o.addProperty("message", mensaje);
        return gson.fromJson(o, clase);
    }

    private JsonElement envoltorio(Object data) {
        JsonObject o = new JsonObject();
        o.addProperty("success", true);
        o.add("data", aJson(data));
        return o;
    }

    private <R> R modelo(Map<String, Object> data, Class<R> clase) {
        return gson.fromJson(aJson(data), clase);
    }

    private <R> List<R> modelos(List<Map<String, Object>> lista, Class<R> clase) {
        List<R> r = new ArrayList<>();
        for (Map<String, Object> m : lista) r.add(modelo(m, clase));
        return r;
    }

    private JsonElement aJson(Object valor) {
        return gson.toJsonTree(normalizar(null, valor));
    }

    /** Fechas a "yyyy-MM-dd HH:mm:ss" (formato que ya parsean las pantallas) y dinero a texto con 2 decimales. */
    private Object normalizar(String clave, Object v) {
        if (v instanceof Timestamp) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(((Timestamp) v).toDate());
        }
        if (v instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format((Date) v);
        }
        if (v instanceof Number && clave != null && CAMPOS_DINERO.contains(clave)) {
            return String.format(Locale.US, "%.2f", ((Number) v).doubleValue());
        }
        if (v instanceof Map) {
            Map<String, Object> r = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : ((Map<?, ?>) v).entrySet()) {
                String k = String.valueOf(e.getKey());
                r.put(k, normalizar(k, e.getValue()));
            }
            return r;
        }
        if (v instanceof List) {
            List<Object> r = new ArrayList<>();
            for (Object o : (List<?>) v) r.add(normalizar(clave, o));
            return r;
        }
        return v;
    }

    private static List<Map<String, Object>> ordenarPorIdDesc(List<Map<String, Object>> lista) {
        Collections.sort(lista, (a, b) -> Long.compare(largo(b.get("id")), largo(a.get("id"))));
        return lista;
    }

    private static List<Map<String, Object>> ordenarPorTexto(List<Map<String, Object>> lista, String campo) {
        Collections.sort(lista, (a, b) -> texto(a.get(campo)).compareToIgnoreCase(texto(b.get(campo))));
        return lista;
    }

    private static int compararFechas(Object a, Object b) {
        if (a instanceof Timestamp && b instanceof Timestamp) return ((Timestamp) a).compareTo((Timestamp) b);
        return a instanceof Timestamp ? 1 : (b instanceof Timestamp ? -1 : 0);
    }

    private static boolean esItemOferta(JsonObject it) {
        return "OFERTA".equalsIgnoreCase(campoTexto(it, "tipo")) && campoLargo(it, "oferta_id") > 0;
    }

    private static String rol(Map<String, Object> perfil) {
        return texto(perfil != null ? perfil.get("rol") : null).toLowerCase(Locale.ROOT);
    }

    private static boolean esPersonal(Map<String, Object> perfil) {
        String rol = rol(perfil);
        return "admin".equals(rol) || "vendedor".equals(rol) || "chofer".equals(rol);
    }

    private static void sumar(Map<String, Long> mapa, String clave, long valor) {
        Long actual = mapa.get(clave);
        mapa.put(clave, (actual != null ? actual : 0) + valor);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapa(Object v) {
        return v instanceof Map ? (Map<String, Object>) v : new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listaDeMapas(Object v) {
        List<Map<String, Object>> r = new ArrayList<>();
        if (v instanceof List) {
            for (Object o : (List<?>) v) if (o instanceof Map) r.add((Map<String, Object>) o);
        }
        return r;
    }

    static String texto(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }

    static long largo(Object v) {
        return largo(v, 0);
    }

    static long largo(Object v, long defecto) {
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof String && !TextUtils.isEmpty((String) v)) {
            try {
                return (long) Double.parseDouble((String) v);
            } catch (NumberFormatException ignored) {
            }
        }
        return defecto;
    }

    static double decimal(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v instanceof String && !TextUtils.isEmpty((String) v)) {
            try {
                return Double.parseDouble(((String) v).replace(",", "."));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String campoTexto(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        return e == null || e.isJsonNull() ? "" : e.getAsString().trim();
    }

    private static long campoLargo(JsonObject o, String campo) {
        Long v = campoLargoONull(o, campo);
        return v != null ? v : 0;
    }

    private static Long campoLargoONull(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        if (e == null || e.isJsonNull()) return null;
        try {
            return (long) e.getAsDouble();
        } catch (Exception ex) {
            return null;
        }
    }

    private static double campoDecimal(JsonObject o, String campo) {
        JsonElement e = o.get(campo);
        if (e == null || e.isJsonNull()) return 0;
        try {
            return Double.parseDouble(e.getAsString().replace(",", "."));
        } catch (Exception ex) {
            return 0;
        }
    }
}
