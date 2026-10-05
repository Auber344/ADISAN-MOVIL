package com.example.adisan_app_android;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("api/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @GET("api/dashboard")
    Call<DashboardResponse> getDashboardData(@Header("Authorization") String token);

    // ==========================================
    // CLIENTES / USUARIOS
    // ==========================================

    @GET("api/usuarios")
    Call<UsuarioResponse> getUsuarios();

    @GET("api/usuarios/{id}")
    Call<Usuario> getUsuarioById(@Path("id") int id);

    @GET("api/usuarios")
    Call<UsuarioResponse> buscarUsuarios(@Query("search") String search);

    @GET("api/usuarios")
    Call<List<Usuario>> getUsuariosDirectList();

    @POST("api/usuarios")
    Call<UsuarioResponse> crearUsuario(@Body Usuario usuario);

    @PUT("api/usuarios/{id}")
    Call<UsuarioResponse> editarUsuario(@Path("id") int id, @Body Usuario usuario);

    @DELETE("api/usuarios/{id}")
    Call<UsuarioResponse> eliminarUsuario(@Path("id") int id);

    @PUT("api/usuarios/{id}/reset-password")
    Call<UsuarioResponse> resetPasswordUsuario(@Path("id") int id, @Body ResetPasswordRequest request);

    // ==========================================
    // PROVEEDORES
    // ==========================================

    @GET("api/proveedores")
    Call<ProveedorResponse> getProveedores();

    @GET("api/proveedores")
    Call<List<Proveedor>> getProveedoresDirectList();

    @POST("api/proveedores")
    Call<ProveedorResponse> crearProveedor(@Body Proveedor proveedor);

    // ==========================================
    // CATEGORÍAS
    // ==========================================

    @GET("api/categorias")
    Call<List<Categoria>> getCategorias();

    @GET("api/categorias")
    Call<com.google.gson.JsonElement> getCategoriasElement();

    @GET("api/categorias")
    Call<CategoriaResponse> getCategoriasResponse();

    @POST("api/categorias")
    Call<CategoriaResponse> crearCategoria(@Body Categoria categoria);

    // ==========================================
    // CRUD PRODUCTOS
    // ==========================================

    @GET("api/productos")
    Call<ProductoResponse> getProductos();

    @GET("api/productos")
    Call<com.google.gson.JsonElement> getProductosElement();

    @GET("api/productos")
    Call<List<Producto>> getProductosDirectList();

    @GET("api/productos/{id}")
    Call<Producto> getProductoById(@Path("id") int id);

    @POST("api/productos")
    Call<ProductoResponse> crearProducto(@Body Producto producto);

    @PUT("api/productos/{id}")
    Call<ProductoResponse> editarProducto(@Path("id") int id, @Body Producto producto);

    @DELETE("api/productos/{id}")
    Call<ProductoResponse> eliminarProducto(@Path("id") int id);

    // ==========================================
    // CRUD PEDIDOS / OPERACIONES DE VENTA
    // ==========================================

    @GET("api/pedidos")
    Call<PedidoResponse> getPedidos();

    @GET("api/pedidos")
    Call<List<Pedido>> getPedidosDirectList();

    @GET("api/pedidos/{id}")
    Call<Pedido> getPedidoById(@Path("id") int id);

    @GET("api/pedidos/{id}")
    Call<com.google.gson.JsonElement> getPedidoByIdJson(@Path("id") int id);

    @POST("api/pedidos")
    Call<PedidoResponse> crearPedido(@Body Pedido pedido);

    @POST("api/pedidos")
    Call<PedidoResponse> crearPedidoNuevo(@Body PedidoRequest request);

    @PUT("api/pedidos/{id}")
    Call<PedidoResponse> editarPedido(@Path("id") int id, @Body Pedido pedido);

    @DELETE("api/pedidos/{id}")
    Call<PedidoResponse> eliminarPedido(@Path("id") int id);

    // ==========================================
    // INVENTARIO
    // ==========================================

    @GET("api/inventario")
    Call<InventarioResponse> getInventario();

    @GET("api/inventario")
    Call<List<Inventario>> getInventarioDirectList();

    @GET("api/inventario/{productoId}")
    Call<Inventario> getInventarioByProductoId(@Path("productoId") int productoId);

    @GET("api/inventario/{productoId}/movimientos")
    Call<MovimientoInventarioResponse> getMovimientosInventario(@Path("productoId") int productoId);

    @GET("api/inventario/{productoId}/movimientos")
    Call<List<MovimientoInventario>> getMovimientosInventarioDirectList(@Path("productoId") int productoId);

    @POST("api/inventario/{productoId}/ajustes")
    Call<InventarioResponse> ajustarInventario(@Path("productoId") int productoId, @Body InventarioAjusteRequest request);

    // ==========================================
    // COMPRAS
    // ==========================================

    @GET("api/compras")
    Call<CompraResponse> getCompras();

    @GET("api/compras")
    Call<List<Compra>> getComprasDirectList();

    @GET("api/compras/{id}")
    Call<Compra> getCompraById(@Path("id") int id);

    @POST("api/compras")
    Call<CompraResponse> crearCompra(@Body CompraRequest request);

    // ==========================================
    // OFERTAS
    // ==========================================

    @GET("api/ofertas")
    Call<OfertaResponse> getOfertas();

    @GET("api/ofertas")
    Call<List<Oferta>> getOfertasDirectList();

    @GET("api/ofertas/catalogo")
    Call<OfertaResponse> getOfertasCatalogo();

    @GET("api/ofertas/catalogo")
    Call<List<Oferta>> getOfertasCatalogoDirectList();

    @POST("api/ofertas")
    Call<OfertaResponse> crearOferta(@Body OfertaRequest request);

    @PUT("api/ofertas/{id}")
    Call<OfertaResponse> editarOferta(@Path("id") int id, @Body OfertaRequest request);

    @DELETE("api/ofertas/{id}")
    Call<OfertaResponse> eliminarOferta(@Path("id") int id);
}