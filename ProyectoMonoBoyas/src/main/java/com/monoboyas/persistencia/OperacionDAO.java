package com.monoboyas.persistencia;

import java.sql.PreparedStatement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class OperacionDAO {

    private final JdbcTemplate jdbc;

    public OperacionDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public int crearPlanificada(int buqueNroIMO, int plantaId, String tipo, int operadorBuqueId) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(con -> {
        PreparedStatement ps = con.prepareStatement(
            "INSERT INTO operaciones (buque_nro_imo, planta_id, tipo, operador_buque_id, estado) " +
            "VALUES (?, ?, ?, ?, 'PLANIFICADA')",
            new String[]{"id"}
        );
        ps.setInt(1, buqueNroIMO);
        ps.setInt(2, plantaId);
        ps.setString(3, tipo);
        ps.setInt(4, operadorBuqueId);
        return ps;
    }, keyHolder);
    return keyHolder.getKey().intValue();
}

    public void actualizarParaPreparar(int id, int monoboyaId, int opPlantaId, int opLanchaId) {
        jdbc.update(
            "UPDATE operaciones SET monoboya_id = ?, operador_planta_id = ?, " +
            "operador_lancha_id = ?, estado = 'PREPARADA' WHERE id = ?",
            monoboyaId, opPlantaId, opLanchaId, id
        );
    }

    public void actualizarEstado(int id, String estado) {
        jdbc.update(
            "UPDATE operaciones SET estado = ? WHERE id = ?",
            estado, id
        );
    }

    public void eliminar(int id) {
        jdbc.update("DELETE FROM operaciones WHERE id = ?", id);
    }

    public OperacionInfo buscarPorId(int id) {
        return jdbc.queryForObject(
            "SELECT * FROM operaciones WHERE id = ?",
            (rs, rowNum) -> mapRow(rs),
            id
        );
    }

    public List<OperacionInfo> listarPorEstado(String estado) {
        return jdbc.query(
            "SELECT * FROM operaciones WHERE estado = ? ORDER BY id",
            (rs, rowNum) -> mapRow(rs),
            estado
        );
    }

    // Cambiar el string hardcodeado:
    public List<OperacionInfo> listarActivas() {
        return listarPorEstado("ACTIVA");
    }

    public List<OperacionInfo> listarTodas() {
        return jdbc.query(
            "SELECT * FROM operaciones ORDER BY id",
            (rs, rowNum) -> mapRow(rs)
        );
    }

    private OperacionInfo mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new OperacionInfo(
            rs.getInt("id"),
            rs.getObject("monoboya_id") != null ? rs.getInt("monoboya_id") : null,
            rs.getObject("buque_nro_imo") != null ? rs.getInt("buque_nro_imo") : null,
            rs.getObject("operador_lancha_id") != null ? rs.getInt("operador_lancha_id") : null,
            rs.getObject("operador_buque_id") != null ? rs.getInt("operador_buque_id") : null,
            rs.getObject("operador_planta_id") != null ? rs.getInt("operador_planta_id") : null,
            rs.getString("estado"),
            rs.getString("tipo"),
            rs.getObject("planta_id") != null ? rs.getInt("planta_id") : null
        );
    }

    public Integer obtenerPlantaIdDeOperador(int usuarioId) {
        List<Integer> resultado = jdbc.query(
            "SELECT planta_id FROM operaciones WHERE operador_planta_id = ? ORDER BY id DESC LIMIT 1",
            (rs, rowNum) -> rs.getObject("planta_id", Integer.class),
            usuarioId
        );
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public Integer obtenerBuqueNroImoDeOperador(int usuarioId) {
        List<Integer> resultado = jdbc.query(
            "SELECT buque_nro_imo FROM operaciones WHERE operador_buque_id = ? ORDER BY id DESC LIMIT 1",
            (rs, rowNum) -> rs.getObject("buque_nro_imo", Integer.class),
            usuarioId
        );
        return resultado.isEmpty() ? null : resultado.get(0);
    }

    public static class OperacionInfo {
        private final int id;
        private final Integer monoboyaId;
        private final Integer buqueNroIMO;
        private final Integer operadorLanchaId;
        private final Integer operadorBuqueId;
        private final Integer operadorPlantaId;
        private final String estado;
        private final String tipo;
        private final Integer plantaId;

        public OperacionInfo(int id, Integer monoboyaId, Integer buqueNroIMO,
                             Integer operadorLanchaId, Integer operadorBuqueId,
                             Integer operadorPlantaId, String estado, String tipo,
                             Integer plantaId) {
            this.id = id;
            this.monoboyaId = monoboyaId;
            this.buqueNroIMO = buqueNroIMO;
            this.operadorLanchaId = operadorLanchaId;
            this.operadorBuqueId = operadorBuqueId;
            this.operadorPlantaId = operadorPlantaId;
            this.estado = estado;
            this.tipo = tipo;
            this.plantaId = plantaId;
        }

        public int getId()                   { return id; }
        public Integer getMonoboyaId()       { return monoboyaId; }
        public Integer getBuqueNroIMO()      { return buqueNroIMO; }
        public Integer getOperadorLanchaId() { return operadorLanchaId; }
        public Integer getOperadorBuqueId()  { return operadorBuqueId; }
        public Integer getOperadorPlantaId() { return operadorPlantaId; }
        public String getEstado()            { return estado; }
        public String getTipo()              { return tipo; }
        public Integer getPlantaId()         { return plantaId; }
    }
}