package com.monoboyas.api;

import com.monoboyas.persistencia.OperacionDAO;
import com.monoboyas.persistencia.UsuarioDAO;
import com.monoboyas.usuarios.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UsuarioDAO usuarioDAO;
    private final OperacionDAO operacionDAO;

    public AuthController(UsuarioDAO usuarioDAO, OperacionDAO operacionDAO) {
        this.usuarioDAO = usuarioDAO;
        this.operacionDAO = operacionDAO;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {

        Object dniObj = body.get("dni");
        Object password = body.get("password");

        if (dniObj == null || password == null) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", Map.of(
                            "code", "DATOS_INVALIDOS",
                            "message", "DNI y contraseña son requeridos")));
        }

        int dni;

        try {
            dni = Integer.parseInt(dniObj.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", Map.of(
                            "code", "DATOS_INVALIDOS",
                            "message", "El DNI debe ser un número")));
        }

        try {

            Usuario usuario = usuarioDAO.buscarPorDni(dni);

            if (!usuario.getContrasena().equals(password.toString())) {
                return ResponseEntity.status(401).body(Map.of(
                        "error", Map.of(
                                "code", "CREDENCIALES_INVALIDAS",
                                "message", "DNI o contraseña incorrectos")));
            }

            HashMap<String, Object> usuarioResponse = new HashMap<>();

            usuarioResponse.put("dni", usuario.getDni());
            usuarioResponse.put("nombre", usuario.getNombre());
            usuarioResponse.put("rol", usuario.getRol());

            // Estos campos pueden ser null.
            usuarioResponse.put("plantaId", operacionDAO.obtenerPlantaIdDeOperador(usuario.getId()));
            usuarioResponse.put("buqueNroIMO", operacionDAO.obtenerBuqueNroImoDeOperador(usuario.getId()));
            usuarioResponse.put("operacionId", null);
            usuarioResponse.put("creadoEn", null);

            return ResponseEntity.ok(Map.of(
                    "token", "fake-token-de-prueba-12345",
                    "usuario", usuarioResponse));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", Map.of(
                            "code", "CREDENCIALES_INVALIDAS",
                            "message", "DNI o contraseña incorrectos")));
        }
    }
}