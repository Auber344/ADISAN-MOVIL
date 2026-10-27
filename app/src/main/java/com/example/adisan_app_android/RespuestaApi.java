package com.example.adisan_app_android;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Response;

/** Utilidades para leer las respuestas de ApiService en las pantallas. */
public final class RespuestaApi {

    private RespuestaApi() {
    }

    /**
     * Mensaje legible de una respuesta fallida: el "message" que envía FirebaseApiService
     * (por ejemplo "Stock insuficiente de ...") o el texto por defecto si no hay ninguno.
     */
    public static String mensajeError(Response<?> response, String porDefecto) {
        if (response != null && response.errorBody() != null) {
            try {
                JsonElement el = JsonParser.parseString(response.errorBody().string());
                if (el.isJsonObject()) {
                    JsonObject obj = el.getAsJsonObject();
                    if (obj.has("message") && !obj.get("message").isJsonNull()) {
                        String mensaje = obj.get("message").getAsString();
                        if (!mensaje.trim().isEmpty()) return mensaje;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return porDefecto;
    }
}
