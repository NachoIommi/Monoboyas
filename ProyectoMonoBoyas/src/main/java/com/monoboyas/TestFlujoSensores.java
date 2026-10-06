/*package com.monoboyas;

import com.monoboyas.equipamiento.Buque;
import com.monoboyas.equipamiento.Monoboya;
import com.monoboyas.operaciones.Operacion;
import com.monoboyas.sensores.*;
import com.monoboyas.central.BrokerMQTT;
import com.monoboyas.central.CentralDatos;
import com.monoboyas.central.CentralDatosSubscriber;
import com.monoboyas.usuarios.OperadorLancha;
import com.monoboyas.usuarios.OperadorBuque;
import com.monoboyas.usuarios.OperadorPlanta;

public class TestFlujoSensores {

    public static void main(String[] args) throws InterruptedException {

        CentralDatos centralEnPlanta = new CentralDatos();
        BrokerMQTT broker = new BrokerMQTT();
        broker.suscribir(new CentralDatosSubscriber(centralEnPlanta));
        Monoboya monoboya = new Monoboya(101, 8, null, broker);

        // Crear operadores ANTES de la operación (el constructor los necesita)
        OperadorLancha juanLancha = new OperadorLancha(1, "Juan (Lancha)", "123", 1111);
        OperadorBuque capitanBuque = new OperadorBuque(2, "Capitán Smith (Buque)", "456", 2222);
        OperadorPlanta pedroPlanta = new OperadorPlanta(3, "Pedro (Sala Control)", "789", 3333);

        // Crear buque
        Buque buque = new Buque(9876543, 50000, "MT Concordia", null, null);

        // Crear la operación con el nuevo constructor
        Operacion opTransferencia = new Operacion(5001, buque, capitanBuque, null);

        opTransferencia.asignarMonoboya(monoboya);
        monoboya.asignarOperacion(opTransferencia);
        opTransferencia.asignarOperadorLancha(juanLancha);
        opTransferencia.asignarOperadorPlanta(pedroPlanta);
        opTransferencia.iniciarOperacion();

        SensorDePresion presion = new SensorDePresion(1, new ArchivoDataProvider("presion.txt"));
        SensorDeOleaje oleaje = new SensorDeOleaje(2, new ApiOleajeProvider());
        SensorDeTension tension = new SensorDeTension(3, new ArchivoDataProvider("tension.txt"));
        Anemometro anemometro = new Anemometro(4, new ApiVientoProvider());
        Correntometro correntometro = new Correntometro(5, new ApiCorrienteProvider());
        Caudalimetro caudalimetro = new Caudalimetro(6, new ArchivoDataProvider("caudal.txt"));
        Giroscopio giroscopio = new Giroscopio(7, new ArchivoDataProvider("giroscopio.txt"));
        SensorDeAmarre amarre = new SensorDeAmarre(8, new ArchivoDataProvider("amarre.txt"));

        monoboya.agregarSensor(presion);
        monoboya.agregarSensor(oleaje);
        monoboya.agregarSensor(tension);
        monoboya.agregarSensor(anemometro);
        monoboya.agregarSensor(correntometro);
        monoboya.agregarSensor(caudalimetro);
        monoboya.agregarSensor(giroscopio);
        monoboya.agregarSensor(amarre);

        System.out.println("--- INICIANDO DESCARGA DE PETRÓLEO ---");

        int segundosTranscurridos = 0;
        int tiempoSimuladoDescarga = 10;

        while (opTransferencia.isActiva() && segundosTranscurridos < tiempoSimuladoDescarga) {
            System.out.println("\n--- Segundo " + (segundosTranscurridos + 1) + " ---");
            monoboya.recolectarYTransmitirDatos();
            Thread.sleep(1000);
            segundosTranscurridos++;
        }

        if (opTransferencia.isActiva()) {
            System.out.println(
                    "\n[OPERADOR] Tiempo de descarga completado sin incidentes críticos. Finalizando manualmente.");
            opTransferencia.finalizarOperacion();
        } else {
            System.out.println(
                    "\n[SISTEMA] El ciclo de muestreo se detuvo de forma anticipada debido a protocolos de seguridad.");
        }

        System.out.println("--- EL BARCO SE HA DESCONECTADO ---");
    }
} */