package com.example.adisan_app_android;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Locale;

/**
 * Punto de acceso a los datos de la app.
 * Antes construía Retrofit contra el backend Node; ahora devuelve FirebaseApiService,
 * que implementa la misma interfaz ApiService sobre Firebase Auth + Firestore.
 */
public class ApiClient {

    private static ApiService apiService;
    private static Gson gson;

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            apiService = new FirebaseApiService();
        }
        return apiService;
    }

    /**
     * Gson compartido para convertir documentos de Firestore a los modelos.
     * Los campos String que reciben un número (precios) se formatean con 2 decimales.
     */
    static synchronized Gson gson() {
        if (gson == null) {
            TypeAdapter<String> stringTypeAdapter = new TypeAdapter<String>() {
                @Override
                public void write(JsonWriter out, String value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        out.value(value);
                    }
                }

                @Override
                public String read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }
                    if (in.peek() == JsonToken.STRING) {
                        return in.nextString();
                    }
                    if (in.peek() == JsonToken.NUMBER) {
                        double d = in.nextDouble();
                        return String.format(Locale.US, "%.2f", d);
                    }
                    if (in.peek() == JsonToken.BOOLEAN) {
                        return String.valueOf(in.nextBoolean());
                    }
                    in.skipValue();
                    return null;
                }
            };

            gson = new GsonBuilder()
                    .serializeNulls()
                    .registerTypeAdapter(String.class, stringTypeAdapter)
                    .create();
        }
        return gson;
    }
}
