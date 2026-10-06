package com.monoboyas.operaciones;

import com.monoboyas.usuarios.*;
import com.monoboyas.alertas.Alerta;
import com.monoboyas.equipamiento.*;

public class Operacion {
    protected int id;
    protected Monoboya monoboya;
    protected Buque buque;
    protected OperadorLancha operadorLancha;
    protected OperadorBuque operadorBuque;
    protected OperadorPlanta operadorPlanta;
    protected boolean estaActiva;
    protected Planta planta; // Referencia a la planta que opera

    public enum TipoOperacion {
        PREPARADA,
        ENCURSO,
        DETENIDA,
        FINALIZADA
    }

    protected TipoOperacion tipoOperacion;

    public Operacion(int id, Buque b, OperadorBuque operadorBuque, Planta planta) {
        this.id = id;
        buque = b;
        this.planta = planta;
        this.estaActiva = false;
        this.tipoOperacion = TipoOperacion.PREPARADA;
        this.operadorBuque = operadorBuque;
    }

    public void finalizarOperacion() {
        this.estaActiva = false;
        this.tipoOperacion = TipoOperacion.DETENIDA;
        System.out.println("\n[SISTEMA] >>> OPERACIÓN " + this.id + " FINALIZADA. Deteniendo recolección de datos.");
    }

    public void iniciarOperacion() {
        // 1. Verifica que esté en PREPARADA
        if (tipoOperacion != TipoOperacion.PREPARADA) {
            System.out.println("[OPERACIÓN " + id + "] No se puede iniciar: estado actual = " + tipoOperacion
                    + " (se requiere PREPARADA).");
            return;
        }
        if (buque == null || monoboya == null) {
            System.out.println("[OPERACIÓN " + id + "] No se puede iniciar: falta buque o monoboya.");
            return;
        }
        // 2. Pasa a ENCURSO
        this.tipoOperacion = TipoOperacion.ENCURSO;
        this.estaActiva = true;

        while (!buque.finalizoDescarga() && tipoOperacion != TipoOperacion.DETENIDA) {

            monoboya.recolectarYTransmitirDatos();
            buque.recolectarYTransmitirDatos();
            System.out.println("");
            if (buque.getCapacidadRestante() <= 0) {
                finalizarOperacion();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[OPERACIÓN " + id + "] El hilo fue interrumpido durante la espera.");
                break;
            }
        }
    }

    public void detenerOperacion() {
        if (tipoOperacion != TipoOperacion.ENCURSO) {
            System.out.println("[OPERACIÓN " + id + "] No se puede detener: estado actual = " + tipoOperacion
                    + " (se requiere ENCURSO).");
            return;
        }
        this.tipoOperacion = TipoOperacion.DETENIDA;
        this.estaActiva = false;
    }

    public void reanudarOperacion() {// verificar logica
        if (tipoOperacion != TipoOperacion.DETENIDA) {
            System.out.println("[OPERACIÓN " + id + "] No se puede reanudar: estado actual = " + tipoOperacion
                    + " (se requiere DETENIDA).");
            return;
        }
        this.tipoOperacion = TipoOperacion.ENCURSO;
        this.estaActiva = true;
    }

    public void enviarAlertaOperadorBuque(Alerta alerta) {
        if (operadorBuque != null) {
            operadorBuque.recibirAlerta(alerta);
        } else {
            System.out.println("[OPERACIÓN " + id + "] No se puede enviar alerta: no hay operador de buque asignado.");
        }
    }

    public void enviarAlertaOperadorLancha(Alerta alerta) {
        if (operadorLancha != null) {
            operadorLancha.recibirAlerta(alerta);
        } else {
            System.out.println("[OPERACIÓN " + id + "] No se puede enviar alerta: no hay operador de lancha asignado.");
        }
    }

    public void enviarAlertaOperadorPlanta(Alerta alerta) {
        if (operadorPlanta != null) {
            operadorPlanta.recibirAlerta(alerta);
        } else {
            System.out.println("[OPERACIÓN " + id + "] No se puede enviar alerta: no hay operador de planta asignado.");
        }
    }

    public TipoOperacion getTipoOperacion() {
        return tipoOperacion;
    }

    public void asignarMonoboya(Monoboya monoboya) {
        this.monoboya = monoboya;
    }

    public void asignarOperadorLancha(OperadorLancha operador) {
        this.operadorLancha = operador;
    }

    public void asignarOperadorPlanta(OperadorPlanta operador) {
        this.operadorPlanta = operador;
    }

    public void asignarOperadorBuque(OperadorBuque operador) {
        this.operadorBuque = operador;
    }

    public void asignarBuque(Buque barco) {
        this.buque = barco;
    }

    public Planta getPlanta() {
        return planta;
    }

    public Buque getBuque() {
        return buque;
    }

    public Monoboya getMonoboya() {
        return monoboya;
    }

    public OperadorBuque getOperadorBuque() {
        return operadorBuque;
    }

    public OperadorPlanta getOperadorPlanta() {
        return operadorPlanta;
    }

    public OperadorLancha getOperadorLancha() {
        return operadorLancha;
    }

    public int getId() {
        return id;
    }

    public boolean isActiva() {
        return estaActiva;
    }

    public void setId(int i) {
        id = i;
    }

}