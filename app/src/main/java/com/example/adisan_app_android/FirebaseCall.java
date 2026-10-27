package com.example.adisan_app_android;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Expone una operación de Firebase (Task) como un retrofit2.Call.
 * Así las pantallas siguen usando enqueue / Callback / Response igual que con el backend REST.
 *
 * Errores:
 *  - ErrorNegocio (stock insuficiente, datos inválidos...) -> Response.error(código, {"success":false,"message":...})
 *  - Permiso denegado en Firestore -> 403
 *  - Sin conexión -> Callback.onFailure
 */
public class FirebaseCall<T> implements Call<T> {

    public interface Operacion<T> {
        Task<T> ejecutar() throws Exception;
    }

    /** Error de negocio que llega a la pantalla como respuesta HTTP con mensaje legible. */
    public static class ErrorNegocio extends RuntimeException {
        final int codigo;

        public ErrorNegocio(int codigo, String mensaje) {
            super(mensaje);
            this.codigo = codigo;
        }
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final Operacion<T> operacion;
    private volatile boolean ejecutado;
    private volatile boolean cancelado;

    FirebaseCall(Operacion<T> operacion) {
        this.operacion = operacion;
    }

    @Override
    public void enqueue(@NonNull Callback<T> callback) {
        marcarEjecutado();
        iniciar().addOnCompleteListener(task -> {
            if (cancelado) return;
            if (task.isSuccessful()) {
                callback.onResponse(this, Response.success(task.getResult()));
                return;
            }
            Exception error = task.getException();
            Response<T> respuesta = respuestaDeError(error);
            if (respuesta != null) {
                callback.onResponse(this, respuesta);
            } else {
                callback.onFailure(this, error != null ? error : new IOException("Error de conexión"));
            }
        });
    }

    @NonNull
    @Override
    public Response<T> execute() throws IOException {
        marcarEjecutado();
        try {
            return Response.success(Tasks.await(iniciar()));
        } catch (Exception e) {
            Response<T> respuesta = respuestaDeError(e);
            if (respuesta != null) return respuesta;
            throw new IOException(e);
        }
    }

    private Task<T> iniciar() {
        try {
            return operacion.ejecutar();
        } catch (Exception e) {
            return Tasks.forException(e);
        }
    }

    private synchronized void marcarEjecutado() {
        if (ejecutado) throw new IllegalStateException("La llamada ya fue ejecutada");
        ejecutado = true;
    }

    /** Devuelve null cuando el error es de red y debe ir a onFailure. */
    private Response<T> respuestaDeError(Exception error) {
        Throwable causa = error;
        while (causa != null) {
            if (causa instanceof ErrorNegocio) {
                return respuestaError(((ErrorNegocio) causa).codigo, causa.getMessage());
            }
            if (causa instanceof FirebaseFirestoreException) {
                switch (((FirebaseFirestoreException) causa).getCode()) {
                    case UNAVAILABLE:
                    case DEADLINE_EXCEEDED:
                        return null;
                    case PERMISSION_DENIED:
                    case UNAUTHENTICATED:
                        return respuestaError(403, "No tienes permiso para realizar esta acción");
                    case NOT_FOUND:
                        return respuestaError(404, "El registro no existe");
                    default:
                        return respuestaError(500, "Error en la base de datos: " + causa.getMessage());
                }
            }
            if (causa instanceof FirebaseAuthException) {
                return respuestaError(400, mensajeAuth((FirebaseAuthException) causa));
            }
            if (causa instanceof FirebaseNetworkException) {
                return null;
            }
            if (causa instanceof FirebaseTooManyRequestsException) {
                return respuestaError(429, "Demasiados intentos. Espera unos minutos y vuelve a intentarlo.");
            }
            if (causa instanceof FirebaseException) {
                String detalle = causa.getMessage() != null ? causa.getMessage() : "";
                if (detalle.contains("CONFIGURATION_NOT_FOUND")) {
                    return respuestaError(503, "El inicio de sesión con correo aún no está habilitado en Firebase");
                }
                return respuestaError(500, "Error del servicio: " + detalle);
            }
            causa = causa.getCause();
        }
        return null;
    }

    static String mensajeAuth(FirebaseAuthException e) {
        if (e instanceof FirebaseAuthUserCollisionException) return "Ese correo ya tiene una cuenta registrada";
        if (e instanceof FirebaseAuthWeakPasswordException) return "La contraseña debe tener al menos 6 caracteres";
        if (e instanceof FirebaseAuthInvalidUserException) return "La cuenta no existe o fue deshabilitada";
        if (e instanceof FirebaseAuthInvalidCredentialsException) return "Correo o contraseña incorrectos";
        return "Error de autenticación: " + e.getMessage();
    }

    private Response<T> respuestaError(int codigo, String mensaje) {
        JsonObject cuerpo = new JsonObject();
        cuerpo.addProperty("success", false);
        cuerpo.addProperty("message", mensaje);
        return Response.error(codigo, ResponseBody.create(JSON, cuerpo.toString()));
    }

    @Override
    public boolean isExecuted() {
        return ejecutado;
    }

    @Override
    public void cancel() {
        cancelado = true;
    }

    @Override
    public boolean isCanceled() {
        return cancelado;
    }

    @NonNull
    @Override
    public Call<T> clone() {
        return new FirebaseCall<>(operacion);
    }

    @NonNull
    @Override
    public Request request() {
        return new Request.Builder().url("https://firestore.googleapis.com/").build();
    }

    @NonNull
    @Override
    public Timeout timeout() {
        return Timeout.NONE;
    }
}
