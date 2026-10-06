package com.monoboyas.api;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import com.monoboyas.central.CentralDatos;
import com.monoboyas.equipamiento.Buque;
import com.monoboyas.equipamiento.Monoboya;
import com.monoboyas.equipamiento.Planta;
import com.monoboyas.operaciones.Operacion;
import com.monoboyas.persistencia.BuqueDAO;
import com.monoboyas.persistencia.MonoboyaDAO;
import com.monoboyas.persistencia.OperacionDAO;
import com.monoboyas.persistencia.PlantaDAO;
import com.monoboyas.persistencia.UsuarioDAO;
import com.monoboyas.usuarios.OperadorBuque;
import com.monoboyas.usuarios.OperadorLancha;
import com.monoboyas.usuarios.OperadorPlanta;
import com.monoboyas.usuarios.Usuario;

@Service
public class OperacionService implements CommandLineRunner {

    private static final String PLANIFICADA = "PLANIFICADA";
    private static final String PREPARADA = "PREPARADA";
    private static final String ACTIVA = "ACTIVA";
    private static final String PAUSADA = "PAUSADA";
    private static final String FINALIZADA = "FINALIZADA";

    private final OperacionDAO operacionDAO;
    private final MonoboyaDAO monoboyaDAO;
    private final UsuarioDAO usuarioDAO;
    private final BuqueDAO buqueDAO;
    private final PlantaDAO plantaDAO;
    private final CentralDatos centralDatos;

    public OperacionService(OperacionDAO operacionDAO, MonoboyaDAO monoboyaDAO, UsuarioDAO usuarioDAO,
            BuqueDAO buqueDAO, PlantaDAO plantaDAO, CentralDatos centralDatos) {
        this.operacionDAO = operacionDAO;
        this.monoboyaDAO = monoboyaDAO;
        this.usuarioDAO = usuarioDAO;
        this.buqueDAO = buqueDAO;
        this.plantaDAO = plantaDAO;
        this.centralDatos = centralDatos;
    }

    @Override
    public void run(String... args) {
        List<OperacionDAO.OperacionInfo> activas = operacionDAO.listarPorEstado(ACTIVA);
        for (OperacionDAO.OperacionInfo op : activas) {
            registrarEnCentralDatos(op);
        }
    }

    // ── TRANSICIONES DE ESTADO ───────────────────────────────────────────

    public OperacionDAO.OperacionInfo planificar(int buqueNroIMO, int plantaId, String tipo) {
        Usuario operadorBuque = usuarioDAO.primerOperadorBuqueDisponible()
                .orElseThrow(() -> new IllegalStateException("No hay operadores de buque disponibles"));
        int id = operacionDAO.crearPlanificada(buqueNroIMO, plantaId, tipo, operadorBuque.getId());
        return operacionDAO.buscarPorId(id);
    }

    public OperacionDAO.OperacionInfo preparar(int operacionId, int monoboyaId,
            int operadorPlantaDni, int operadorLanchaDni) {
        OperacionDAO.OperacionInfo op = operacionDAO.buscarPorId(operacionId);
        requireEstado(op, PLANIFICADA, "preparar");

        Usuario operadorPlanta = validarYObtenerOperador(operadorPlantaDni, "OPERADOR_PLANTA");
        Usuario operadorLancha = validarYObtenerOperador(operadorLanchaDni, "OPERADOR_LANCHA");

        operacionDAO.actualizarParaPreparar(operacionId, monoboyaId, operadorPlanta.getId(), operadorLancha.getId());
        return operacionDAO.buscarPorId(operacionId);
    }

    public OperacionDAO.OperacionInfo iniciar(int operacionId, int operadorLanchaDni) {
        OperacionDAO.OperacionInfo op = operacionDAO.buscarPorId(operacionId);
        requireEstado(op, PREPARADA, "iniciar");

        // Solo confirma que quien da el visto bueno tiene el rol correcto.
        // No asigna a nadie nuevo: ya quedó todo asignado en preparar().
        validarYObtenerOperador(operadorLanchaDni, "OPERADOR_LANCHA");

        boolean monoboyaOcupada = operacionDAO.listarPorEstado(ACTIVA).stream()
                .anyMatch(o -> op.getMonoboyaId() != null && op.getMonoboyaId().equals(o.getMonoboyaId()));
        if (monoboyaOcupada) {
            throw new IllegalStateException(
                    "La monoboya " + op.getMonoboyaId() + " ya tiene una operación ACTIVA");
        }

        operacionDAO.actualizarEstado(operacionId, ACTIVA);
        if (op.getMonoboyaId() != null) {
            monoboyaDAO.actualizarOperacionActiva(op.getMonoboyaId(), operacionId);
            monoboyaDAO.actualizarEstado(op.getMonoboyaId(), "OCUPADA");
        }
        registrarEnCentralDatos(op);
        return operacionDAO.buscarPorId(operacionId);
    }

    public OperacionDAO.OperacionInfo detener(int operacionId, int operadorBuqueDni) {
        OperacionDAO.OperacionInfo op = operacionDAO.buscarPorId(operacionId);
        requireEstado(op, ACTIVA, "detener");
        validarYObtenerOperador(operadorBuqueDni, "OPERADOR_BUQUE");
        operacionDAO.actualizarEstado(operacionId, PAUSADA);
        return operacionDAO.buscarPorId(operacionId);
    }

    public OperacionDAO.OperacionInfo reanudar(int operacionId, int operadorBuqueDni) {
        OperacionDAO.OperacionInfo op = operacionDAO.buscarPorId(operacionId);
        requireEstado(op, PAUSADA, "reanudar");
        validarYObtenerOperador(operadorBuqueDni, "OPERADOR_BUQUE");
        operacionDAO.actualizarEstado(operacionId, ACTIVA);
        return operacionDAO.buscarPorId(operacionId);
    }

    public OperacionDAO.OperacionInfo finalizar(int operacionId) {
        OperacionDAO.OperacionInfo op = operacionDAO.buscarPorId(operacionId);
        if (FINALIZADA.equals(op.getEstado())) {
            throw new IllegalStateException(
                    "La operación " + operacionId + " ya está FINALIZADA y no puede modificarse");
        }
        if (!ACTIVA.equals(op.getEstado()) && !PAUSADA.equals(op.getEstado())) {
            throw new IllegalStateException(
                    "Solo se puede finalizar una operación ACTIVA o PAUSADA. Estado actual: " + op.getEstado());
        }
        operacionDAO.actualizarEstado(operacionId, FINALIZADA);
        if (op.getMonoboyaId() != null) {
            monoboyaDAO.actualizarOperacionActiva(op.getMonoboyaId(), null);
            monoboyaDAO.actualizarEstado(op.getMonoboyaId(), "DISPONIBLE");
        }
        centralDatos.finalizarOperacion(operacionId);
        return operacionDAO.buscarPorId(operacionId);
    }

    private void registrarEnCentralDatos(OperacionDAO.OperacionInfo op) {
        Buque buque = (op.getBuqueNroIMO() != null) ? buqueDAO.buscarPorNroIMO(op.getBuqueNroIMO()) : null;
        OperadorBuque operadorBuque = (op.getOperadorBuqueId() != null)
                ? (OperadorBuque) usuarioDAO.buscarPorId(op.getOperadorBuqueId())
                : null;

        Planta planta = null;
        if (op.getPlantaId() != null) {
            PlantaDAO.PlantaInfo info = plantaDAO.buscarPorId(op.getPlantaId());
            planta = new Planta(info.getNombre(), info.getId(), centralDatos);
        }

        Operacion operacionDominio = new Operacion(op.getId(), buque, operadorBuque, planta);

        if (op.getMonoboyaId() != null) {
            Monoboya monoboya = monoboyaDAO.buscarPorId(op.getMonoboyaId());
            monoboya.asignarOperacion(operacionDominio);
            operacionDominio.asignarMonoboya(monoboya);
        }
        if (op.getOperadorLanchaId() != null) {
            operacionDominio.asignarOperadorLancha((OperadorLancha) usuarioDAO.buscarPorId(op.getOperadorLanchaId()));
        }
        if (op.getOperadorPlantaId() != null) {
            operacionDominio.asignarOperadorPlanta((OperadorPlanta) usuarioDAO.buscarPorId(op.getOperadorPlantaId()));
        }

        centralDatos.iniciarOperacion(operacionDominio);
    }

    // ── CONSULTAS ────────────────────────────────────────────────────────

    public List<OperacionDAO.OperacionInfo> obtenerActivas() {
        return operacionDAO.listarPorEstado(ACTIVA);
    }

    public OperacionDAO.OperacionInfo obtenerPorId(int id) {
        return operacionDAO.buscarPorId(id);
    }

    public List<OperacionDAO.OperacionInfo> obtenerTodas() {
        return operacionDAO.listarTodas();
    }

    // ── VALIDACIÓN ───────────────────────────────────────────────────────

    private Usuario validarYObtenerOperador(int dni, String rolEsperado) {
        Usuario usuario = usuarioDAO.buscarPorDni(dni);
        if (!rolEsperado.equals(usuario.getRol())) {
            throw new IllegalArgumentException(
                    "El usuario con DNI " + dni + " tiene rol " + usuario.getRol() +
                            ", se requiere " + rolEsperado);
        }
        return usuario;
    }

    private void requireEstado(OperacionDAO.OperacionInfo op, String requerido, String accion) {
        if (FINALIZADA.equals(op.getEstado())) {
            throw new IllegalStateException(
                    "La operación " + op.getId() + " ya está FINALIZADA y no puede modificarse");
        }
        if (!requerido.equals(op.getEstado())) {
            throw new IllegalStateException(
                    "No se puede " + accion + " la operación " + op.getId() +
                            ": estado actual " + op.getEstado() + ", se requiere " + requerido);
        }
    }
}