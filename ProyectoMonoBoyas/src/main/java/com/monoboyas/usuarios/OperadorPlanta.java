package com.monoboyas.usuarios;

import java.util.ArrayList;
import java.util.List;

import com.monoboyas.alertas.Alerta;
import com.monoboyas.equipamiento.Planta;
import com.monoboyas.operaciones.Operacion;

public class OperadorPlanta extends UsuarioOperador {

    protected int id;
    protected String nombre;
    protected String contrasena;
    protected int dni;
    protected List<Alerta> alertasRecibidas; // Lista para almacenar las alertas recibidas por el usuario
    protected String rol;
    protected Planta planta; // Referencia a la planta que opera
    protected Operacion operacion; // Referencia a la operación que el operador está manejando

    public OperadorPlanta(int id, String nombre, String contrasena, int dni) {
        super(id, nombre, contrasena, dni);
        this.alertasRecibidas = new ArrayList<>();
        rol = "OPERADOR_PLANTA";
    }

    public String getRol() {
    return rol;
    }

    public void reconocerAlerta() {
        System.out.println("\n[OPERADOR DE PLANTA] >>> ALERTA RECONOCIDA. Tomando medidas de seguridad.");
    }

    public void asignarOperacion(Operacion op){
        operacion=op;
    }
    public void recibirAlerta(Alerta alerta) {
        alertasRecibidas.add(alerta);
        System.out.println("\n[OPERADOR DE PLANTA] >>> ALERTA RECIBIDA: " + alerta.getMensaje() + " (Tipo: " + alerta.getTipoAlerta() + ")");
    }
}