package com.monoboyas.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.monoboyas.persistencia.UsuarioDAO;

@RestController
@RequestMapping("/v1/usuarios")
public class UsuarioController {

    private static final Set<String> ROLES_VALIDOS = Set.of(
            "ADMIN", "OPERADOR_LANCHA", "OPERADOR_BUQUE", "OPERADOR_PLANTA");

    private final UsuarioDAO usuarioDAO;

    public UsuarioController(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @PostMapping
    public ResponseEntity<?> crear(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, Object> body) {

        if (!esAdmin(authHeader)) {
            return error(403, "SIN_PERMISO", "Solo un ADMIN puede crear usuarios");
        }

        Object nombreObj = body.get("nombre");
        Object contrasenaObj = body.get("contrasena");
        Object dniObj = body.get("dni");
        Object rolObj = body.get("rol");

        if (nombreObj == null || nombreObj.toString().isBlank()
                || contrasenaObj == null || contrasenaObj.toString().isBlank()
                || dniObj == null || rolObj == null) {
            return error(400, "DATOS_INVALIDOS", "nombre, contrasena, dni y rol son requeridos");
        }

        int dni;
        try {
            dni = Integer.parseInt(dniObj.toString());
        } catch (NumberFormatException e) {
            return error(400, "DATOS_INVALIDOS", "El DNI debe ser un número");
        }

        String rol = rolObj.toString();
        if (!ROLES_VALIDOS.contains(rol)) {
            return error(400, "ROL_INVALIDO", "Rol inválido. Permitidos: " + ROLES_VALIDOS);
        }

        try {
            int id = usuarioDAO.crear(nombreObj.toString(), contrasenaObj.toString(), dni, rol);

            Map<String, Object> usuarioResponse = new HashMap<>();
            usuarioResponse.put("id", id);
            usuarioResponse.put("dni", dni);
            usuarioResponse.put("nombre", nombreObj.toString());
            usuarioResponse.put("rol", rol);

            return ResponseEntity.created(URI.create("/v1/usuarios/" + id)).body(usuarioResponse);

        } catch (DuplicateKeyException e) {
            return error(409, "DNI_YA_REGISTRADO", "Ya existe un usuario con ese DNI");
        } catch (Exception e) {
            return error(500, "ERROR_INTERNO", "No se pudo crear el usuario");
        }
    }

    private boolean esAdmin(String authHeader) {
        return true; // retorno true pues esta implementado sin JWT
    }

    private ResponseEntity<Map<String, Object>> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "error", Map.of("code", code, "message", message)));
    }
}