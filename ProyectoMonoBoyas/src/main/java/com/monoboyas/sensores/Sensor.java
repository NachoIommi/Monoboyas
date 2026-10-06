package com.monoboyas.sensores;

import java.time.LocalDateTime;

public abstract class Sensor {
    protected int id;
    protected TipoSensor tipo;
    protected String unidad;
    protected boolean activo;
    protected LocalDateTime ultimaMedicion;

    protected double valor;
    protected ISensorDataProvider dataProvider;

    public enum TipoSensor {
        TENSION,        // SensorDeTension - tf
        PRESION,        // SensorDePresion - Pa
        OLEAJE,         // SensorDeOleaje - m
        ORIENTACION,    // Giroscopio - grados
        CORRIENTE,      // Correntometro - m/s
        CAUDAL,         // Caudalimetro - l/s
        VIENTO,         // Anemometro - km/h
        AMARRE          // SensorDeAmarre - kN
    }

    public Sensor(int id, TipoSensor tipo, String unidad, ISensorDataProvider dataProvider) {
        this.id = id;
        this.tipo = tipo;
        this.unidad = unidad;
        this.dataProvider = dataProvider;
        this.activo = true;
        this.valor = 0.0;
    }

    public TipoSensor getTipo() {
        return tipo;
    }

    //En lugar de generar un objeto, simplemente actualiza su estado interno
    public void actualizarDato() {
        if (this.activo && this.dataProvider != null) {
            System.out.println("[SENSOR] " + tipo + " (ID=" + id + ") solicitando dato al proveedor " );
            try {
                this.valor = dataProvider.obtenerDato();
                this.ultimaMedicion = LocalDateTime.now();
                System.out.println("[SENSOR] " + tipo + " (ID=" + id + ") obtuvo valor=" + valor + " " + unidad + " a las " + ultimaMedicion);
            } catch (Exception e) {
                System.err.println("[SENSOR] " + tipo + " (ID=" + id + ") error al leer dato: " + e.getMessage());
                // Mantenemos el último valor conocido o manejamos el error según el negocio
            }
        }
    }

    // --------------------------------------------------------
    // GETTERS IMPLEMENTADOS
    // --------------------------------------------------------
    public int getId() { return id; }
    public String getUnidad() { return unidad; }
    public boolean isActivo() { return activo; }
    public double getValor() { return valor; }
    public LocalDateTime getUltimaMedicion() { return ultimaMedicion; }
}