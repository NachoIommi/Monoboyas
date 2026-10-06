package com.monoboyas.persistencia;

import com.monoboyas.equipamiento.Buque;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BuqueDAO {

    private final JdbcTemplate jdbc;

    public BuqueDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void guardar(Buque buque) {
        jdbc.update(
            "INSERT INTO buques (nro_imo, capacidad, nombre) VALUES (?, ?, ?) ON CONFLICT (nro_imo) DO NOTHING",
            buque.getNroIMO(), buque.getCapacidad(), buque.getNombre()
        );
    }

    public Buque buscarPorNroIMO(int nroIMO) {
        return jdbc.queryForObject(
            "SELECT * FROM buques WHERE nro_imo = ?",
            (rs, rowNum) -> new Buque(
                rs.getInt("nro_imo"),
                rs.getInt("capacidad"),
                null,
                rs.getString("nombre"),
                null
            ),
            nroIMO
        );
    }

    public List<Buque> listarTodos() {
        return jdbc.query(
            "SELECT * FROM buques",
            (rs, rowNum) -> new Buque(
                rs.getInt("nro_imo"),
                rs.getInt("capacidad"),
                null,
                rs.getString("nombre"),
                null
            )
        );
    }

    public void actualizar(Buque buque) {
        jdbc.update(
            "UPDATE buques SET capacidad = ?, nombre = ? WHERE nro_imo = ?",
            buque.getCapacidad(), buque.getNombre(), buque.getNroIMO()
        );
    }

    public void eliminar(int nroIMO) {
        jdbc.update("DELETE FROM buques WHERE nro_imo = ?", nroIMO);
    }
}
