package com.monoboyas;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.monoboyas.central.BrokerMQTT;
import com.monoboyas.central.CentralDatos;
import com.monoboyas.central.CentralDatosSubscriber;
import com.monoboyas.equipamiento.Buque;
import com.monoboyas.equipamiento.Monoboya;
import com.monoboyas.equipamiento.Planta;
import com.monoboyas.persistencia.AlertaDAO;
import com.monoboyas.persistencia.BuqueDAO;
import com.monoboyas.persistencia.MedicionDAO;
import com.monoboyas.persistencia.MonoboyaDAO;
import com.monoboyas.persistencia.OperacionDAO;
import com.monoboyas.persistencia.PlantaDAO;
import com.monoboyas.persistencia.SensorDAO;
import com.monoboyas.persistencia.UsuarioAlertaDAO;
import com.monoboyas.persistencia.UsuarioDAO;
import com.monoboyas.sensores.Anemometro;
import com.monoboyas.sensores.ApiCorrienteProvider;
import com.monoboyas.sensores.ApiOleajeProvider;
import com.monoboyas.sensores.ApiVientoProvider;
import com.monoboyas.sensores.ArchivoDataProvider;
import com.monoboyas.sensores.Caudalimetro;
import com.monoboyas.sensores.Correntometro;
import com.monoboyas.sensores.Giroscopio;
import com.monoboyas.sensores.SensorDeAmarre;
import com.monoboyas.sensores.SensorDeOleaje;
import com.monoboyas.sensores.SensorDePresion;
import com.monoboyas.sensores.SensorDeTension;
import com.monoboyas.usuarios.Administrador;
import com.monoboyas.usuarios.OperadorBuque;

@SpringBootApplication
@ComponentScan("com.monoboyas")
public class TestTelemetria implements CommandLineRunner {

    @Autowired
    private PlantaDAO plantaDAO;
    @Autowired
    private BuqueDAO buqueDAO;
    @Autowired
    private MonoboyaDAO monoboyaDAO;
    @Autowired
    private UsuarioDAO usuarioDAO;
    @Autowired
    private SensorDAO sensorDAO;
    @Autowired
    private OperacionDAO operacionDAO;
    @Autowired
    private CentralDatos centralDatos;
    @Autowired
    private MedicionDAO medicionDAO;
    @Autowired
    private AlertaDAO alertaDAO;
    @Autowired
    private UsuarioAlertaDAO usuarioAlertaDAO;
    @Autowired
    private BrokerMQTT broker;

    public static void main(String[] args) {
        SpringApplication.run(TestTelemetria.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Test: pipeline completo de telemetría (Amarre crítico) ===");

        int plantaId = 501;
        int buqueNroIMO = 5551234;
        int monoboyaId = 505;
        int sensorId = 501;
        int operadorBuqueId = 501;
        int operadorLanchaId = 502;
        int operadorPlantaId = 503;

        BrokerMQTT broker = new BrokerMQTT();
        CentralDatos centralEnPlanta = new CentralDatos(medicionDAO, alertaDAO, operacionDAO, usuarioAlertaDAO);
        broker.suscribir(new CentralDatosSubscriber(centralEnPlanta, broker));

        Planta planta = new Planta("Planta Test Telemetria", plantaId, centralEnPlanta);
        plantaDAO.guardar(plantaId, "Planta Test Telemetria");

        Administrador admin = new Administrador(1, "Admin Test", "adminpass", 12345678, planta);
        SensorDePresion presion2 = new SensorDePresion(22, new ArchivoDataProvider("presion.txt"));
        Buque buque = new Buque(buqueNroIMO, 10000,presion2 ,"Buque Test", broker);
        buqueDAO.guardar(buque);
        sensorDAO.guardar(presion2, monoboyaId);

        Monoboya monoboya = new Monoboya(monoboyaId, 8, null, broker);
        planta.agregarMonoboya(monoboya);
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
        monoboyaDAO.guardar(monoboya);
        sensorDAO.guardar(presion, monoboyaId);
        sensorDAO.guardar(oleaje, monoboyaId);
        sensorDAO.guardar(tension, monoboyaId);
        sensorDAO.guardar(anemometro, monoboyaId);
        sensorDAO.guardar(correntometro, monoboyaId);
        sensorDAO.guardar(caudalimetro, monoboyaId);
        sensorDAO.guardar(giroscopio, monoboyaId);
        sensorDAO.guardar(amarre, monoboyaId);

        OperadorBuque opBuque = new OperadorBuque(operadorBuqueId, "Capitán Test", "1234", 111222111);
        admin.PlanificarOperacion(buque, planta, usuarioDAO);
        buque.solitudTransferencia(usuarioDAO);

        // OperadorLancha opLancha = new OperadorLancha(operadorLanchaId, "Lanchero
        // Test", "1234", 22232222);
        // OperadorPlanta opPlanta = new OperadorPlanta(operadorPlantaId, "Operador
        // Planta Test", "1234", 333433);
        // usuarioDAO.guardar(opLancha);
        // usuarioDAO.guardar(opPlanta);

        /*
         * // Operación: en BD y en dominio, con el MISMO id
         * int operacionId = operacionDAO.crearPlanificada(buqueNroIMO, plantaId,
         * "DESCARGA", operadorBuqueId);
         * operacionDAO.actualizarParaPreparar(operacionId, monoboyaId,
         * operadorPlantaId, operadorLanchaId);
         * System.out.println("✔ Operación creada en BD: ID=" + operacionId);
         * 
         * Operacion operacionDominio = new Operacion(operacionId, buque, opBuque,
         * null);
         * 
         * operacionDominio.asignarMonoboya(monoboya);
         * //operacionDominio.asignarOperadorLancha(opLancha);
         * //operacionDominio.asignarOperadorPlanta(opPlanta);
         * 
         * centralDatos.iniciarOperacion(operacionDominio);
         * System.out.
         * println("✔ Operación registrada en CentralDatos.operacionesActivas");
         * 
         * // Medición que supera AMARRE_ROJA (900 kN)
         * Medicion medicion = new Medicion(sensorId, 950.0, "kN",
         * Sensor.TipoSensor.AMARRE, OrigenMedicion.MONOBOYA,
         * operacionId);
         * 
         * CentralDatos.TelemetriaResultado resultado =
         * centralDatos.procesarTelemetria(medicion);
         * 
         * System.out.println("\n=== Resultado ===");
         * System.out.println("Medición guardada con ID: " + resultado.getMedicionId());
         * System.out.println("¿Generó alertas? " + resultado.tieneAlertas());
         * for (var info : resultado.getAlertas()) {
         * System.out.println("  → Alerta ID " + info.getId() + " | Tipo: " +
         * info.getTipoAlerta()
         * + " | Mensaje: " + info.getMensaje());
         * }
         */
    }
}