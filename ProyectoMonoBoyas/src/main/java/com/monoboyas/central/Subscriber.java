package com.monoboyas.central;

import com.monoboyas.sensores.*;

public interface Subscriber {
    void recibirMensaje(Medicion medicion);
}