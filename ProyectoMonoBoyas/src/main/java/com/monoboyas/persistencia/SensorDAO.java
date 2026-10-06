package com.monoboyas.persistencia;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.monoboyas.sensores.Anemometro;
import com.monoboyas.sensores.Caudalimetro;
import com.monoboyas.sensores.Correntometro;
import com.monoboyas.sensores.Giroscopio;
import com.monoboyas.sensores.ISensorDataProvider;
import com.monoboyas.sensores.ProveedorRegistry;
import com.monoboyas.sensores.Sensor;
import com.monoboyas.sensores.SensorDeAmarre;
import com.monoboyas.sensores.SensorDeOleaje;
import com.monoboyas.sensores.SensorDePresion;
import com.monoboyas.sensores.SensorDeTension;

@Repository
public class SensorDAO {

    private final JdbcTemplate jdbc;
    private final ProveedorRegistry proveedores;

    public SensorDAO(JdbcTemplate jdbc, ProveedorRegistry proveedores) {
        this.jdbc = jdbc;
        this.proveedores = proveedores;
    }

    /**
     * Guarda un sensor en la BD.
     * El tipo se obtiene del método getTipo() del sensor.
     */
    public void guardar(Sensor sensor) {
        jdbc.update(
                "INSERT INTO sensores (id, tipo, unidad, activo) VALUES (?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                sensor.getId(), sensor.getTipo().toString(), sensor.getUnidad(), sensor.isActivo());
    }

    public void guardar(Sensor sensor, int monoboyaId) {
        jdbc.update(
                "INSERT INTO sensores (id, tipo, unidad, activo, monoboya_id) VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
                sensor.getId(), sensor.getTipo().toString(), sensor.getUnidad(), sensor.isActivo(), monoboyaId);
    }

    /**
     * Busca un sensor por ID. Devuelve la info básica.
     */
    public SensorInfo buscarPorId(int id) {
        return jdbc.queryForObject(
                "SELECT * FROM sensores WHERE id = ?",
                (rs, rowNum) -> new SensorInfo(
                        rs.getInt("id"),
                        rs.getString("tipo"),
                        rs.getString("unidad"),
                        rs.getBoolean("activo"),
                        rs.getObject("monoboya_id") != null ? rs.getInt("monoboya_id") : null),
                id);
    }

    public List<Sensor> cargarDominioPorMonoboya(int monoboyaId) {
        List<SensorInfo> infos = listarPorMonoboya(monoboyaId);
        List<Sensor> sensores = new ArrayList<>();
        for (SensorInfo info : infos) {
            Sensor sensor = construirSensor(info);
            if (sensor != null)
                sensores.add(sensor);
        }
        return sensores;
    }

    private Sensor construirSensor(SensorInfo info) {
        ISensorDataProvider provider = proveedores.get(info.getTipo());
        return switch (info.getTipo()) {
            case "PRESION" -> new SensorDePresion(info.getId(), provider);
            case "AMARRE" -> new SensorDeAmarre(info.getId(), provider);
            case "OLEAJE" -> new SensorDeOleaje(info.getId(), provider);
            case "TENSION" -> new SensorDeTension(info.getId(), provider);
            case "VIENTO" -> new Anemometro(info.getId(), provider);
            case "CAUDAL" -> new Caudalimetro(info.getId(), provider);
            case "CORRIENTE" -> new Correntometro(info.getId(), provider);
            case "ORIENTACION" -> new Giroscopio(info.getId(), provider);
            default -> null;
        };
    }

    public List<SensorInfo> listarPorMonoboya(int monoboyaId) {
        return jdbc.query(
                "SELECT * FROM sensores WHERE monoboya_id = ?",
                (rs, rowNum) -> new SensorInfo(
                        rs.getInt("id"),
                        rs.getString("tipo"),
                        rs.getString("unidad"),
                        rs.getBoolean("activo"),
                        rs.getInt("monoboya_id")),
                monoboyaId);
    }

    public List<SensorInfo> listarTodos() {
        return jdbc.query(
                "SELECT * FROM sensores",
                (rs, rowNum) -> new SensorInfo(
                        rs.getInt("id"),
                        rs.getString("tipo"),
                        rs.getString("unidad"),
                        rs.getBoolean("activo"),
                        rs.getObject("monoboya_id") != null ? rs.getInt("monoboya_id") : null));
    }

    public void eliminar(int id) {
        jdbc.update("DELETE FROM sensores WHERE id = ?", id);
    }

    /**
     * Clase auxiliar para representar datos de sensor leídos de la BD.
     * Los sensores de dominio (SensorDePresion, Anemometro, etc.) necesitan
     * un ISensorDataProvider para funcionar, que es un objeto de runtime.
     * Esta clase contiene solo los datos persistidos.
     */
    public static class SensorInfo {
        private final int id;
        private final String tipo;
        private final String unidad;
        private final boolean activo;
        private final Integer monoboyaId;

        public SensorInfo(int id, String tipo, String unidad, boolean activo, Integer monoboyaId) {
            this.id = id;
            this.tipo = tipo;
            this.unidad = unidad;
            this.activo = activo;
            this.monoboyaId = monoboyaId;
        }

        public int getId() {
            return id;
        }

        public String getTipo() {
            return tipo;
        }

        public String getUnidad() {
            return unidad;
        }

        public boolean isActivo() {
            return activo;
        }

        public Integer getMonoboyaId() {
            return monoboyaId;
        }
    }
}
