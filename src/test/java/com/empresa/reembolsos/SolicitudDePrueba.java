package com.empresa.reembolsos;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Fabrica de solicitudes para las pruebas (evita repetir setters). */
public final class SolicitudDePrueba {

    private SolicitudDePrueba() {
    }

    public static SolicitudReembolso solicitud(CategoriaGasto categoria, String monto, LocalDate fecha,
                                               int diasViaje, boolean documentos) {
        SolicitudReembolso s = new SolicitudReembolso();
        s.setEmpleadoId("E-100");
        s.setCategoria(categoria);
        s.setMontoSolicitado(new BigDecimal(monto));
        s.setFechaGasto(fecha);
        s.setDiasViaje(diasViaje);
        s.setDocumentosSoporte(documentos);
        s.setEstado(EstadoSolicitud.PENDIENTE);
        return s;
    }

    public static SolicitudReembolso valida(CategoriaGasto categoria, String monto) {
        return solicitud(categoria, monto, LocalDate.of(2026, 5, 10), 3, true);
    }

    /** Compara BigDecimal por valor, ignorando la escala (2.4 y 2.40 son iguales). */
    public static boolean igualesPorValor(String esperado, BigDecimal real) {
        return new BigDecimal(esperado).compareTo(real) == 0;
    }
}
