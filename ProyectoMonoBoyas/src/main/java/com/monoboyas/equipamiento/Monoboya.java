package com.monoboyas.equipamiento;

import com.monoboyas.central.*;
import com.monoboyas.operaciones.*;
import com.monoboyas.sensores.*;
import com.monoboyas.sensores.Medicion.OrigenMedicion;

public class Monoboya {
    protected int id;
    protected Sensor[] sensores; // Arreglo de sensores con tam fijo
    protected Operacion operacion;
    protected int contadorSensores; // Para llevar el control de cuantos se han agregado
    protected Publisher publisher; // La Monoboya tiene un Publisher para enviar datos a la CentralDatos

    public enum EstadoMonoboya {
        OCUPADA,
        DISPONIBLE,
        DESHABILITADA
    }

    protected EstadoMonoboya estado;

    public Monoboya(int id, int capacidadMaxima, Operacion operacion, Publisher publisher) {
        this.id = id;
        this.sensores = new Sensor[capacidadMaxima];
        this.contadorSensores = 0;
        this.operacion = operacion;
        this.publisher = publisher;
        estado = EstadoMonoboya.DISPONIBLE;
    }

    public void asignarOperacion(Operacion operacion) {
        this.operacion = operacion;
    }

    public void recolectarYTransmitirDatos() {
        for (Sensor sensor : sensores) {
            if (sensor != null) {
                sensor.actualizarDato();
                Medicion nuevaMedicion = new Medicion(sensor.getId(), sensor.getValor(), sensor.getUnidad(),
                        sensor.getTipo(), OrigenMedicion.MONOBOYA, operacion.getId());
                if (sensor.getTipo() == Sensor.TipoSensor.CAUDAL) {
                    double litrosEnEsteCiclo = sensor.getValor();
                    operacion.getBuque().descontarCapacidad((int) Math.round(litrosEnEsteCiclo));
                }
                publisher.publicar(nuevaMedicion);
            }
        }
    }

    public void agregarSensor(Sensor sensor) {
        if (contadorSensores < sensores.length) {
            sensores[contadorSensores] = sensor;
            contadorSensores++;
        } else {
            System.err.println("Error: No hay más espacio para sensores en la monoboya " + this.id);
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public EstadoMonoboya getEstadoEnum() {
        return estado;
    }

    public Sensor[] getSensores() {
        return sensores;
    }

    public int getCantidadSensores() {
        return contadorSensores;
    }

    public Operacion getOperacion() {
        return operacion;
    }

}