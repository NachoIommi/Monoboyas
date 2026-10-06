package com.monoboyas.alertas;

import java.time.LocalDateTime;

import com.monoboyas.operaciones.Operacion;

public class Alerta {

    protected int id;
    protected String mensaje;
    protected Operacion operacion;
    protected double double_medicion;
    protected String string_medicion;
    protected LocalDateTime timestamp;

    public enum TipoAlerta {
        VERDE,
        AMARILLA,
        ROJA
    }

    protected TipoAlerta tipoAlerta;

    public Alerta(int id, TipoAlerta tipoAlerta, String mensaje, Operacion operacion, double valor_medicion,
            String string_medicion) {
        this.id = id;
        this.tipoAlerta = tipoAlerta;
        this.operacion = operacion;
        this.double_medicion = valor_medicion;
        this.string_medicion = string_medicion;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public TipoAlerta getTipoAlerta() {
        return tipoAlerta;
    }

    public void setTipoAlerta(TipoAlerta tipoAlerta) {
        this.tipoAlerta = tipoAlerta;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Operacion getOperacion() {
        return operacion;
    }

    public void setOperacion(Operacion operacion) {
        this.operacion = operacion;
    }

    public double getDouble_medicion() {
        return double_medicion;
    }

    public void setDouble_medicion(double double_medicion) {
        this.double_medicion = double_medicion;
    }

    public String getString_medicion() {
        return string_medicion;
    }

    public void setString_medicion(String string_medicion) {
        this.string_medicion = string_medicion;
    }

}