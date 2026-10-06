package com.monoboyas.persistencia;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.monoboyas.central.BrokerMQTT;
import com.monoboyas.equipamiento.Monoboya;
import com.monoboyas.sensores.Sensor;

@Repository
public class MonoboyaDAO {

    private final JdbcTemplate jdbc;
    private final SensorDAO sensorDAO;
    private final BrokerMQTT broker;

    public MonoboyaDAO(JdbcTemplate jdbc, SensorDAO sensorDAO, BrokerMQTT broker) {
        this.jdbc = jdbc;
        this.sensorDAO = sensorDAO;
        this.broker = broker;
    }

    // Guarda desde el objeto de dominio (sin operacion_activa_id ni planta_id)
    // Usa esta cuando ya tenés un objeto Monoboya armado en memoria.
    public void guardar(Monoboya m) {
        int filas = jdbc.update(
                "INSERT INTO monoboyas (id) VALUES (?) ON CONFLICT (id) DO NOTHING",
                m.getId());
        if (filas > 0) {
            crearSensoresPorDefecto(m.getId());
        }
    }

    // Overload sin operacionActivaId — solo delega, no inserta nada por su cuenta.
    // No hace falta tocarlo: como no hace su propio INSERT, no necesita crear
    // sensores acá.
    public void guardar(int id, Integer plantaId) {
        guardar(id, plantaId, null);
    }

    // Guarda con todos los campos conocidos en el momento de la inserción.
    // Usa esta cuando tenés los datos sueltos (id, planta, operación activa) en vez
    // de un objeto.
    public void guardar(int id, Integer plantaId, Integer operacionActivaId) {
        int filas = jdbc.update(
                "INSERT INTO monoboyas (id, planta_id, operacion_activa_id) " +
                        "VALUES (?, ?, ?) ON CONFLICT (id) DO NOTHING",
                id, plantaId, operacionActivaId);
        if (filas > 0) {
            crearSensoresPorDefecto(id);
        }
    }

    // Crea los 8 sensores de una monoboya recién creada.
    // No se llama nunca solo — solo lo disparan los dos métodos de arriba, y solo
    // si la
    // monoboya era nueva de verdad (no si ya existía).
    private void crearSensoresPorDefecto(int monoboyaId) {
        String[] tipos = { "TENSION", "PRESION", "OLEAJE", "ORIENTACION", "CORRIENTE", "CAUDAL", "VIENTO", "AMARRE" };
        String[] unidades = { "tf", "Pa", "m", "grados", "m/s", "l/s", "km/h", "kN" };
        for (int i = 0; i < tipos.length; i++) {
            jdbc.update(
                    "INSERT INTO sensores (tipo, unidad, activo, monoboya_id) VALUES (?, ?, true, ?)",
                    tipos[i], unidades[i], monoboyaId);
        }
    }

    public void actualizarOperacionActiva(int monoboyaId, Integer operacionId) {
        jdbc.update(
                "UPDATE monoboyas SET operacion_activa_id = ? WHERE id = ?",
                operacionId, monoboyaId);
    }

    public void actualizarEstado(int monoboyaId, String estado) {
        jdbc.update(
                "UPDATE monoboyas SET estado = ? WHERE id = ?",
                estado, monoboyaId);
    }

    public Monoboya buscarPorId(int id) {
        List<Sensor> sensores = sensorDAO.cargarDominioPorMonoboya(id);

        Monoboya monoboya = jdbc.queryForObject(
                "SELECT * FROM monoboyas WHERE id = ?",
                (rs, rowNum) -> new Monoboya(rs.getInt("id"), sensores.size(), null, broker),
                id);

        for (Sensor sensor : sensores) {
            monoboya.agregarSensor(sensor);
        }
        return monoboya;
    }

    // Devuelve el ID de la operación activa, o null si no hay ninguna
    public Integer buscarOperacionActivaId(int monoboyaId) {
        return jdbc.queryForObject(
                "SELECT operacion_activa_id FROM monoboyas WHERE id = ?",
                (rs, rowNum) -> {
                    int val = rs.getInt("operacion_activa_id");
                    return rs.wasNull() ? null : val;
                },
                monoboyaId);
    }

    public List<Monoboya> listarTodas() {
        return jdbc.query(
                "SELECT * FROM monoboyas",
                (rs, rowNum) -> new Monoboya(rs.getInt("id"), 0, null, null));
    }

    public List<Monoboya> listarPorPlanta(int plantaId) {
        return jdbc.query(
                "SELECT * FROM monoboyas WHERE planta_id = ?",
                (rs, rowNum) -> new Monoboya(rs.getInt("id"), 0, null, null),
                plantaId);
    }

    public void eliminar(int id) {
        jdbc.update("DELETE FROM monoboyas WHERE id = ?", id);
    }

    public List<MonoboyaInfo> listarTodasInfo() {
        return jdbc.query(
                "SELECT id, estado, planta_id, operacion_activa_id FROM monoboyas",
                (rs, rowNum) -> new MonoboyaInfo(
                        rs.getInt("id"),
                        rs.getString("estado"),
                        rs.getObject("planta_id", Integer.class),
                        rs.getObject("operacion_activa_id", Integer.class)));
    }

    public MonoboyaInfo buscarInfoPorId(int id) {
        return jdbc.queryForObject(
                "SELECT id, estado, planta_id, operacion_activa_id FROM monoboyas WHERE id = ?",
                (rs, rowNum) -> new MonoboyaInfo(
                        rs.getInt("id"),
                        rs.getString("estado"),
                        rs.getObject("planta_id", Integer.class),
                        rs.getObject("operacion_activa_id", Integer.class)),
                id);
    }

    public static class MonoboyaInfo {
        private final int id;
        private final String estado;
        private final Integer plantaId;
        private final Integer operacionActivaId;

        public MonoboyaInfo(int id, String estado, Integer plantaId, Integer operacionActivaId) {
            this.id = id;
            this.estado = estado;
            this.plantaId = plantaId;
            this.operacionActivaId = operacionActivaId;
        }

        public int getId() {
            return id;
        }

        public String getEstado() {
            return estado;
        }

        public Integer getPlantaId() {
            return plantaId;
        }

        public Integer getOperacionActivaId() {
            return operacionActivaId;
        }
    }
}
