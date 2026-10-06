package com.monoboyas.api;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monoboyas.central.CentralDatos;
import com.monoboyas.persistencia.MedicionDAO;
import com.monoboyas.sensores.Medicion;
import com.monoboyas.sensores.Sensor;

@RestController
@RequestMapping("/v1")
public class MedicionController {

    private final MedicionDAO medicionDAO;
    private final CentralDatos centralDatos;

    public MedicionController(MedicionDAO medicionDAO, CentralDatos centralDatos) {
        this.medicionDAO = medicionDAO;
        this.centralDatos = centralDatos;
    }

    // GET /v1/operaciones/{id}/mediciones?sensorId=&desde=&hasta=
    @GetMapping("/operaciones/{operacionId}/mediciones")
    public List<MedicionDAO.MedicionInfo> listarPorOperacion(
            @PathVariable int operacionId,
            @RequestParam(required = false) Integer sensorId,
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta) {

        // Caso 1: sensor + rango → usa el método específico del DAO
        if (sensorId != null && desde != null && hasta != null) {
            return medicionDAO.listarPorRangoTiempo(
                sensorId, Timestamp.valueOf(desde), Timestamp.valueOf(hasta));
        }
        // Caso 2: sensor solo
        if (sensorId != null) {
            return medicionDAO.listarPorOperacionYSensor(operacionId, sensorId);
        }
        // Caso 3: toda la operación
        return medicionDAO.listarPorOperacion(operacionId);
    }

    // POST /v1/mediciones  — ingesta de una lectura nueva
@PostMapping("/mediciones")
public ResponseEntity<?> ingestar(@RequestBody MedicionRequest body) {
    try {
        Sensor.TipoSensor tipoSensor = Sensor.TipoSensor.valueOf(body.getTipoSensor());
        Medicion.OrigenMedicion origen = Medicion.OrigenMedicion.valueOf(body.getOrigen());

        Medicion medicion = new Medicion(
            body.getIdSensor(), body.getValor(), body.getUnidad(),
            tipoSensor, origen, body.getIdOperacion()
        );

        CentralDatos.TelemetriaResultado resultado = centralDatos.procesarTelemetria(medicion);

        return ResponseEntity.status(201).body(Map.of(
            "medicionId", resultado.getMedicionId(),
            "alertas",    resultado.getAlertas()
        ));
    } catch (IllegalArgumentException e) {
        return ResponseEntity.status(400).body(Map.of("error", Map.of(
            "code", "DATOS_INVALIDOS", "message", e.getMessage())));
    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", Map.of(
            "code", "ERROR_INTERNO", "message", e.getMessage())));
    }
}
}
class MedicionRequest {
    private int idSensor;
    private double valor;
    private String unidad;
    private String tipoSensor;
    private String origen;
    private int idOperacion;

    // getters y setters (Spring los necesita para deserializar)
    public int getIdSensor() { return idSensor; }
    public void setIdSensor(int idSensor) { this.idSensor = idSensor; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getTipoSensor() { return tipoSensor; }
    public void setTipoSensor(String tipoSensor) { this.tipoSensor = tipoSensor; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public int getIdOperacion() { return idOperacion; }
    public void setIdOperacion(int idOperacion) { this.idOperacion = idOperacion; }
}