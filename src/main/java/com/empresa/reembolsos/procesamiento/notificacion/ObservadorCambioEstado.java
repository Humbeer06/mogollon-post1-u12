package com.empresa.reembolsos.procesamiento.notificacion;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;

/**
 * Interesado en los cambios de estado de una solicitud (Observer, RF3). Cada implementacion reacciona a su
 * manera; el servicio de aplicacion solo conoce esta interfaz, no a los interesados concretos.
 */
public interface ObservadorCambioEstado {

    /**
     * Se invoca despues de guardar el cambio de estado.
     *
     * @param solicitud      la solicitud ya con su estado nuevo
     * @param estadoAnterior el estado que tenia antes del cambio
     */
    void onCambioEstado(SolicitudReembolso solicitud, EstadoSolicitud estadoAnterior);
}
