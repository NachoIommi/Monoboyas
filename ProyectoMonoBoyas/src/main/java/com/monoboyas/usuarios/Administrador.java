package com.monoboyas.usuarios;

import java.util.List;

import com.monoboyas.equipamiento.Buque;
import com.monoboyas.equipamiento.Planta;
import com.monoboyas.operaciones.Operacion;
import com.monoboyas.persistencia.UsuarioDAO;

public class Administrador extends Usuario {

    protected Usuario usuario; // Referencia al usuario que se está gestionando (para agregar, eliminar o
                               // modificar)
    protected Planta planta; // Referencia a la planta que opera

    public Administrador(int id, String nombre, String contrasena, int dni, Planta planta) {
        super(id, nombre, contrasena, dni);
        this.planta = planta;
        rol = "ADMIN";
    }

    public String getRol() {
        return rol;
    }

    public void PlanificarOperacion(Buque barco, Planta planta, UsuarioDAO usuarioDAO) {
        OperadorBuque operadorBuque = obtenerOperadorBuqueDisponible(usuarioDAO);
        Operacion operacion = new Operacion((int) (Math.random() * 100), barco, operadorBuque, planta);
        barco.setOperacion(operacion);
        operadorBuque.asignarOperacion(operacion);

    }

    public Usuario primerOperadorBuqueDisponible(List<Usuario> usuarios) {
        for (Usuario u : usuarios) {
            if (u.getOperacion() == null) {
                return u;
            }
        }
        return null;
    }

    public OperadorBuque obtenerOperadorBuqueDisponible(UsuarioDAO usuarioDAO) {
        List<Usuario> usuarios = usuarioDAO.listarPorRol("OPERADOR_BUQUE");
        Usuario disponible = primerOperadorBuqueDisponible(usuarios);
        return (OperadorBuque) disponible;
    }

}
