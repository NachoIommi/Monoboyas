package com.monoboyas.usuarios;

import java.util.ArrayList;
import java.util.List;

import com.monoboyas.alertas.Alerta;
import com.monoboyas.operaciones.Operacion;

public class OperadorBuque extends UsuarioOperador {

    protected List<Alerta> alertasRecibidas; // Lista para almacenar las alertas recibidas por el usuario
    protected String rol;
    protected Operacion operacion;

    public OperadorBuque(int id, String nombre, String contrasena, int dni) {
        super(id, nombre, contrasena, dni);
        this.alertasRecibidas = new ArrayList<>();
        rol = "OPERADOR_BUQUE";
    }

    public String getRol() {
        return rol;
    }

    @Override
    public void reconocerAlerta( ) {
        System.out.println("\n[OPERADOR DE BUQUE] >>> ALERTA RECONOCIDA. Tomando medidas de seguridad.");
    }
    public void recibirAlerta(Alerta alerta) {
        alertasRecibidas.add(alerta);
        System.out.println("\n[OPERADOR DE BUQUE] >>> ALERTA RECIBIDA: " + alerta.getMensaje() + " (Tipo: " + alerta.getTipoAlerta() + ")");
    }

    public void asignarOperacion(Operacion operacion) {
        this.operacion = operacion;
    }

}