package com.monoboyas.equipamiento;

import java.util.List;

import com.monoboyas.central.CentralDatos;
import com.monoboyas.operaciones.Operacion;
import com.monoboyas.persistencia.UsuarioDAO;
import com.monoboyas.usuarios.OperadorLancha;
import com.monoboyas.usuarios.OperadorPlanta;
import com.monoboyas.usuarios.Usuario;

public class Planta {
    protected String nombre;
    protected int idPlanta;
    protected final int capacidadMaximaMonoboyas = 3;
    protected int cantidadActualMonoboyas;
    protected OperadorPlanta[] operadorPlanta;
    protected CentralDatos centralDatos;
    private Monoboya[] monoboyas;

    public Planta(String nombre, int idPlanta, CentralDatos centralDatos) {
        this.nombre = nombre;
        this.idPlanta = idPlanta;
        cantidadActualMonoboyas = 0;
        this.monoboyas = new Monoboya[capacidadMaximaMonoboyas];
        this.centralDatos = centralDatos;

    }

    public void agregarMonoboya(Monoboya m) {
        if (m != null && cantidadActualMonoboyas < capacidadMaximaMonoboyas) {
            monoboyas[cantidadActualMonoboyas] = m;
            cantidadActualMonoboyas++;
        }
    }

    public void iniciarOperacion(Monoboya m, Operacion operacion) {
        if (m.getEstadoEnum() == Monoboya.EstadoMonoboya.DISPONIBLE && operacion != null) {
            m.estado = Monoboya.EstadoMonoboya.DESHABILITADA;
            m.asignarOperacion(operacion);
            // terminar
        }

    }

    public void recibirSolicitudTransferencia(Operacion operacion, UsuarioDAO usuarioDAO) {
        System.out.println("\n[PLANTA " + nombre + "] >>> RECIBIENDO SOLICITUD DE TRANSFERENCIA ");
        Monoboya m = obtenerMonoboyaDisponible();
        // Aca habria una logica para que espere en caso de que no haya monoboyas
        // disponibles
        // Pero por cuestiones de tiempo y complejidad no lo hicimos

        operacion.asignarMonoboya(m);
        m.asignarOperacion(operacion);

        System.out.println("\n[PLANTA] >>> MONOBOYA ASIGNADA " + m);

        OperadorLancha operadorLancha = obtenerOperadorLanchaDisponible(usuarioDAO);
        OperadorPlanta operadorPlanta = obtenerOperadorPlantaDisponible(usuarioDAO);

        operacion.asignarOperadorPlanta(operadorPlanta);
        operacion.asignarOperadorLancha(operadorLancha);

        operadorLancha.asignarOperacion(operacion);
        operadorPlanta.asignarOperacion(operacion);

        centralDatos.iniciarOperacion(operacion);

        operacion.iniciarOperacion();

    }

    public Monoboya obtenerMonoboyaDisponible() {
        for (int i = 0; i < cantidadActualMonoboyas; i++) {
            if (monoboyas[i].getEstadoEnum() == Monoboya.EstadoMonoboya.DISPONIBLE) {
                return monoboyas[i];
            }
        }
        return null; // No hay monoboyas disponibles
    }

    public CentralDatos getCentralDatos() {
        return centralDatos;
    }

    // ── Operador Lancha ───────────────────────────────────────
    public Usuario primerOperadorLanchaDisponible(List<Usuario> usuarios) {
        for (Usuario u : usuarios) {
            if (u.getOperacion() == null) {
                return u;
            }
        }
        return null;
    }

    public OperadorLancha obtenerOperadorLanchaDisponible(UsuarioDAO usuarioDAO) {
        List<Usuario> usuarios = usuarioDAO.listarPorRol("OPERADOR_LANCHA");
        Usuario disponible = primerOperadorLanchaDisponible(usuarios);
        return (OperadorLancha) disponible;
    }

    // ── Operador Planta ───────────────────────────────────────
    public Usuario primerOperadorPlantaDisponible(List<Usuario> usuarios) {
        for (Usuario u : usuarios) {
            if (u.getOperacion() == null) {
                return u;
            }
        }
        return null;
    }

    public OperadorPlanta obtenerOperadorPlantaDisponible(UsuarioDAO usuarioDAO) {
        List<Usuario> usuarios = usuarioDAO.listarPorRol("OPERADOR_PLANTA");
        Usuario disponible = primerOperadorPlantaDisponible(usuarios);
        return (OperadorPlanta) disponible;
    }

    public int getId() {
        return idPlanta;
    }

}