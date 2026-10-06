package com.monoboyas.sensores;

import java.time.LocalDateTime;

public class SensorDeAmarre extends Sensor {
    public SensorDeAmarre(int id, ISensorDataProvider provider) {
        super(id, TipoSensor.AMARRE, "kN", provider);
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
