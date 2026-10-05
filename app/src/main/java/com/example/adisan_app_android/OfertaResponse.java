package com.example.adisan_app_android;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OfertaResponse {

    private boolean success;

    @SerializedName(value = "data", alternate = {"ofertas", "items", "results"})
    private List<Oferta> data;

    private String message;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<Oferta> getData() {
        return data;
    }

    public void setData(List<Oferta> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
