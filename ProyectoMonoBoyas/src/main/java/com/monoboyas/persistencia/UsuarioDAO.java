package com.monoboyas.persistencia;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.monoboyas.usuarios.Administrador;
import com.monoboyas.usuarios.OperadorBuque;
import com.monoboyas.usuarios.OperadorLancha;
import com.monoboyas.usuarios.OperadorPlanta;
import com.monoboyas.usuarios.Usuario;

@Repository
public class UsuarioDAO {

    private final JdbcTemplate jdbc;

    public UsuarioDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * Guarda cualquier subclase de Usuario.
     */
    public void guardar(Usuario usuario) {
        jdbc.update(
            "INSERT INTO usuarios (id, nombre, contrasena, dni, rol) VALUES (?, ?, ?, ?, ?) ON CONFLICT (id) DO NOTHING",
            usuario.getId(),
            usuario.getNombre(),
            usuario.getContrasena(),
            usuario.getDni(),
            usuario.getRol()
        );
    }

    public int crear(String nombre, String contrasena, int dni, String rol) {
    KeyHolder keyHolder = new GeneratedKeyHolder();
    jdbc.update(connection -> {
        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO usuarios (nombre, contrasena, dni, rol) VALUES (?, ?, ?, ?)",
            new String[]{"id"}
        );
        ps.setString(1, nombre);
        ps.setString(2, contrasena);
        ps.setInt(3, dni);
        ps.setString(4, rol);
        return ps;
    }, keyHolder);
    return keyHolder.getKey().intValue();
}

    public Usuario buscarPorDni(int dni) {
        return jdbc.queryForObject(
            "SELECT * FROM usuarios WHERE dni = ?",
            (rs, rowNum) -> instanciarUsuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("contrasena"),
                rs.getInt("dni"),
                rs.getString("rol")
            ),
            dni
        );
    }

    public Usuario buscarPorId(int id) {
        return jdbc.queryForObject(
            "SELECT * FROM usuarios WHERE id = ?",
            (rs, rowNum) -> instanciarUsuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("contrasena"),
                rs.getInt("dni"),
                rs.getString("rol")
            ),
            id
        );
    }

    public List<Usuario> listarTodos() {
        return jdbc.query(
            "SELECT * FROM usuarios",
            (rs, rowNum) -> instanciarUsuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("contrasena"),
                rs.getInt("dni"),
                rs.getString("rol")
            )
        );
    }

    public List<Usuario> listarPorRol(String rol) {
        return jdbc.query(
            "SELECT * FROM usuarios WHERE rol = ?",
            (rs, rowNum) -> instanciarUsuario(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("contrasena"),
                rs.getInt("dni"),
                rs.getString("rol")
            ),
            rol
        );
    }

    public void actualizar(Usuario usuario) {
        jdbc.update(
            "UPDATE usuarios SET nombre = ?, contrasena = ? WHERE id = ?",
            usuario.getNombre(),
            usuario.getContrasena(),
            usuario.getId()
        );
    }

    public void eliminar(int id) {
        jdbc.update(
            "DELETE FROM usuarios WHERE id = ?",
            id
        );
    }

    /**
     * Reconstruye la subclase correspondiente según el rol almacenado en la BD.
     */
    private Usuario instanciarUsuario(int id, String nombre, String contrasena, int dni, String rol) {

        return switch (rol) {
            case "ADMIN" ->
                new Administrador(id, nombre, contrasena, dni, null);

            case "OPERADOR_LANCHA" ->
                new OperadorLancha(id, nombre, contrasena, dni);

            case "OPERADOR_BUQUE" ->
                new OperadorBuque(id, nombre, contrasena, dni);

            case "OPERADOR_PLANTA" ->
                new OperadorPlanta(id, nombre, contrasena, dni);

            default ->
                throw new IllegalArgumentException("Rol desconocido: " + rol);
        };
    }

    public Optional<Usuario> primerOperadorBuqueDisponible() {
        List<Usuario> resultado = jdbc.query(
            "SELECT * FROM usuarios u " +
            "WHERE u.rol = 'OPERADOR_BUQUE' " +
            "AND u.id NOT IN (" +
            "  SELECT operador_buque_id FROM operaciones " +
            "  WHERE estado != 'FINALIZADA' AND operador_buque_id IS NOT NULL" +
            ") ORDER BY u.id LIMIT 1",
            (rs, rowNum) -> instanciarUsuario(
                rs.getInt("id"), rs.getString("nombre"), rs.getString("contrasena"),
                rs.getInt("dni"), rs.getString("rol")
            )
        );
        return resultado.isEmpty() ? Optional.empty() : Optional.of(resultado.get(0));
    }
}