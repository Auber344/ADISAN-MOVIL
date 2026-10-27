package com.example.adisan_app_android;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class UsuarioAdapter extends RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder> {

    public interface OnUsuarioActionListener {
        void onEditar(Usuario usuario);
        void onResetPassword(Usuario usuario);
        void onEliminar(Usuario usuario);
    }

    private final Context context;
    private List<Usuario> usuarioList;
    private OnUsuarioActionListener listener;

    public UsuarioAdapter(Context context, List<Usuario> usuarioList) {
        this.context = context;
        this.usuarioList = usuarioList != null ? new ArrayList<>(usuarioList) : new ArrayList<>();
    }

    public void setOnUsuarioActionListener(OnUsuarioActionListener listener) {
        this.listener = listener;
    }

    public void setUsuarios(List<Usuario> usuarios) {
        this.usuarioList = usuarios != null ? new ArrayList<>(usuarios) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsuarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_usuario, parent, false);
        return new UsuarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsuarioViewHolder holder, int position) {
        Usuario usuario = usuarioList.get(position);

        holder.tvNombre.setText(usuario.getNombreCompleto());
        holder.tvUsuario.setText("Usuario: " + (!TextUtils.isEmpty(usuario.getUsuario()) ? usuario.getUsuario() : "-"));

        String rol = !TextUtils.isEmpty(usuario.getRol()) ? usuario.getRol().toUpperCase() : "CLIENTE";
        holder.tvRol.setText(rol);

        switch (rol.toLowerCase()) {
            case "admin":
            case "administrador":
                holder.tvRol.setTextColor(Color.parseColor("#60A5FA"));
                holder.tvRol.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#0D2247")));
                break;
            case "vendedor":
                holder.tvRol.setTextColor(Color.parseColor("#34D399"));
                holder.tvRol.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#062E29")));
                break;
            case "chofer":
                holder.tvRol.setTextColor(Color.parseColor("#2DD4BF"));
                holder.tvRol.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#042F2E")));
                break;
            default: // cliente
                holder.tvRol.setTextColor(Color.parseColor("#FBBF24"));
                holder.tvRol.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#3B2C0D")));
                break;
        }

        holder.tvDni.setText("DNI: " + (!TextUtils.isEmpty(usuario.getDni()) ? usuario.getDni() : "-"));
        holder.tvRuc.setText("RUC: " + (!TextUtils.isEmpty(usuario.getRuc()) ? usuario.getRuc() : "-"));

        String correo = !TextUtils.isEmpty(usuario.getCorreo()) ? usuario.getCorreo() : "-";
        String tel = !TextUtils.isEmpty(usuario.getTelefono()) ? usuario.getTelefono() : "-";
        holder.tvContacto.setText("Correo: " + correo + " • Tel: " + tel);

        String dir = !TextUtils.isEmpty(usuario.getDireccion()) ? usuario.getDireccion() : "Sin dirección registrada";
        holder.tvDireccion.setText("Dirección: " + dir);

        if (holder.btnEditar != null) {
            holder.btnEditar.setOnClickListener(v -> {
                if (listener != null) listener.onEditar(usuario);
            });
        }

        if (holder.btnResetPassword != null) {
            holder.btnResetPassword.setOnClickListener(v -> {
                if (listener != null) listener.onResetPassword(usuario);
            });
        }

        if (holder.btnEliminar != null) {
            holder.btnEliminar.setOnClickListener(v -> {
                if (listener != null) listener.onEliminar(usuario);
            });
        }
    }

    @Override
    public int getItemCount() {
        return usuarioList != null ? usuarioList.size() : 0;
    }

    static class UsuarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre;
        TextView tvUsuario;
        TextView tvRol;
        TextView tvDni;
        TextView tvRuc;
        TextView tvContacto;
        TextView tvDireccion;
        MaterialButton btnEditar;
        MaterialButton btnResetPassword;
        MaterialButton btnEliminar;

        public UsuarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreUsuario);
            tvUsuario = itemView.findViewById(R.id.tvUsuarioUsername);
            tvRol = itemView.findViewById(R.id.tvRolUsuario);
            tvDni = itemView.findViewById(R.id.tvDniUsuario);
            tvRuc = itemView.findViewById(R.id.tvRucUsuario);
            tvContacto = itemView.findViewById(R.id.tvContactoUsuario);
            tvDireccion = itemView.findViewById(R.id.tvDireccionUsuario);
            btnEditar = itemView.findViewById(R.id.btnEditarUsuario);
            btnResetPassword = itemView.findViewById(R.id.btnResetPasswordUsuario);
            btnEliminar = itemView.findViewById(R.id.btnEliminarUsuario);
        }
    }
}