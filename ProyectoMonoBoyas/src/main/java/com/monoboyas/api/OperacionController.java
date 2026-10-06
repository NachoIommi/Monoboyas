package com.monoboyas.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monoboyas.persistencia.BuqueDAO;
import com.monoboyas.persistencia.OperacionDAO;
import com.monoboyas.persistencia.PlantaDAO;
import com.monoboyas.persistencia.UsuarioDAO;
import com.monoboyas.usuarios.Usuario;

@RestController
@RequestMapping("/v1/operaciones")
public class OperacionController {

    private final OperacionDAO operacionDAO;
    private final OperacionService operacionService;
    private final BuqueDAO buqueDAO;
    private final PlantaDAO plantaDAO;
    private final UsuarioDAO usuarioDAO;

    public OperacionController(OperacionDAO operacionDAO,
            OperacionService operacionService,
            BuqueDAO buqueDAO,
            PlantaDAO plantaDAO,
            UsuarioDAO usuarioDAO) {
        this.operacionDAO = operacionDAO;
        this.operacionService = operacionService;
        this.buqueDAO = buqueDAO;
        this.plantaDAO = plantaDAO;
        this.usuarioDAO = usuarioDAO;
    }

    @GetMapping
    public Map<String, Object> listar(@RequestParam(required = false) String estado) {
        List<OperacionDAO.OperacionInfo> operaciones = (estado != null)
                ? operacionDAO.listarPorEstado(estado)
                : operacionDAO.listarTodas();

        return Map.of(
                "data", operaciones,
                "pagination", Map.of(
                        "page", 1,
                        "limit", 20,
                        "total", operaciones.size(),
                        "totalPages", 1));
    }

    @PostMapping
    public ResponseEntity<?> planificar(@RequestBody PlanificarOperacionRequest body) {
        try {
            return ResponseEntity.status(201).body(
                    operacionService.planificar(
                            body.getBuqueNroIMO(),
                            body.getPlantaId(),
                            body.getTipo()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/preparar")
    public ResponseEntity<?> preparar(@PathVariable int id,
            @RequestBody PrepararOperacionRequest body) {
        try {
            return ResponseEntity.ok(
                    operacionService.preparar(
                            id,
                            body.getMonoboyaId(),
                            body.getOperadorPlantaDni(),
                            body.getOperadorLanchaDni()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/iniciar")
    public ResponseEntity<?> iniciar(@PathVariable int id,
            @RequestBody IniciarOperacionRequest body) {
        try {
            return ResponseEntity.ok(
                    operacionService.iniciar(id, body.getOperadorLanchaDni()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/detener")
    public ResponseEntity<?> detener(@PathVariable int id,
            @RequestBody OperadorBuqueRequest body) {
        try {
            return ResponseEntity.ok(
                    operacionService.detener(id, body.getOperadorBuqueDni()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", Map.of(
                            "code", "CONFLICTO_ESTADO",
                            "message", e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", Map.of(
                            "code", "DATOS_INVALIDOS",
                            "message", e.getMessage())));
        }
    }

    @PatchMapping("/{id}/reanudar")
    public ResponseEntity<?> reanudar(@PathVariable int id,
            @RequestBody OperadorBuqueRequest body) {
        try {
            return ResponseEntity.ok(
                    operacionService.reanudar(id, body.getOperadorBuqueDni()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", Map.of(
                            "code", "CONFLICTO_ESTADO",
                            "message", e.getMessage())));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", Map.of(
                            "code", "DATOS_INVALIDOS",
                            "message", e.getMessage())));
        }
    }

    @PatchMapping("/{id}/finalizar")
    public ResponseEntity<?> finalizar(@PathVariable int id) {
        try {
            return ResponseEntity.ok(
                    operacionService.finalizar(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/opciones-planificacion")
    public Map<String, Object> opcionesPlanificacion() {

        List<Map<String, Object>> buques = buqueDAO.listarTodos().stream()
                .map(b -> Map.<String, Object>of(
                        "nroIMO", b.getNroIMO(),
                        "nombre", b.getNombre(),
                        "capacidad", b.getCapacidad()))
                .collect(Collectors.toList());

        List<Map<String, Object>> plantas = plantaDAO.listarTodas().stream()
                .map(p -> Map.<String, Object>of(
                        "id", p.getId(),
                        "nombre", p.getNombre()))
                .collect(Collectors.toList());

        List<Map<String, Object>> operadoresBuque = usuarioDAO.listarPorRol("OPERADOR_BUQUE")
                .stream()
                .map(u -> Map.<String, Object>of(
                        "dni", u.getDni(),
                        "nombre", u.getNombre()))
                .collect(Collectors.toList());

        List<Map<String, Object>> operadoresLancha = usuarioDAO.listarPorRol("OPERADOR_LANCHA")
                .stream()
                .map(u -> Map.<String, Object>of(
                        "dni", u.getDni(),
                        "nombre", u.getNombre()))
                .collect(Collectors.toList());

        return Map.of(
                "buques", buques,
                "plantas", plantas,
                "operadoresBuque", operadoresBuque,
                "operadoresLancha", operadoresLancha);

    }

    @GetMapping("/mis-operaciones")
    public ResponseEntity<?> misOperaciones(@RequestParam int dni) {

        Usuario u;

        try {
            u = usuarioDAO.buscarPorDni(dni);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", Map.of(
                            "code", "USUARIO_NO_ENCONTRADO",
                            "message", "Usuario no encontrado")));
        }

        List<OperacionDAO.OperacionInfo> propias = operacionDAO.listarTodas().stream()
                .filter(o -> (o.getOperadorLanchaId() != null && o.getOperadorLanchaId() == u.getId()) ||
                        (o.getOperadorBuqueId() != null && o.getOperadorBuqueId() == u.getId()) ||
                        (o.getOperadorPlantaId() != null && o.getOperadorPlantaId() == u.getId()))
                .filter(o -> !"FINALIZADA".equals(o.getEstado()))
                .sorted((a, b) -> b.getId() - a.getId())
                .collect(Collectors.toList());

        return ResponseEntity.ok(propias);
    }
}

class OperadorBuqueRequest {
    private int operadorBuqueDni;

    public int getOperadorBuqueDni() {
        return operadorBuqueDni;
    }

    public void setOperadorBuqueDni(int operadorBuqueDni) {
        this.operadorBuqueDni = operadorBuqueDni;
    }
}

class IniciarOperacionRequest {
    private int operadorLanchaDni;

    public int getOperadorLanchaDni() {
        return operadorLanchaDni;
    }

    public void setOperadorLanchaDni(int operadorLanchaDni) {
        this.operadorLanchaDni = operadorLanchaDni;
    }
}

class PrepararOperacionRequest {
    private int monoboyaId;
    private int operadorPlantaDni;
    private int operadorLanchaDni;

    public int getMonoboyaId() {
        return monoboyaId;
    }

    public void setMonoboyaId(int monoboyaId) {
        this.monoboyaId = monoboyaId;
    }

    public int getOperadorPlantaDni() {
        return operadorPlantaDni;
    }

    public void setOperadorPlantaDni(int operadorPlantaDni) {
        this.operadorPlantaDni = operadorPlantaDni;
    }

    public int getOperadorLanchaDni() {
        return operadorLanchaDni;
    }

    public void setOperadorLanchaDni(int operadorLanchaDni) {
        this.operadorLanchaDni = operadorLanchaDni;
    }
}

class PlanificarOperacionRequest {
    private int buqueNroIMO;
    private int plantaId;
    private String tipo;

    public int getBuqueNroIMO() {
        return buqueNroIMO;
    }

    public void setBuqueNroIMO(int buqueNroIMO) {
        this.buqueNroIMO = buqueNroIMO;
    }

    public int getPlantaId() {
        return plantaId;
    }

    public void setPlantaId(int plantaId) {
        this.plantaId = plantaId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}