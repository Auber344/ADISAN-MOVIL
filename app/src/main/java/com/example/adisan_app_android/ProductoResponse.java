package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public class ProductoResponse {

    @SerializedName("success")
    private Object success;

    @SerializedName(value = "data", alternate = {"producto", "item", "result", "productos", "items", "results", "list", "productosList", "rows"})
    private List<Producto> data;

    @SerializedName("total")
    private int total;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("currentPage")
    private int currentPage;

    private String message;

    public boolean isSuccess() {
        if (success instanceof Boolean) {
            return (Boolean) success;
        } else if (success instanceof Number) {
            return ((Number) success).intValue() != 0;
        } else if (success instanceof String) {
            return Boolean.parseBoolean((String) success);
        }
        return true;
    }

    public void setSuccess(Object success) {
        this.success = success;
    }

    public List<Producto> getData() {
        return data != null ? data : Collections.emptyList();
    }

    public void setData(List<Producto> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }
}
