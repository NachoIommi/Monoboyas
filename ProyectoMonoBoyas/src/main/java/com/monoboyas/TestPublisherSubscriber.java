/* package com.monoboyas;

import com.monoboyas.central.BrokerMQTT;
import com.monoboyas.central.CentralDatos;
import com.monoboyas.central.CentralDatosSubscriber;
import com.monoboyas.equipamiento.Monoboya;
import com.monoboyas.sensores.SensorDeOleaje;
import com.monoboyas.sensores.SensorDePresion;
import com.monoboyas.sensores.MockSensorDataProvider;

public class TestPublisherSubscriber {
    public static void main(String[] args) {
        CentralDatos central = new CentralDatos();
        BrokerMQTT broker = new BrokerMQTT();
        broker.suscribir(new CentralDatosSubscriber(central));

        Monoboya monoboya = new Monoboya(101, 2, null, broker);
        monoboya.agregarSensor(new SensorDePresion(1, new MockSensorDataProvider())); 
        monoboya.agregarSensor(new SensorDeOleaje(2, new MockSensorDataProvider()));

        monoboya.recolectarYTransmitirDatos();
        //lo testie y funciono flama
    }
} */