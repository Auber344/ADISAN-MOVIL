package com.example.adisan_app_android;

public class ItemCarrito {

    private boolean isOferta;

    // Product fields
    private int productoId;
    private String productoNombre;
    private String tipoVenta; // 'PAQUETE', 'UNIDAD', 'MIXTA'
    private int cantidadPaquetes;
    private int cantidadUnidades;
    private int unidadesPorPaquete;
    private double precioVentaPaquete;
    private double precioVentaUnidad;
    private int stockUnidades;

    // Offer fields
    private int ofertaId;
    private String nombreOferta;
    private int cantidadOfertas;
    private double precioFinalVisual;

    // Legacy compatibility fields
    private int presentacionId = 1;
    private int stock;
    private String precio;
    private double precioDouble;

    public ItemCarrito(Producto producto, String tipoVenta, int cantidadPaquetes, int cantidadUnidades) {
        this.isOferta = false;
        this.productoId = producto.getProductoId();
        this.productoNombre = producto.getProductoNombre();
        this.tipoVenta = tipoVenta != null ? tipoVenta : "UNIDAD";
        this.cantidadPaquetes = cantidadPaquetes;
        this.cantidadUnidades = cantidadUnidades;
        this.unidadesPorPaquete = producto.getUnidadesPorPaqueteVal();

        double pPaq = 0.0;
        try {
            pPaq = Double.parseDouble(producto.getPrecioVentaPaquete());
        } catch (Exception ignored) {}

        double pUnid = 0.0;
        try {
            pUnid = Double.parseDouble(producto.getPrecioVentaUnidad());
        } catch (Exception ignored) {}

        this.precioVentaPaquete = pPaq;
        this.precioVentaUnidad = pUnid;
        this.stockUnidades = producto.getStockUnidadesVal();

        // Legacy compatibility
        this.stock = this.stockUnidades;
        this.precio = producto.getPrecioVenta();
        this.precioDouble = pPaq > 0 ? pPaq : pUnid;
    }

    public ItemCarrito(Oferta oferta, int cantidadOfertas) {
        this.isOferta = true;
        this.ofertaId = oferta.getId();
        this.nombreOferta = oferta.getNombre();
        this.cantidadOfertas = cantidadOfertas;
        this.precioFinalVisual = oferta.getPrecioFinalDouble();

        // Legacy compatibility
        this.productoId = 0;
        this.productoNombre = this.nombreOferta;
        this.stock = 999;
        this.precio = oferta.getPrecioFinal();
        this.precioDouble = this.precioFinalVisual;
        this.tipoVenta = "OFERTA";
    }

    public boolean isOferta() {
        return isOferta;
    }

    public int getProductoId() {
        return productoId;
    }

    public String getProductoNombre() {
        return isOferta ? nombreOferta : productoNombre;
    }

    public String getTipoVenta() {
        return isOferta ? "OFERTA" : (tipoVenta != null ? tipoVenta : "UNIDAD");
    }

    public void setTipoVenta(String tipoVenta) {
        if (!isOferta) this.tipoVenta = tipoVenta;
    }

    public int getCantidadPaquetes() {
        return isOferta ? 0 : cantidadPaquetes;
    }

    public void setCantidadPaquetes(int cantidadPaquetes) {
        if (!isOferta) this.cantidadPaquetes = Math.max(0, cantidadPaquetes);
    }

    public int getCantidadUnidades() {
        return isOferta ? 0 : cantidadUnidades;
    }

    public void setCantidadUnidades(int cantidadUnidades) {
        if (!isOferta) this.cantidadUnidades = Math.max(0, cantidadUnidades);
    }

    public int getUnidadesPorPaquete() {
        return unidadesPorPaquete > 0 ? unidadesPorPaquete : 1;
    }

    public double getPrecioVentaPaquete() {
        return precioVentaPaquete;
    }

    public double getPrecioVentaUnidad() {
        return precioVentaUnidad;
    }

    public int getStockUnidades() {
        return stockUnidades;
    }

    public int getCantidadTotalUnidades() {
        if (isOferta) return cantidadOfertas;
        String tv = getTipoVenta();
        if ("PAQUETE".equalsIgnoreCase(tv)) {
            return cantidadPaquetes * getUnidadesPorPaquete();
        } else if ("UNIDAD".equalsIgnoreCase(tv)) {
            return cantidadUnidades;
        } else {
            return (cantidadPaquetes * getUnidadesPorPaquete()) + cantidadUnidades;
        }
    }

    public int getOfertaId() {
        return ofertaId;
    }

    public String getNombreOferta() {
        return nombreOferta;
    }

    public int getCantidadOfertas() {
        return isOferta ? cantidadOfertas : 0;
    }

    public void setCantidadOfertas(int cantidadOfertas) {
        if (isOferta) this.cantidadOfertas = cantidadOfertas;
    }

    public double getPrecioFinalVisual() {
        return precioFinalVisual;
    }

    public double getSubtotal() {
        if (isOferta) {
            return cantidadOfertas * precioFinalVisual;
        } else {
            String tv = getTipoVenta();
            if ("PAQUETE".equalsIgnoreCase(tv)) {
                return cantidadPaquetes * precioVentaPaquete;
            } else if ("UNIDAD".equalsIgnoreCase(tv)) {
                return cantidadUnidades * precioVentaUnidad;
            } else {
                return (cantidadPaquetes * precioVentaPaquete) + (cantidadUnidades * precioVentaUnidad);
            }
        }
    }

    // Legacy getters for compatibility
    public int getPresentacionId() {
        return presentacionId;
    }

    public int getStock() {
        return stock;
    }

    public int getCantidad() {
        if (isOferta) return cantidadOfertas;
        String tv = getTipoVenta();
        if ("PAQUETE".equalsIgnoreCase(tv)) return cantidadPaquetes;
        if ("UNIDAD".equalsIgnoreCase(tv)) return cantidadUnidades;
        return cantidadPaquetes > 0 ? cantidadPaquetes : cantidadUnidades;
    }

    public void setCantidad(int cantidad) {
        if (isOferta) {
            this.cantidadOfertas = cantidad;
        } else {
            String tv = getTipoVenta();
            if ("PAQUETE".equalsIgnoreCase(tv)) {
                this.cantidadPaquetes = cantidad;
            } else if ("UNIDAD".equalsIgnoreCase(tv)) {
                this.cantidadUnidades = cantidad;
            } else {
                this.cantidadPaquetes = cantidad;
            }
        }
    }

    public String getPrecio() {
        if (isOferta) return String.valueOf(precioFinalVisual);
        return String.valueOf(precioVentaPaquete > 0 ? precioVentaPaquete : precioVentaUnidad);
    }

    public double getPrecioDouble() {
        if (isOferta) return precioFinalVisual;
        return precioVentaPaquete > 0 ? precioVentaPaquete : precioVentaUnidad;
    }
}
