package com.monoboyas.sensores;

import com.monoboyas.sensores.ISensorDataProvider;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiVientoProvider implements ISensorDataProvider {
    private final String URL = "https://api.open-meteo.com/v1/forecast?latitude=-38.71&longitude=-62.28&current_weather=true&timezone=auto";

    @Override
    public double obtenerDato() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(URL)).GET().build();
        System.out.println("[PROVEEDOR API] Solicitando viento a " + URL);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(response.body());
        JsonNode currentWeather = rootNode.path("current_weather");
        if (!currentWeather.isMissingNode()) {
            double valor = currentWeather.path("windspeed").asDouble();
            System.out.println("[PROVEEDOR API] Viento recibido = " + valor + " km/h");
            return valor;
        }
        System.out.println("[PROVEEDOR API] Viento no disponible, devolviendo 0.0");
        return 0.0;
    }
}