package com.monoboyas.usuarios;

import java.util.ArrayList;
import java.util.List;

import com.monoboyas.alertas.Alerta;
import com.monoboyas.equipamiento.Planta;
import com.monoboyas.operaciones.*;

public class OperadorLancha extends UsuarioOperador {

    protected int id;
    protected String nombre;
    protected String contrasena;
    protected int dni;
    protected List<Alerta> alertasRecibidas; // Lista para almacenar las alertas recibidas por el usuario
    protected String rol;
    protected Operacion operacion; // Referencia a la operación que el operador está manejando


    public OperadorLancha(int id, String nombre, String contrasena, int dni) {
        super(id, nombre, contrasena, dni);
        this.alertasRecibidas = new ArrayList<>();
        rol = "OPERADOR_LANCHA";
    }

    public String getRol() {
        return rol;
    }

    // Específico de la lancha: dispara el inicio. La lógica real vive en Operacion (compa).
    public void iniciarOperacion() {
        if (operacion == null) {
            System.out.println("  OperadorLancha (" + nombre + "): sin operación asignada.");
            return;
        }
        if(operacion.getTipoOperacion() == Operacion.TipoOperacion.PREPARADA){
            operacion.iniciarOperacion();
        }

        System.out.println(" OperadorLancha (" + nombre + ") inició la operación " + operacion.getId() + ".");
    }

    public void asignarOperacion(Operacion op){
        operacion = op;
    }

    public void reconocerAlerta( ) {
        System.out.println("\n[OPERADOR DE LANCHA] >>> ALERTA RECONOCIDA. Tomando medidas de seguridad.");
    }
    public void recibirAlerta(Alerta alerta) {
        alertasRecibidas.add(alerta);
        System.out.println("\n[OPERADOR DE LANCHA] >>> ALERTA RECIBIDA: " + alerta.getMensaje() + " (Tipo: " + alerta.getTipoAlerta() + ")");
    }
}