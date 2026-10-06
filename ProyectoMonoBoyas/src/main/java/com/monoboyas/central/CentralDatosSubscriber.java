package com.monoboyas.central;
import org.springframework.stereotype.Component;

import com.monoboyas.sensores.Medicion;


@Component
public class CentralDatosSubscriber implements Subscriber {

    private CentralDatos centralDatos;

    public CentralDatosSubscriber(CentralDatos centralDatos, BrokerMQTT broker) {
        this.centralDatos = centralDatos;
        broker.suscribir(this);
    }

    @Override
    public void recibirMensaje(Medicion medicion) {
        System.out.println("[CENTRAL] Recibida medición -> Sensor:" + medicion.getIdSensor() + " Tipo:" + medicion.getTipo() + " Valor:" + medicion.getValor() + " " + medicion.getUnidad() + " Origen:" + medicion.getOrigen());
        centralDatos.procesarTelemetria(medicion);
    }
}