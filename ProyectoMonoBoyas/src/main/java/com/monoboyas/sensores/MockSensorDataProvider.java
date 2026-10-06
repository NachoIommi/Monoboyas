package com.monoboyas.sensores;

import java.util.Random;

public class MockSensorDataProvider implements ISensorDataProvider {
    private Random random = new Random();

    @Override
    public double obtenerDato() throws Exception {
        // Simulamos un fallo aleatorio (ej. 20% de probabilidad de error)
        if (random.nextInt(5) == 0) {
            throw new Exception("Error en la conexión con API externa");
        }
        // Retornamos un valor simulado
        return 10.0 + (random.nextDouble() * 90.0);
    }
}