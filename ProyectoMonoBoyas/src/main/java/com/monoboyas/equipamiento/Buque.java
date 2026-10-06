package com.monoboyas.equipamiento;

import com.monoboyas.central.CentralDatos;
import com.monoboyas.central.Publisher;
import com.monoboyas.operaciones.Operacion;
import com.monoboyas.persistencia.UsuarioDAO;
import com.monoboyas.sensores.Medicion;
import com.monoboyas.sensores.Medicion.OrigenMedicion;
import com.monoboyas.sensores.Sensor;

public class Buque {

    protected int nroIMO;
    protected int capacidad;
    protected int capacidadRestante;
    protected String nombre;
    protected Sensor transmisorPresion;
    private Publisher publisher;
    protected CentralDatos centralDatos;
    protected Operacion operacion;

    public Buque(int nroIMO, int capacidad, Sensor sensorPresion, String nombre, Publisher publisher,
            Operacion operacion) {
        this.nroIMO = nroIMO;
        this.capacidad = capacidad;
        this.transmisorPresion = sensorPresion;
        capacidadRestante = capacidad;
        this.nombre = nombre;
        this.publisher = publisher;
        this.operacion = operacion;
        this.centralDatos = centralDatos;
    }

    public Buque(int nroIMO, int capacidad, Sensor sensorPresion, String nombre, Publisher publisher) {
        this(nroIMO, capacidad, sensorPresion, nombre, publisher, null);
    }

    public void recolectarYTransmitirDatos() {
        Medicion nuevaMedicion = new Medicion(transmisorPresion.getId(), transmisorPresion.getValor(),
                transmisorPresion.getUnidad(), transmisorPresion.getTipo(), OrigenMedicion.BUQUE, operacion.getId());
        publisher.publicar(nuevaMedicion);
    }

    public void solitudTransferencia(UsuarioDAO usuarioDAO) {
        System.out.println("\n[BUQUE " + nombre + "] >>> SOLICITANDO TRANSFERENCIA DE CARGA...");
        operacion.getPlanta().recibirSolicitudTransferencia(operacion, usuarioDAO);
    }

    public int getNroIMO() {
        return nroIMO;
    }

    public void descontarCapacidad(double litrosTransferidos) {
        System.out.println("");
        System.out.print("soy el buque llamada a restar");
        System.out.println("");
        int litrosEnteros = (int) Math.round(litrosTransferidos); // trunca el decimal, ej: 142.7 -> 142
        capacidadRestante = capacidadRestante - litrosEnteros;

        System.out.println("");
        System.out.print("mi capacidad es " + capacidadRestante);
        System.out.println("");
        if (capacidadRestante < 0) {
            ;
        }
    }

    public int getCapacidadRestante() {
        return capacidadRestante;
    }

    public void setNroIMO(int nroIMO) {
        this.nroIMO = nroIMO;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Sensor getTransmisorPresion() {
        return transmisorPresion;
    }

    public void setTransmisorPresion(Sensor transmisorPresion) {
        this.transmisorPresion = transmisorPresion;
    }

    public void setOperacion(Operacion operacion) {
        this.operacion = operacion;
    }

    public boolean finalizoDescarga() {
        if (capacidadRestante <= 0) {
            return true;
        } else
            return false;
    }

    public CentralDatos getCentralDatos() {
        return centralDatos;
    }

}
