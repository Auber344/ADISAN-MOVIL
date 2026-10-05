package com.example.adisan_app_android;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class CompraResponse {

    @SerializedName("success")
    private Object success;

    @SerializedName(value = "data", alternate = {"compras", "items", "results", "list", "rows"})
    private JsonElement dataObj;

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

    public List<Compra> getData() {
        List<Compra> lista = new ArrayList<>();
        if (dataObj == null) return lista;
        try {
            if (dataObj.isJsonArray()) {
                java.lang.reflect.Type listType = new TypeToken<List<Compra>>() {}.getType();
                lista = new Gson().fromJson(dataObj, listType);
            } else if (dataObj.isJsonObject()) {
                JsonObject obj = dataObj.getAsJsonObject();
                String[] keys = {"compras", "data", "items", "results", "list", "rows", "compra"};
                JsonElement targetEl = null;
                for (String k : keys) {
                    if (obj.has(k)) {
                        targetEl = obj.get(k);
                        break;
                    }
                }
                if (targetEl != null && targetEl.isJsonArray()) {
                    java.lang.reflect.Type listType = new TypeToken<List<Compra>>() {}.getType();
                    lista = new Gson().fromJson(targetEl, listType);
                } else if (targetEl != null && targetEl.isJsonObject()) {
                    Compra c = new Gson().fromJson(targetEl, Compra.class);
                    if (c != null) lista.add(c);
                } else if (obj.has("id") || obj.has("total") || obj.has("detalles")) {
                    Compra c = new Gson().fromJson(obj, Compra.class);
                    if (c != null) lista.add(c);
                }
            }
        } catch (Exception ignored) {}
        return lista;
    }

    public void setData(JsonElement dataObj) {
        this.dataObj = dataObj;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
