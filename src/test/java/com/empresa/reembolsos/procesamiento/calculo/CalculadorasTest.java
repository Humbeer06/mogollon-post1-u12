package com.empresa.reembolsos.procesamiento.calculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.SolicitudDePrueba;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class CalculadorasTest {

    private final CalculadoraViaje viaje = new CalculadoraViaje();
    private final CalculadoraRepresentacion representacion = new CalculadoraRepresentacion();
    private final CalculadoraCapacitacion capacitacion = new CalculadoraCapacitacion();

    @Test
    void viajeReembolsaElMontoSiNoSuperaElTopeDiarioPorDias() {
        var s = SolicitudDePrueba.solicitud(CategoriaGasto.VIAJE, "300000", LocalDate.of(2026, 5, 1), 3, true);
        assertTrue(SolicitudDePrueba.igualesPorValor("300000", viaje.calcular(s)));
    }

    @Test
    void viajeRecortaAlTopeDiarioPorDias() {
        var s = SolicitudDePrueba.solicitud(CategoriaGasto.VIAJE, "900000", LocalDate.of(2026, 5, 1), 3, true);
        assertTrue(SolicitudDePrueba.igualesPorValor("450000", viaje.calcular(s)));
    }

    @Test
    void representacionReembolsaElMontoCompletoHastaElTope() {
        assertTrue(SolicitudDePrueba.igualesPorValor("750000", representacion.calcular(SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "750000"))));
        assertTrue(SolicitudDePrueba.igualesPorValor("2000000", representacion.calcular(SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "2500000"))));
    }

    @Test
    void capacitacionReembolsaElOchentaPorCiento() {
        assertTrue(SolicitudDePrueba.igualesPorValor("800000", capacitacion.calcular(SolicitudDePrueba.valida(CategoriaGasto.CAPACITACION, "1000000"))));
    }

    @Test
    void capacitacionRespetaElTopeAnual() {
        // 80 % de 5.000.000 = 4.000.000 > tope 3.000.000
        assertTrue(SolicitudDePrueba.igualesPorValor("3000000", capacitacion.calcular(SolicitudDePrueba.valida(CategoriaGasto.CAPACITACION, "5000000"))));
    }

    @Test
    void cadaCalculadoraDeclaraSuCategoria() {
        assertEquals(CategoriaGasto.VIAJE, viaje.getCategoria());
        assertEquals(CategoriaGasto.REPRESENTACION, representacion.getCategoria());
        assertEquals(CategoriaGasto.CAPACITACION, capacitacion.getCategoria());
    }

    @Test
    void selectorDevuelveLaCalculadoraDeLaCategoria() {
        var selector = new SelectorCalculadora(List.of(viaje, representacion, capacitacion));
        assertSame(viaje, selector.obtener(CategoriaGasto.VIAJE));
        assertSame(representacion, selector.obtener(CategoriaGasto.REPRESENTACION));
        assertSame(capacitacion, selector.obtener(CategoriaGasto.CAPACITACION));
    }

    @Test
    void selectorFallaConMensajeClaroSiNoHayCalculadora() {
        var selector = new SelectorCalculadora(List.of(viaje));
        var ex = assertThrows(IllegalArgumentException.class, () -> selector.obtener(CategoriaGasto.CAPACITACION));
        assertEquals("Categoria no soportada: CAPACITACION", ex.getMessage());
    }
}
