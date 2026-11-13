package com.empresa.reembolsos.aplicacion;

/** Se lanza cuando una solicitud no supera la cadena de validacion; el mensaje es el motivo del rechazo. */
public class SolicitudInvalidaException extends RuntimeException {

    public SolicitudInvalidaException(String motivo) {
        super(motivo);
    }
}
