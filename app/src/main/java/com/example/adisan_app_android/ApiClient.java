package com.example.adisan_app_android;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "http://10.0.2.2:3000/";
    private static final String PREFS_NAME = "AdisanPrefs";
    private static final String KEY_TOKEN = "token";

    private static Retrofit retrofit;

    public static ApiService getApiService() {
        if (retrofit == null) {

            // Interceptor de OkHttp para adjuntar el JWT automáticamente
            Interceptor authInterceptor = new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request originalRequest = chain.request();
                    Request.Builder builder = originalRequest.newBuilder();

                    // Obtener token desde SharedPreferences ("AdisanPrefs" -> "token")
                    Context context = getApplicationContext();
                    if (context != null) {
                        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                        String token = preferences.getString(KEY_TOKEN, null);

                        if (!TextUtils.isEmpty(token)) {
                            builder.header("Authorization", "Bearer " + token.trim());
                        }
                    }

                    Request newRequest = builder.build();
                    return chain.proceed(newRequest);
                }
            };

            // Cliente OkHttp con el interceptor configurado
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();

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
                        return "0.00";
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
                    return "0.00";
                }
            };

            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .registerTypeAdapter(String.class, stringTypeAdapter)
                    .create();

            // Construir la instancia de Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }

        return retrofit.create(ApiService.class);
    }

    /**
     * Obtiene el Context global de la aplicación para acceder a SharedPreferences
     * sin modificar firmas de métodos ni otros archivos del proyecto.
     */
    private static Context getApplicationContext() {
        try {
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
            Object app = activityThreadClass.getMethod("getApplication").invoke(activityThread);
            return (Context) app;
        } catch (Exception e) {
            return null;
        }
    }
}
