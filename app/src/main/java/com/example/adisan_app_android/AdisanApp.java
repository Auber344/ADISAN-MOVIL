package com.example.adisan_app_android;

import android.app.Activity;
import android.app.Application;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Con targetSdk 35+ Android dibuja las pantallas detrás de la barra de estado y de navegación.
 * Este callback deja el contenido de cada pantalla dentro de la zona visible (y por encima del
 * teclado), pinta las franjas de las barras con el color de fondo de la pantalla y elige íconos
 * claros u oscuros según ese color, porque la app mezcla pantallas oscuras y claras.
 */
public class AdisanApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.FIREBASE_EMULADOR) {
            // 10.0.2.2 es la PC vista desde el emulador de Android
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099);
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
        }
        registerActivityLifecycleCallbacks(new BarrasDelSistema());
    }

    private static class BarrasDelSistema implements ActivityLifecycleCallbacks {

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
            View contenido = activity.findViewById(android.R.id.content);
            if (contenido == null || contenido.getTag(R.id.tag_margenes_sistema) != null) return;
            contenido.setTag(R.id.tag_margenes_sistema, Boolean.TRUE);

            Window window = activity.getWindow();
            Integer fondo = colorDeFondo(contenido, 3);
            if (fondo != null) {
                window.getDecorView().setBackgroundColor(fondo);
                window.setStatusBarColor(Color.TRANSPARENT);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                    window.setNavigationBarColor(fondo);
                }
                boolean fondoClaro = ColorUtils.calculateLuminance(fondo) > 0.5;
                WindowInsetsControllerCompat barras = WindowCompat.getInsetsController(window, window.getDecorView());
                barras.setAppearanceLightStatusBars(fondoClaro);
                barras.setAppearanceLightNavigationBars(fondoClaro);
            }

            // Estas dos pantallas ya ajustan su cabecera a la barra de estado
            if (activity instanceof ProductosActivity || activity instanceof PedidosActivity) return;

            ViewCompat.setOnApplyWindowInsetsListener(contenido, (v, insets) -> {
                Insets sistema = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
                Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
                v.setPadding(sistema.left, sistema.top, sistema.right, Math.max(sistema.bottom, teclado.bottom));
                return WindowInsetsCompat.CONSUMED;
            });
            ViewCompat.requestApplyInsets(contenido);
        }

        /** Primer color sólido de fondo en la raíz del layout (o en sus primeros hijos). */
        @Nullable
        private static Integer colorDeFondo(View vista, int profundidad) {
            Drawable fondo = vista.getBackground();
            if (fondo instanceof ColorDrawable && Color.alpha(((ColorDrawable) fondo).getColor()) == 255) {
                return ((ColorDrawable) fondo).getColor();
            }
            if (profundidad > 0 && vista instanceof ViewGroup && ((ViewGroup) vista).getChildCount() > 0) {
                return colorDeFondo(((ViewGroup) vista).getChildAt(0), profundidad - 1);
            }
            return null;
        }

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    }
}
