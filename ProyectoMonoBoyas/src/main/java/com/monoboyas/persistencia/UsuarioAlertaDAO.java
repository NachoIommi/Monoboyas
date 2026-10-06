package com.monoboyas.persistencia;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class UsuarioAlertaDAO {

    private final JdbcTemplate jdbc;

    public UsuarioAlertaDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void registrarRecepcion(int alertaId, int usuarioId) {
        jdbc.update(
            "INSERT INTO usuario_alerta (alerta_id, usuario_id) VALUES (?, ?) ON CONFLICT DO NOTHING",
            alertaId, usuarioId
        );
    }

    // Solo aplica a alertas de tipo ROJO — el caller es responsable de verificarlo
    public void reconocer(int alertaId, int usuarioId) {
        jdbc.update(
            "UPDATE usuario_alerta SET reconocida = TRUE, fecha_reconocimiento = NOW() " +
            "WHERE alerta_id = ? AND usuario_id = ? AND reconocida = FALSE",
            alertaId, usuarioId
        );
    }

    public List<UsuarioAlertaInfo> listarPorAlerta(int alertaId) {
        return jdbc.query(
            "SELECT * FROM usuario_alerta WHERE alerta_id = ? ORDER BY fecha_recepcion DESC",
            (rs, rowNum) -> mapRow(rs),
            alertaId
        );
    }

    public List<UsuarioAlertaInfo> listarPorUsuario(int usuarioId) {
        return jdbc.query(
            "SELECT * FROM usuario_alerta WHERE usuario_id = ? ORDER BY fecha_recepcion DESC",
            (rs, rowNum) -> mapRow(rs),
            usuarioId
        );
    }

    private UsuarioAlertaInfo mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        java.sql.Timestamp fechaRecon = rs.getTimestamp("fecha_reconocimiento");
        return new UsuarioAlertaInfo(
            rs.getInt("alerta_id"),
            rs.getInt("usuario_id"),
            rs.getTimestamp("fecha_recepcion").toLocalDateTime(),
            rs.getBoolean("reconocida"),
            fechaRecon != null ? fechaRecon.toLocalDateTime() : null
        );
    }

    public static class UsuarioAlertaInfo {
        private final int alertaId;
        private final int usuarioId;
        private final LocalDateTime fechaRecepcion;
        private final boolean reconocida;
        private final LocalDateTime fechaReconocimiento;

        public UsuarioAlertaInfo(int alertaId, int usuarioId, LocalDateTime fechaRecepcion,
                                 boolean reconocida, LocalDateTime fechaReconocimiento) {
            this.alertaId = alertaId;
            this.usuarioId = usuarioId;
            this.fechaRecepcion = fechaRecepcion;
            this.reconocida = reconocida;
            this.fechaReconocimiento = fechaReconocimiento;
        }

        public int getAlertaId()                        { return alertaId; }
        public int getUsuarioId()                       { return usuarioId; }
        public LocalDateTime getFechaRecepcion()        { return fechaRecepcion; }
        public boolean isReconocida()                   { return reconocida; }
        public LocalDateTime getFechaReconocimiento()   { return fechaReconocimiento; }
    }
}
