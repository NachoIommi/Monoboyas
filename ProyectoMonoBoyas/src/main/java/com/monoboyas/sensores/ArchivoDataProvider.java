package com.monoboyas.sensores;

import com.monoboyas.sensores.ISensorDataProvider;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

public class ArchivoDataProvider implements ISensorDataProvider {
    private final List<String> lineas;
    private final String rutaArchivo;
    private int indiceActual = 0;

    public ArchivoDataProvider(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
        List<String> contenido;
        try {
            contenido = Files.readAllLines(Paths.get(rutaArchivo), StandardCharsets.UTF_8);
        } catch (Exception e) {
            contenido = Collections.emptyList();
        }
        this.lineas = contenido;
    }

    @Override
    public double obtenerDato() throws Exception {
        if (lineas.isEmpty()) {
            throw new Exception("Archivo no encontrado o vacío: " + rutaArchivo);
        }

        String linea = lineas.get(indiceActual).trim();
        double valor = Double.parseDouble(linea);

        // Avanzar al siguiente, y si llegamos al final, volver a cero (Loop infinito)
        indiceActual++;
        if (indiceActual >= lineas.size()) {
            indiceActual = 0;
        }

        return valor;
    }
}