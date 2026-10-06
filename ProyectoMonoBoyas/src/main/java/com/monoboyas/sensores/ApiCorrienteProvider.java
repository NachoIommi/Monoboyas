package com.monoboyas.sensores;

import com.monoboyas.sensores.ISensorDataProvider;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiCorrienteProvider implements ISensorDataProvider {
    private final String URL = "https://marine-api.open-meteo.com/v1/marine?latitude=-38.71&longitude=-62.28&hourly=ocean_current_velocity&timezone=auto";

    @Override
    public double obtenerDato() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).GET().build();
        System.out.println("[PROVEEDOR API] Solicitando corriente a " + URL);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());
        JsonNode currentVelocities = rootNode.path("hourly").path("ocean_current_velocity");
        if (currentVelocities.isArray() && currentVelocities.size() > 0) {
            double valor = currentVelocities.get(0).asDouble();
            System.out.println("[PROVEEDOR API] Corriente recibida = " + valor + " m/s");
            return valor;
        }
        System.out.println("[PROVEEDOR API] Corriente no disponible, devolviendo 0.0");
        return 0.0;
    }
}