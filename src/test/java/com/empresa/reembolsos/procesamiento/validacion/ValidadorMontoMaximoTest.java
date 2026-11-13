package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.SolicitudDePrueba;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ValidadorMontoMaximoTest {

    private final ValidadorMontoMaximo validador = new ValidadorMontoMaximo();
    private final LocalDate fecha = LocalDate.of(2026, 5, 10);

    @Test
    void viajeUsaTopeDiarioPorDiasDelViaje() {
        // 3 dias x 150.000 = 450.000: justo en el tope se acepta, un peso mas se rechaza
        assertTrue(validador.validar(SolicitudDePrueba.solicitud(CategoriaGasto.VIAJE, "450000", fecha, 3, true), List.of()).valida());
        var excedida = validador.validar(SolicitudDePrueba.solicitud(CategoriaGasto.VIAJE, "450001", fecha, 3, true), List.of());
        assertFalse(excedida.valida());
        assertEquals("Monto excede el tope de la categoria VIAJE", excedida.motivoRechazo());
    }

    @Test
    void viajeLargoNoSeRechazaPorElTopeDeUnSoloDia() {
        // con el tope de un dia (150.000) este viaje de 5 dias seria rechazado injustamente
        assertTrue(validador.validar(SolicitudDePrueba.solicitud(CategoriaGasto.VIAJE, "600000", fecha, 5, true), List.of()).valida());
    }

    @Test
    void representacionTieneTopeFijo() {
        assertTrue(validador.validar(SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "2000000"), List.of()).valida());
        assertFalse(validador.validar(SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "2000001"), List.of()).valida());
    }

    @Test
    void capacitacionTieneTopeFijo() {
        assertTrue(validador.validar(SolicitudDePrueba.valida(CategoriaGasto.CAPACITACION, "3000000"), List.of()).valida());
        assertFalse(validador.validar(SolicitudDePrueba.valida(CategoriaGasto.CAPACITACION, "3000001"), List.of()).valida());
    }

    @Test
    void montoCeroONegativoSeRechaza() {
        assertFalse(validador.validar(SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "0"), List.of()).valida());
        assertFalse(validador.validar(SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "-5"), List.of()).valida());
    }

    @Test
    void categoriaNulaSeRechazaSinLanzarExcepcion() {
        var s = SolicitudDePrueba.valida(CategoriaGasto.VIAJE, "1000");
        s.setCategoria(null);
        var r = validador.validar(s, List.of());
        assertFalse(r.valida());
        assertEquals("La categoria del gasto es obligatoria", r.motivoRechazo());
    }
}
