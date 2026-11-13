package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.SolicitudDePrueba;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidadorDuplicadosTest {

    private final ValidadorDuplicados validador = new ValidadorDuplicados();

    private com.empresa.reembolsos.dominio.SolicitudReembolso en(CategoriaGasto c, LocalDate f) {
        return SolicitudDePrueba.solicitud(c, "100000", f, 1, true);
    }

    @Test
    void sinPendientesEsValida() {
        assertTrue(validador.validar(en(CategoriaGasto.VIAJE, LocalDate.of(2026, 5, 10)), List.of()).valida());
    }

    @Test
    void mismaCategoriaYMismoMesSeRechaza() {
        var pendiente = en(CategoriaGasto.VIAJE, LocalDate.of(2026, 5, 2));
        var r = validador.validar(en(CategoriaGasto.VIAJE, LocalDate.of(2026, 5, 28)), List.of(pendiente));
        assertFalse(r.valida());
    }

    @Test
    void otraCategoriaEnElMismoMesEsValida() {
        var pendiente = en(CategoriaGasto.CAPACITACION, LocalDate.of(2026, 5, 2));
        assertTrue(validador.validar(en(CategoriaGasto.VIAJE, LocalDate.of(2026, 5, 28)), List.of(pendiente)).valida());
    }

    @Test
    void mismaCategoriaEnOtroMesEsValida() {
        var pendiente = en(CategoriaGasto.VIAJE, LocalDate.of(2026, 4, 28));
        assertTrue(validador.validar(en(CategoriaGasto.VIAJE, LocalDate.of(2026, 5, 2)), List.of(pendiente)).valida());
    }

    @Test
    void mismoMesDeOtroAnioNoCuentaComoDuplicado() {
        // la version de la guia comparaba solo getMonth() y rechazaba este caso por error
        var pendiente = en(CategoriaGasto.VIAJE, LocalDate.of(2025, 5, 10));
        assertTrue(validador.validar(en(CategoriaGasto.VIAJE, LocalDate.of(2026, 5, 10)), List.of(pendiente)).valida());
    }
}
