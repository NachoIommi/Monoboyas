package com.monoboyas.persistencia;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PlantaDAO {

    private final JdbcTemplate jdbc;

    public PlantaDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // Guarda sin coordenadas (Planta de dominio no las expone)
    public void guardar(int id, String nombre) {
        jdbc.update(
            "INSERT INTO plantas (id, nombre) VALUES (?, ?) ON CONFLICT (id) DO NOTHING",
            id, nombre
        );
    }

    // Guarda con coordenadas geográficas
    public void guardar(int id, String nombre, Double geoLat, Double geoLng) {
        jdbc.update(
            "INSERT INTO plantas (id, nombre, geoLat, geoLng) VALUES (?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
            id, nombre, geoLat, geoLng
        );
    }

    public PlantaInfo buscarPorId(int id) {
        return jdbc.queryForObject(
            "SELECT * FROM plantas WHERE id = ?",
            (rs, rowNum) -> mapRow(rs),
            id
        );
    }

    public List<PlantaInfo> listarTodas() {
        return jdbc.query(
            "SELECT * FROM plantas",
            (rs, rowNum) -> mapRow(rs)
        );
    }

    public void actualizarCoordenadas(int id, double geoLat, double geoLng) {
        jdbc.update(
            "UPDATE plantas SET geoLat = ?, geoLng = ? WHERE id = ?",
            geoLat, geoLng, id
        );
    }

    public void eliminar(int id) {
        jdbc.update("DELETE FROM plantas WHERE id = ?", id);
    }

    private PlantaInfo mapRow(java.sql.ResultSet rs) throws java.sql.SQLException {
        Double lat = rs.getObject("geoLat") != null ? rs.getDouble("geoLat") : null;
        Double lng = rs.getObject("geoLng") != null ? rs.getDouble("geoLng") : null;
        return new PlantaInfo(rs.getInt("id"), rs.getString("nombre"), lat, lng);
    }

    public static class PlantaInfo {
        private final int id;
        private final String nombre;
        private final Double geoLat;
        private final Double geoLng;

        public PlantaInfo(int id, String nombre, Double geoLat, Double geoLng) {
            this.id = id;
            this.nombre = nombre;
            this.geoLat = geoLat;
            this.geoLng = geoLng;
        }

        public int getId()        { return id; }
        public String getNombre() { return nombre; }
        public Double getGeoLat() { return geoLat; }
        public Double getGeoLng() { return geoLng; }
    }
}
