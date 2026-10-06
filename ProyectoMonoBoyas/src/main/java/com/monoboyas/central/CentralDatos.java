package com.monoboyas.central;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.monoboyas.alertas.Alerta;
import com.monoboyas.operaciones.Operacion;
import com.monoboyas.persistencia.AlertaDAO;
import com.monoboyas.persistencia.MedicionDAO;
import com.monoboyas.persistencia.OperacionDAO;
import com.monoboyas.persistencia.UsuarioAlertaDAO;
import com.monoboyas.sensores.Medicion;
import com.monoboyas.sensores.Medicion.OrigenMedicion;

@Service
public class CentralDatos {

    public enum NivelAlerta {
        VERDE,
        AMARILLA,
        ROJA
    }

    private Map<Integer, Operacion> operacionesActivas = new HashMap<>();
    private Map<Integer, Double> ultimaPresionMonoboyaPorOperacion = new HashMap<>();
    private Map<Integer, Double> ultimaPresionBuquePorOperacion = new HashMap<>();
    private int contadorAlertaId = 1;

    private final MedicionDAO medicionDAO;
    private final AlertaDAO alertaDAO;
    private final OperacionDAO operacionDAO;
    private final UsuarioAlertaDAO usuarioAlertaDAO;

    public CentralDatos(MedicionDAO medicionDAO, AlertaDAO alertaDAO, OperacionDAO operacionDAO,
            UsuarioAlertaDAO usuarioAlertaDAO) {
        this.medicionDAO = medicionDAO;
        this.alertaDAO = alertaDAO;
        this.operacionDAO = operacionDAO;
        this.usuarioAlertaDAO = usuarioAlertaDAO;
    }

    private int nextAlertaId() {
        return contadorAlertaId++;
    }

    private static final double AMARRE_AMARILLA = 600.0;
    private static final double AMARRE_ROJA = 900.0;

    private static final double TENSION_AMARILLA = 8.0;
    private static final double TENSION_ROJA = 12.0;

    private static final double PRESION_AMARILLA_ALTA = 1_400_000.0;
    private static final double PRESION_ROJA_ALTA = 1_600_000.0;
    private static final double PRESION_ROJA_BAJA = 50_000.0;
    private static final double PRESION_ROJA_DISCREPANCIA = 200_000.0;

    private static final double CAUDAL_AMARILLA = 1600.0;

    private static final double OLEAJE_AMARILLA = 2.5;
    private static final double OLEAJE_ROJA = 3.5;

    private static final double ORIENTACION_AMARILLA = 15.0;
    private static final double ORIENTACION_ROJA = 25.0;

    private static final double CORRIENTE_AMARILLA = 1.5;
    private static final double CORRIENTE_ROJA = 2.2;

    private static final double VIENTO_AMARILLA = 55.0;
    private static final double VIENTO_ROJA = 75.0;

    public TelemetriaResultado procesarTelemetria(Medicion medicion) {
        int medicionId = medicionDAO.guardar(medicion);

        Map<String, Alerta> alertasPorRol = switch (medicion.getTipo()) {
            case TENSION -> verificarUmbralTension(medicion.getValor(), medicion.getIdOperacion());
            case PRESION -> verificarUmbralPresion(medicion);
            case OLEAJE -> verificarUmbralOleaje(medicion.getValor(), medicion.getIdOperacion());
            case ORIENTACION -> verificarUmbralOrientacion(medicion.getValor(), medicion.getIdOperacion());
            case CORRIENTE -> verificarUmbralCorriente(medicion.getValor(), medicion.getIdOperacion());
            case CAUDAL -> verificarUmbralCaudal(medicion.getValor(), medicion.getIdOperacion());
            case VIENTO -> verificarUmbralViento(medicion.getValor(), medicion.getIdOperacion());
            case AMARRE -> verificarUmbralAmarre(medicion.getValor(), medicion.getIdOperacion());
        };

        if (alertasPorRol.isEmpty()) {
            return new TelemetriaResultado(medicionId, new ArrayList<>());
        }

        int operacionId = medicion.getIdOperacion();
        OperacionDAO.OperacionInfo op = operacionDAO.buscarPorId(operacionId);
        List<AlertaDAO.AlertaInfo> infos = new ArrayList<>();

        for (Map.Entry<String, Alerta> entry : alertasPorRol.entrySet()) {
            String rol = entry.getKey();
            Alerta alerta = entry.getValue();
            int alertaId = alertaDAO.guardar(alerta, operacionId, medicionId);

            Integer usuarioId = switch (rol) {
                case "OPERADOR_BUQUE" -> op.getOperadorBuqueId();
                case "OPERADOR_LANCHA" -> op.getOperadorLanchaId();
                case "OPERADOR_PLANTA" -> op.getOperadorPlantaId();
                default -> null;
            };
            if (usuarioId != null) {
                usuarioAlertaDAO.registrarRecepcion(alertaId, usuarioId);
            }

            infos.add(new AlertaDAO.AlertaInfo(
                    alertaId, alerta.getTipoAlerta().toString(), alerta.getMensaje(),
                    operacionId, medicionId, LocalDateTime.now()));
        }

        return new TelemetriaResultado(medicionId, infos);
    }

    /*
     * public void iniciarOperacion(Operacion operacion) {
     * operacionesActivas.put(operacion.getId(), operacion);
     * //(int buqueNroIMO, int plantaId, String tipo, int operadorBuqueId)
     * //crearPlanificada(operacion.getBuque().getNroIMO(),operacion.getPlanta().
     * getId(),"ENCURSO",operacion.getOperadorBuque().getId());
     * }
     */
    public void iniciarOperacion(Operacion operacion) {
        // 1. Guardas en la base de datos y capturas el ID REAL generado por Postgres
        int idGeneradoBD = operacionDAO.crearPlanificada(
                operacion.getBuque().getNroIMO(),
                operacion.getPlanta().getId(),
                "ENCURSO",
                operacion.getOperadorBuque().getId());

        // 2. Le asignas ese ID real al objeto de Java para que todos se enteren
        operacion.setId(idGeneradoBD);

        // 3. Ahora sí lo guardas en el mapa con el ID correcto que la BD sí va a
        // reconocer
        operacionesActivas.put(idGeneradoBD, operacion);
    }
    /*
     * public void finalizarOperacion(int idOperacion) {
     * operacionesActivas.remove(idOperacion);
     * ultimaPresionMonoboyaPorOperacion.remove(idOperacion);
     * ultimaPresionBuquePorOperacion.remove(idOperacion);
     * }
     */

    public void finalizarOperacion(int idOperacion) {
        operacionesActivas.remove(idOperacion);
        ultimaPresionMonoboyaPorOperacion.remove(idOperacion);
        ultimaPresionBuquePorOperacion.remove(idOperacion);

        operacionDAO.actualizarEstado(idOperacion, "FINALIZADA");
    }

    private Map<String, Alerta> verificarUmbralAmarre(double valor, int idoperacion) {
        Map<String, Alerta> alertas = new HashMap<>();

        if (valor > AMARRE_ROJA) {
            System.out.println("SOY LA CENTRAL estoy por crear una Alerta Roja de amarre");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Amarre en riesgo de ruptura", op,
                    valor, "kN");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Amarre en riesgo de ruptura", op,
                    valor, "kN");
            Alerta alertaPlanta = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Amarre en riesgo de ruptura", op,
                    valor, "kN");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);
            alertaPlanta.getOperacion().enviarAlertaOperadorPlanta(alertaPlanta);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);
            alertas.put("OPERADOR_PLANTA", alertaPlanta);

        } else if (valor > AMARRE_AMARILLA) {
            System.out.println("SOY LA CENTRAL [ALERTA AMARILLA] Amarre: " + valor + " kN");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Amarre en riesgo de ruptura",
                    op, valor, "kN");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Amarre en riesgo de ruptura",
                    op, valor, "kN");
            Alerta alertaPlanta = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Amarre en riesgo de ruptura",
                    op, valor, "kN");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);
            alertaPlanta.getOperacion().enviarAlertaOperadorPlanta(alertaPlanta);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);
            alertas.put("OPERADOR_PLANTA", alertaPlanta);
        }
        return alertas;
    }

    private Map<String, Alerta> verificarUmbralTension(double valor, int idoperacion) {
        Map<String, Alerta> alertas = new HashMap<>();

        if (valor > TENSION_ROJA) {
            System.out.println("SOY LA CENTRAL estoy por crear una Alerta Roja de Tension");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Manguera tensada", op, valor,
                    "tf");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Manguera tensada", op, valor,
                    "tf");
            Alerta alertaPlanta = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Manguera tensada", op, valor,
                    "tf");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);
            alertaPlanta.getOperacion().enviarAlertaOperadorPlanta(alertaPlanta);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);
            alertas.put("OPERADOR_PLANTA", alertaPlanta);

        } else if (valor > TENSION_AMARILLA) {
            System.out.println("SOY LA CENTRAL [ALERTA AMARILLA] Tension: " + valor + " tf");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Manguera tensada", op, valor,
                    "tf");
            Alerta alertaPlanta = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Manguera tensada", op, valor,
                    "tf");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaPlanta.getOperacion().enviarAlertaOperadorPlanta(alertaPlanta);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_PLANTA", alertaPlanta);
        }
        return alertas;
    }

    private Map<String, Alerta> verificarUmbralOleaje(double valor, int idoperacion) {
        Map<String, Alerta> alertas = new HashMap<>();
        if (valor > OLEAJE_ROJA) {
            System.out.println("SOY LA CENTRAL estoy por crear una Alerta Roja de Oleaje");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Oleaje elevado", op, valor,
                    "m");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Oleaje elevado", op, valor, "m");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);

        } else if (valor > OLEAJE_AMARILLA) {
            System.out.println("SOY LA CENTRAL [ALERTA AMARILLA] Oleaje: " + valor + " m");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Oleaje elevado", op, valor,
                    "m");

            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);

            alertas.put("OPERADOR_LANCHA", alertaLancha);
        }
        return alertas;
    }

    private Map<String, Alerta> verificarUmbralOrientacion(double valor, int idoperacion) {
        double valorAbsoluto = Math.abs(valor);
        Map<String, Alerta> alertas = new HashMap<>();

        if (valorAbsoluto > ORIENTACION_ROJA) {
            System.out.println("SOY LA CENTRAL estoy por crear una Alerta Roja de Orientacion");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Inclinación excesiva", op, valor,
                    "°");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Inclinación excesiva", op, valor,
                    "°");
            Alerta alertaPlanta = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Inclinación excesiva", op,
                    valor, "°");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);
            alertaPlanta.getOperacion().enviarAlertaOperadorPlanta(alertaPlanta);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);
            alertas.put("OPERADOR_PLANTA", alertaPlanta);

        } else if (valorAbsoluto > ORIENTACION_AMARILLA) {
            System.out.println("SOY LA CENTRAL [ALERTA AMARILLA] Orientacion: " + valor + " grados");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Inclinación excesiva", op,
                    valor, "°");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Inclinación excesiva", op,
                    valor, "°");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);
        }

        return alertas;
    }

    private Map<String, Alerta> verificarUmbralCorriente(double valor, int idoperacion) {
        Map<String, Alerta> alertas = new HashMap<>();

        if (valor > CORRIENTE_ROJA) {
            System.out.println("SOY LA CENTRAL estoy por crear una Alerta Roja de Corriente");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Corriente excesiva", op, valor,
                    "m/s");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Corriente excesiva", op, valor,
                    "m/s");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);

        } else if (valor > CORRIENTE_AMARILLA) {
            System.out.println("SOY LA CENTRAL [ALERTA AMARILLA] Corriente: " + valor + " m/s");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Corriente excesiva", op,
                    valor, "m/s");

            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);

            alertas.put("OPERADOR_LANCHA", alertaLancha);
        }

        return alertas;
    }

    private Map<String, Alerta> verificarUmbralViento(double valor, int idoperacion) {
        Map<String, Alerta> alertas = new HashMap<>();

        if (valor > VIENTO_ROJA) {
            System.out.println("SOY LA CENTRAL estoy por crear una Alerta Roja de Viento");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Viento excesivo", op, valor,
                    "km/h");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA, "Viento excesivo", op, valor,
                    "km/h");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);

        } else if (valor > VIENTO_AMARILLA) {
            System.out.println("SOY LA CENTRAL [ALERTA AMARILLA] Viento: " + valor + " km/h");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA, "Viento excesivo", op, valor,
                    "km/h");

            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);

            alertas.put("OPERADOR_LANCHA", alertaLancha);
        }

        return alertas;
    }

    private Map<String, Alerta> verificarUmbralCaudal(double valor, int idoperacion) {
        Map<String, Alerta> alertas = new HashMap<>();

        if (valor <= 0.0) {
            System.out.println("SOY LA CENTRAL estoy por crear una Alerta de Caudal (bloqueo)");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA,
                    "Caudal en cero, posible bloqueo", op, valor, "l/s");
            Alerta alertaPlanta = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA,
                    "Caudal en cero, posible bloqueo", op, valor, "l/s");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaPlanta.getOperacion().enviarAlertaOperadorPlanta(alertaPlanta);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_PLANTA", alertaPlanta);

        } else if (valor > CAUDAL_AMARILLA) {
            System.out
                    .println("SOY LA CENTRAL: caudal elevado (" + valor + " l/s) pero sin alerta para ningun operador");
        }

        return alertas;
    }

    private Map<String, Alerta> verificarUmbralPresion(Medicion medicion) {
        double valor = medicion.getValor();
        int idoperacion = medicion.getIdOperacion();
        Map<String, Alerta> alertas = new HashMap<>();

        if (valor > PRESION_ROJA_ALTA || valor < PRESION_ROJA_BAJA) {
            System.out.println("SOY LA CENTRAL estoy por crear una Alerta Roja de Presion");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA,
                    "Presión fuera de rango (" + medicion.getOrigen() + ")", op, valor, "Pa");
            Alerta alertaLancha = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA,
                    "Presión fuera de rango (" + medicion.getOrigen() + ")", op, valor, "Pa");
            Alerta alertaPlanta = new Alerta(nextAlertaId(), Alerta.TipoAlerta.ROJA,
                    "Presión fuera de rango (" + medicion.getOrigen() + ")", op, valor, "Pa");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaLancha.getOperacion().enviarAlertaOperadorLancha(alertaLancha);
            alertaPlanta.getOperacion().enviarAlertaOperadorPlanta(alertaPlanta);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_LANCHA", alertaLancha);
            alertas.put("OPERADOR_PLANTA", alertaPlanta);

        } else if (valor > PRESION_AMARILLA_ALTA) {
            System.out.println("SOY LA CENTRAL [ALERTA AMARILLA] Presion: " + valor + " Pa");
            Operacion op = operacionesActivas.get(idoperacion);
            Alerta alertaBuque = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA,
                    "Presión fuera de rango (" + medicion.getOrigen() + ")", op, valor, "Pa");
            Alerta alertaPlanta = new Alerta(nextAlertaId(), Alerta.TipoAlerta.AMARILLA,
                    "Presión fuera de rango (" + medicion.getOrigen() + ")", op, valor, "Pa");

            alertaBuque.getOperacion().enviarAlertaOperadorBuque(alertaBuque);
            alertaPlanta.getOperacion().enviarAlertaOperadorPlanta(alertaPlanta);

            alertas.put("OPERADOR_BUQUE", alertaBuque);
            alertas.put("OPERADOR_PLANTA", alertaPlanta);
        }

        actualizarPresionYVerificarDiscrepancia(medicion);

        return alertas;
    }

    private void actualizarPresionYVerificarDiscrepancia(Medicion medicion) {
        int idOperacion = medicion.getIdOperacion();

        if (medicion.getOrigen() == OrigenMedicion.MONOBOYA) {
            ultimaPresionMonoboyaPorOperacion.put(idOperacion, medicion.getValor());
        } else if (medicion.getOrigen() == OrigenMedicion.BUQUE) {
            ultimaPresionBuquePorOperacion.put(idOperacion, medicion.getValor());
        }

        Double presionMonoboya = ultimaPresionMonoboyaPorOperacion.get(idOperacion);
        Double presionBuque = ultimaPresionBuquePorOperacion.get(idOperacion);

        if (presionMonoboya != null && presionBuque != null) {
            double discrepancia = Math.abs(presionMonoboya - presionBuque);
            if (discrepancia > PRESION_ROJA_DISCREPANCIA) {
                System.out.println("[ALERTA CRITICA] Discrepancia de presión Monoboya/Buque (Operación "
                        + idOperacion + "): " + discrepancia + " Pa (posible fuga en la línea)");
            }
        }
    }

    public java.util.Collection<Operacion> obtenerOperacionesActivas() {
        return operacionesActivas.values();
    }

    public static class TelemetriaResultado {
        private final int medicionId;
        private final List<AlertaDAO.AlertaInfo> alertas;

        public TelemetriaResultado(int medicionId, List<AlertaDAO.AlertaInfo> alertas) {
            this.medicionId = medicionId;
            this.alertas = alertas;
        }

        public int getMedicionId() {
            return medicionId;
        }

        public List<AlertaDAO.AlertaInfo> getAlertas() {
            return alertas;
        }

        public boolean tieneAlertas() {
            return !alertas.isEmpty();
        }
    }
}