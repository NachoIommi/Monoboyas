package com.monoboyas.sensores;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class ProveedorRegistry {
    private final Map<String, ISensorDataProvider> providers = new HashMap<>();

    public ProveedorRegistry() {
        providers.put("PRESION", new ArchivoDataProvider("presion.txt"));
        providers.put("OLEAJE", new ApiOleajeProvider());
        providers.put("TENSION", new ArchivoDataProvider("tension.txt"));
        providers.put("VIENTO", new ApiVientoProvider());
        providers.put("CORRIENTE", new ApiCorrienteProvider());
        providers.put("CAUDAL", new ArchivoDataProvider("caudal.txt"));
        providers.put("ORIENTACION", new ArchivoDataProvider("giroscopio.txt"));
        providers.put("AMARRE", new ArchivoDataProvider("amarre.txt"));
    }

    public ISensorDataProvider get(String tipo) {
        return providers.get(tipo);
    }
}