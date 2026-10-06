package com.monoboyas.api;

import com.monoboyas.persistencia.SensorDAO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/sensores")
public class SensorController {

    private final SensorDAO sensorDAO;

    public SensorController(SensorDAO sensorDAO) {
        this.sensorDAO = sensorDAO;
    }

    @GetMapping
    public List<SensorDAO.SensorInfo> listar(
            @RequestParam(required = false) Integer monoboyaId,
            @RequestParam(required = false) Boolean activo) {

        List<SensorDAO.SensorInfo> sensores = (monoboyaId != null)
            ? sensorDAO.listarPorMonoboya(monoboyaId)
            : sensorDAO.listarTodos();

        if (activo != null) {
            sensores = sensores.stream()
                .filter(s -> s.isActivo() == activo)
                .collect(Collectors.toList());
        }
        return sensores;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable int id) {
        try {
            return ResponseEntity.ok(sensorDAO.buscarPorId(id));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", Map.of(
                "code", "SENSOR_NO_ENCONTRADO", "message", "Sensor " + id + " no encontrado")));
        }
    }
}