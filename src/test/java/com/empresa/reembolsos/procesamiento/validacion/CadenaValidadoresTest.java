package com.empresa.reembolsos.procesamiento.validacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.empresa.reembolsos.SolicitudDePrueba;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CadenaValidadoresTest {

    private final CadenaValidadores cadena = new CadenaValidadores(
            new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados());

    @Test
    void solicitudCorrectaPasaLosTresEslabones() {
        assertTrue(cadena.validar(SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "500000"), List.of()).valida());
    }

    @Test
    void siFallaUnEslabonLaCadenaSeDetieneConSuMotivo() {
        var s = SolicitudDePrueba.solicitud(CategoriaGasto.REPRESENTACION, "9000000", LocalDate.of(2026, 5, 1), 0, false);
        // falla documentos (primero) y tambien monto: debe reportar el primero
        assertEquals("Faltan documentos soporte", cadena.validar(s, List.of()).motivoRechazo());
    }

    @Test
    void elEslabonDeDuplicadosNoSeEjecutaSiYaFallaUnoAnterior() {
        // la lista lanzaria excepcion si se la recorriera: demuestra el corte de la cadena
        List<SolicitudReembolso> explosiva = new ArrayList<>() {
            @Override
            public java.util.stream.Stream<SolicitudReembolso> stream() {
                throw new IllegalStateException("no deberia recorrerse");
            }
        };
        var sinDocumentos = SolicitudDePrueba.solicitud(CategoriaGasto.VIAJE, "1000", LocalDate.of(2026, 5, 1), 1, false);
        assertFalse(cadena.validar(sinDocumentos, explosiva).valida());
    }

    @Test
    void unEslabonNuevoSeAgregaSinModificarLosExistentes() {
        ValidadorSolicitud limiteExtra = new ValidadorSolicitud() {
            @Override
            protected ResultadoValidacion validarPropio(SolicitudReembolso s, List<SolicitudReembolso> p) {
                return ResultadoValidacion.rechazar("Limite anual superado");
            }
        };
        var docs = new ValidadorDocumentosSoporte();
        docs.siguiente(new ValidadorMontoMaximo()).siguiente(new ValidadorDuplicados()).siguiente(limiteExtra);
        var r = docs.validar(SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "1000"), List.of());
        assertEquals("Limite anual superado", r.motivoRechazo());
    }
}
