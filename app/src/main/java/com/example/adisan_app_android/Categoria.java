package com.example.adisan_app_android;

import androidx.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

public class Categoria {

    @SerializedName("id")
    private int id;

    @SerializedName(value = "nombre", alternate = {"categoria_nombre", "name"})
    private String nombre;

    @SerializedName("factor")
    private int factor = 1;

    public Categoria() {}

    public Categoria(String nombre) {
        this.nombre = nombre;
    }

    public Categoria(String nombre, int factor) {
        this.nombre = nombre;
        this.factor = factor;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre != null ? nombre : "";
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getFactor() {
        return factor;
    }

    public void setFactor(int factor) {
        this.factor = factor;
    }

    @NonNull
    @Override
    public String toString() {
        return getNombre();
    }
}