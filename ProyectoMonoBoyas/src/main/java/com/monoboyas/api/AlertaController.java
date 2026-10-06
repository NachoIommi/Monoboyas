package com.monoboyas.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.monoboyas.persistencia.AlertaDAO;
import com.monoboyas.persistencia.UsuarioAlertaDAO;
import com.monoboyas.persistencia.UsuarioDAO;
import com.monoboyas.usuarios.Usuario;

@RestController
@RequestMapping("/v1/alertas")
public class AlertaController {

    private final AlertaDAO alertaDAO;
    private final UsuarioDAO usuarioDAO;
    private final UsuarioAlertaDAO usuarioAlertaDAO;

    public AlertaController(AlertaDAO alertaDAO, UsuarioDAO usuarioDAO,
                            UsuarioAlertaDAO usuarioAlertaDAO) {
        this.alertaDAO = alertaDAO;
        this.usuarioDAO = usuarioDAO;
        this.usuarioAlertaDAO = usuarioAlertaDAO;
    }

    // GET /v1/alertas
    @GetMapping
    public Map<String, Object> listar(@RequestParam(required = false) String tipo, @RequestParam(required = false) Integer operacionId, @RequestParam(required = false) String estado) {
        List<AlertaDAO.AlertaInfo> alertas = (operacionId != null)
            ? alertaDAO.listarPorOperacion(operacionId)
            : alertaDAO.listarTodas();

        if (tipo != null) {
            alertas = alertas.stream()
                .filter(a -> tipo.equalsIgnoreCase(a.getTipoAlerta()))
                .collect(Collectors.toList());
        }

        return Map.of("data", alertas, "pagination",
            Map.of("page", 1, "limit", 50, "total", alertas.size(), "totalPages", 1));
    }

    // GET /v1/alertas/mis-alertas?dni=...
    @GetMapping("/mis-alertas")
    public ResponseEntity<?> misAlertas(@RequestParam int dni) {
        Usuario u;
        try {
            u = usuarioDAO.buscarPorDni(dni);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                "error", Map.of("code", "USUARIO_NO_ENCONTRADO", "message", "Usuario no encontrado")));
        }

        List<Map<String, Object>> rows = alertaDAO.listarDetalladoPorUsuario(u.getId());
        List<Map<String, Object>> data = new ArrayList<>();

        for (Map<String, Object> r : rows) {
            boolean reconocida = Boolean.TRUE.equals(r.get("reconocida"));
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.get("id"));
            m.put("tipo", mapTipo((String) r.get("tipo_alerta")));
            m.put("mensaje", r.get("mensaje"));
            m.put("operacionId", r.get("id_operacion"));
            m.put("sensorId", null);          // todavía no disponible desde alertas
            m.put("valorMedicion", null);     // idem
            m.put("estado", reconocida ? "RECONOCIDA" : "PENDIENTE");
            m.put("generadaEn", toIso(r.get("timestamp")));
            m.put("reconocidaPorDni", reconocida ? dni : null);
            m.put("reconocidaEn", toIso(r.get("fecha_reconocimiento")));
            data.add(m);
        }
        return ResponseEntity.ok(data); // array plano, como espera tu página
    }

    // PATCH /v1/alertas/{id}/reconocer?dni=...
    @PatchMapping("/{id}/reconocer")
    public ResponseEntity<?> reconocer(@PathVariable int id, @RequestParam int dni) {
        Usuario u;
        try {
            u = usuarioDAO.buscarPorDni(dni);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                "error", Map.of("code", "USUARIO_NO_ENCONTRADO", "message", "Usuario no encontrado")));
        }
        usuarioAlertaDAO.reconocer(id, u.getId());
        return ResponseEntity.ok(Map.of("reconocida", true));
    }

    // ── Helpers ──────────────────────────────────────────────
    private String mapTipo(String tipoBackend) {
        if (tipoBackend == null) return "INFORMATIVA";
        return switch (tipoBackend.toUpperCase()) {
            case "ROJA", "ROJO", "CRITICA"          -> "CRITICA";
            case "AMARILLA", "AMARILLO", "ADVERTENCIA" -> "ADVERTENCIA";
            default                                  -> "INFORMATIVA"; // VERDE, etc.
        };
    }

    private String toIso(Object ts) {
        if (ts == null) return null;
        if (ts instanceof java.sql.Timestamp t) return t.toLocalDateTime().toString();
        return ts.toString();
    }
}