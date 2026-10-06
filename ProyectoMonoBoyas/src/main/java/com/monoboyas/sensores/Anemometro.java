package com.monoboyas.sensores;

import java.time.LocalDateTime;

public class Anemometro extends Sensor {
    public Anemometro(int id, ISensorDataProvider provider) {
        super(id, TipoSensor.VIENTO, "km/h", provider);
    }

    // GETTERS
    @Override
    public int getId() {
        return id;
    }

    @Override
    public TipoSensor getTipo() {
        return tipo;
    }

    @Override
    public String getUnidad() {
        return unidad;
    }

    @Override
    public boolean isActivo() {
        return activo;
    }

    @Override
    public double getValor() {
        return valor;
    }

    @Override
    public LocalDateTime getUltimaMedicion() {
        return ultimaMedicion;
    }
}