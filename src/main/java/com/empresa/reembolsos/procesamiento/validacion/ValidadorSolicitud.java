package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.util.List;

/**
 * Eslabon de la cadena de validacion (Chain of Responsibility, RF1).
 *
 * <p>Cada eslabon aplica su propia verificacion y, solo si la solicitud la supera, pasa el control al
 * siguiente eslabon sin saber cuantos hay despues. Agregar una verificacion nueva es crear una subclase y
 * encadenarla; ningun validador existente se modifica.</p>
 */
public abstract class ValidadorSolicitud {

    private ValidadorSolicitud siguiente;

    /**
     * Define el eslabon que sigue a este.
     *
     * @return el eslabon recibido, para poder encadenar llamadas: a.siguiente(b).siguiente(c)
     */
    public ValidadorSolicitud siguiente(ValidadorSolicitud siguiente) {
        this.siguiente = siguiente;
        return siguiente;
    }

    /** Valida con este eslabon y, si pasa y existe siguiente, continua la cadena. */
    public ResultadoValidacion validar(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado) {
        ResultadoValidacion propio = validarPropio(solicitud, pendientesDelEmpleado);
        if (!propio.valida() || siguiente == null) {
            return propio;
        }
        return siguiente.validar(solicitud, pendientesDelEmpleado);
    }

    /** Verificacion especifica de cada eslabon. */
    protected abstract ResultadoValidacion validarPropio(
            SolicitudReembolso solicitud, List<SolicitudReembolso> pendientesDelEmpleado);
}
