package com.monoboyas.api;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.monoboyas.central.CentralDatos;
import com.monoboyas.equipamiento.Monoboya;
import com.monoboyas.operaciones.Operacion;

@Component
public class TelemetriaScheduler {

    private final CentralDatos centralDatos;

    public TelemetriaScheduler(CentralDatos centralDatos) {
        this.centralDatos = centralDatos;
    }

    @Scheduled(fixedRate = 3000)
    /* */
    public void generarLecturas() {

    }
}