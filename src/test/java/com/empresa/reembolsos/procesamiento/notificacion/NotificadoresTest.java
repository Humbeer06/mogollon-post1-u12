package com.empresa.reembolsos.procesamiento.notificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.SolicitudDePrueba;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class NotificadoresTest {

    private SolicitudReembolso con(EstadoSolicitud estado) {
        SolicitudReembolso s = SolicitudDePrueba.valida(CategoriaGasto.CAPACITACION, "1000000");
        s.setId(7L);
        s.setMontoAprobado(new BigDecimal("800000"));
        s.setEstado(estado);
        return s;
    }

    /** Ejecuta la accion capturando System.out y devuelve lo que imprimio. */
    private String capturar(Runnable accion) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true));
        try {
            accion.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    @Test
    void empleadoRecibeMensajeEnCualquierCambio() {
        String salida = capturar(() -> new NotificadorEmpleado().onCambioEstado(con(EstadoSolicitud.RECHAZADA), EstadoSolicitud.PENDIENTE));
        assertTrue(salida.contains("Notificando a E-100"));
        assertTrue(salida.contains("PENDIENTE a RECHAZADA"));
    }

    @Test
    void contabilidadActuaSoloCuandoSeAprueba() {
        String aprobada = capturar(() -> new NotificadorContabilidad().onCambioEstado(con(EstadoSolicitud.APROBADA), EstadoSolicitud.PENDIENTE));
        assertTrue(aprobada.contains("Contabilidad: programar pago de 800000 para E-100"));
    }

    @Test
    void contabilidadIgnoraRechazosYCorrecciones() {
        for (EstadoSolicitud estado : new EstadoSolicitud[] {EstadoSolicitud.RECHAZADA, EstadoSolicitud.REQUIERE_CORRECCION, EstadoSolicitud.PENDIENTE}) {
            String salida = capturar(() -> new NotificadorContabilidad().onCambioEstado(con(estado), EstadoSolicitud.PENDIENTE));
            assertEquals("", salida, "no debe actuar en " + estado);
        }
    }
}
