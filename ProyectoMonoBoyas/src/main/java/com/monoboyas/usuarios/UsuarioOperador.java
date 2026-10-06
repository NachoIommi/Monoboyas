package com.monoboyas.usuarios;

import java.util.ArrayList;

import com.monoboyas.alertas.Alerta;
import com.monoboyas.operaciones.Operacion;

public abstract class UsuarioOperador extends Usuario {
    protected Operacion operacion;

    public UsuarioOperador(int id, String nombre, String contrasena, int dni) {
        super(id, nombre, contrasena, dni);
        this.operacion = null; // Inicialmente sin operación asignada
        this.alertasRecibidas = new ArrayList<>();
        rol = "OPERADOR";
    }

    public String getRol() {
        return rol;
    }

    public abstract void reconocerAlerta();

    public abstract void recibirAlerta(Alerta alerta);

    // Métodos específicos para UsuarioOperador
    @Override
    public Operacion getOperacion() {
        return operacion;
    }

}
