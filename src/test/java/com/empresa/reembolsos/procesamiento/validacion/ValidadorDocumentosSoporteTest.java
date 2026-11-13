package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.SolicitudDePrueba;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidadorDocumentosSoporteTest {

    private final ValidadorDocumentosSoporte validador = new ValidadorDocumentosSoporte();

    @Test
    void conDocumentosEsValida() {
        var s = SolicitudDePrueba.solicitud(CategoriaGasto.VIAJE, "100000", LocalDate.of(2026, 5, 1), 2, true);
        assertTrue(validador.validar(s, List.of()).valida());
    }

    @Test
    void sinDocumentosSeRechazaConMensajeClaro() {
        var s = SolicitudDePrueba.solicitud(CategoriaGasto.VIAJE, "100000", LocalDate.of(2026, 5, 1), 2, false);
        var r = validador.validar(s, List.of());
        assertFalse(r.valida());
        assertEquals("Faltan documentos soporte", r.motivoRechazo());
    }
}
