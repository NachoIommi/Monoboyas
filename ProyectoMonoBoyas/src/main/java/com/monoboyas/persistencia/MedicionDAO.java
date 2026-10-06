package com.monoboyas.persistencia;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.monoboyas.sensores.Medicion;

@Repository
public class MedicionDAO {

    private final JdbcTemplate jdbc;

    public MedicionDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MedicionInfo> listarRecientePorOperacion(int operacionId, int limite) {
        return jdbc.query(
                "SELECT * FROM (SELECT * FROM mediciones WHERE operacion_id = ? " +
                        "ORDER BY timestamp DESC LIMIT ?) t ORDER BY timestamp ASC",
                (rs, n) -> new MedicionInfo(
                        rs.getInt("id"), rs.getInt("sensor_id"), rs.getInt("operacion_id"),
                        rs.getDouble("valor"), rs.getString("unidad"),
                        rs.getTimestamp("timestamp").toLocalDateTime()),
                operacionId, limite);
    }

    /**
     * Guarda una medición individual y devuelve el ID generado por la BD.
     */
    public int guardar(Medicion medicion) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    // A2: Se agregó operacion_id al INSERT
                    "INSERT INTO mediciones (sensor_id, operacion_id, valor, unidad, timestamp) VALUES (?, ?, ?, ?, ?)",
                    new String[] { "id" });
            ps.setInt(1, medicion.getIdSensor());
            ps.setInt(2, medicion.getIdOperacion()); // A2: Se mapea el ID de la operación
            ps.setDouble(3, medicion.getValor());
            ps.setString(4, medicion.getUnidad());
            ps.setTimestamp(5, Timestamp.valueOf(medicion.getTimestamp()));
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    /**
     * Guarda un lote de mediciones en una sola operación.
     * Ideal para el carril rápido de telemetría (8 mediciones/segundo).
     */
    public void guardarLote(List<Medicion> lote) {
        String sql = "INSERT INTO mediciones (sensor_id, operacion_id, valor, unidad, timestamp) VALUES (?, ?, ?, ?, ?)";

        jdbc.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Medicion m = lote.get(i);
                ps.setInt(1, m.getIdSensor());
                ps.setInt(2, m.getIdOperacion()); // A2: Se mapea el ID de la operación
                ps.setDouble(3, m.getValor());
                ps.setString(4, m.getUnidad());
                ps.setTimestamp(5, Timestamp.valueOf(m.getTimestamp()));
            }

            @Override
            public int getBatchSize() {
                return lote.size();
            }
        });
    }

    /**
     * Esto te permite buscar todas las mediciones de una operación específica para
     * el gráfico.
     */
    public List<MedicionInfo> listarPorOperacion(int operacionId) {
        return jdbc.query(
                "SELECT * FROM mediciones WHERE operacion_id = ? ORDER BY timestamp ASC",
                (rs, rowNum) -> new MedicionInfo(
                        rs.getInt("id"),
                        rs.getInt("sensor_id"),
                        rs.getInt("operacion_id"),
                        rs.getDouble("valor"),
                        rs.getString("unidad"),
                        rs.getTimestamp("timestamp").toLocalDateTime()),
                operacionId);
    }

    /**
     * Obtiene las últimas N mediciones de un sensor.
     */
    public List<MedicionInfo> listarPorSensor(int sensorId, int limite) {
        return jdbc.query(
                "SELECT * FROM mediciones WHERE sensor_id = ? ORDER BY timestamp DESC LIMIT ?",
                (rs, rowNum) -> new MedicionInfo(
                        rs.getInt("id"),
                        rs.getInt("sensor_id"),
                        rs.getInt("operacion_id"),
                        rs.getDouble("valor"),
                        rs.getString("unidad"),
                        rs.getTimestamp("timestamp").toLocalDateTime()),
                sensorId, limite);
    }

    /**
     * Obtiene todas las mediciones en un rango de tiempo.
     */
    public List<MedicionInfo> listarPorRangoTiempo(int sensorId, Timestamp desde, Timestamp hasta) {
        return jdbc.query(
                "SELECT * FROM mediciones WHERE sensor_id = ? AND timestamp BETWEEN ? AND ? ORDER BY timestamp DESC",
                (rs, rowNum) -> new MedicionInfo(
                        rs.getInt("id"),
                        rs.getInt("sensor_id"),
                        rs.getInt("operacion_id"),
                        rs.getDouble("valor"),
                        rs.getString("unidad"),
                        rs.getTimestamp("timestamp").toLocalDateTime()),
                sensorId, desde, hasta);
    }

    public List<MedicionInfo> listarPorOperacionYSensor(int operacionId, int sensorId) {
        return jdbc.query(
                "SELECT * FROM mediciones WHERE operacion_id = ? AND sensor_id = ? ORDER BY timestamp ASC",
                (rs, rowNum) -> new MedicionInfo(
                        rs.getInt("id"),
                        rs.getInt("sensor_id"),
                        rs.getInt("operacion_id"),
                        rs.getDouble("valor"),
                        rs.getString("unidad"),
                        rs.getTimestamp("timestamp").toLocalDateTime()),
                operacionId, sensorId);

    }

    /**
     * Clase auxiliar para mediciones leídas de la BD.
     */
    public static class MedicionInfo {
        private final int id;
        private final int sensorId;
        private final int operacionId;
        private final double valor;
        private final String unidad;
        private final java.time.LocalDateTime timestamp;

        public MedicionInfo(int id, int sensorId, int operacionId, double valor, String unidad,
                java.time.LocalDateTime timestamp) {
            this.id = id;
            this.sensorId = sensorId;
            this.operacionId = operacionId;
            this.valor = valor;
            this.unidad = unidad;
            this.timestamp = timestamp;
        }

        public int getId() {
            return id;
        }

        public int getSensorId() {
            return sensorId;
        }

        public int getOperacionId() {
            return operacionId;
        } // A2: Getter

        public double getValor() {
            return valor;
        }

        public String getUnidad() {
            return unidad;
        }

        public java.time.LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}