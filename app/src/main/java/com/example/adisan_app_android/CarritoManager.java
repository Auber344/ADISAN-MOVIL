package com.example.adisan_app_android;

import java.util.ArrayList;
import java.util.List;

public class CarritoManager {

    private static CarritoManager instance;
    private final List<ItemCarrito> items = new ArrayList<>();

    public static synchronized CarritoManager getInstance() {
        if (instance == null) {
            instance = new CarritoManager();
        }
        return instance;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void agregarProducto(Producto producto, String tipoVenta, int cantidadPaquetes, int cantidadUnidades) {
        if (producto == null) return;

        for (ItemCarrito item : items) {
            if (!item.isOferta() && item.getProductoId() == producto.getProductoId() && item.getTipoVenta().equalsIgnoreCase(tipoVenta)) {
                item.setCantidadPaquetes(item.getCantidadPaquetes() + cantidadPaquetes);
                item.setCantidadUnidades(item.getCantidadUnidades() + cantidadUnidades);
                return;
            }
        }

        items.add(new ItemCarrito(producto, tipoVenta, cantidadPaquetes, cantidadUnidades));
    }

    public void agregarOferta(Oferta oferta, int cantidadOfertas) {
        if (oferta == null || cantidadOfertas <= 0) return;

        for (ItemCarrito item : items) {
            if (item.isOferta() && item.getOfertaId() == oferta.getId()) {
                item.setCantidadOfertas(item.getCantidadOfertas() + cantidadOfertas);
                return;
            }
        }

        items.add(new ItemCarrito(oferta, cantidadOfertas));
    }

    // Legacy support method
    public void agregarProducto(Producto producto, int cantidad) {
        agregarProducto(producto, "UNIDAD", 0, cantidad);
    }

    public void eliminarItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
        }
    }

    public double obtenerTotal() {
        double total = 0;
        for (ItemCarrito item : items) {
            total += item.getSubtotal();
        }
        return total;
    }

    public int obtenerCantidadTotalItems() {
        int count = 0;
        for (ItemCarrito item : items) {
            if (item.isOferta()) {
                count += item.getCantidadOfertas();
            } else {
                count += (item.getCantidadPaquetes() + item.getCantidadUnidades());
            }
        }
        return count;
    }

    public void limpiar() {
        items.clear();
    }
}
