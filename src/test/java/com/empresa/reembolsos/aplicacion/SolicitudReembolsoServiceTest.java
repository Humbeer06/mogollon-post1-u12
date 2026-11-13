package com.empresa.reembolsos.aplicacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.empresa.reembolsos.SolicitudDePrueba;
import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import com.empresa.reembolsos.infraestructura.RepositorioSolicitudesJpa;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraCapacitacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraRepresentacion;
import com.empresa.reembolsos.procesamiento.calculo.CalculadoraViaje;
import com.empresa.reembolsos.procesamiento.calculo.SelectorCalculadora;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.empresa.reembolsos.procesamiento.validacion.CadenaValidadores;
import com.empresa.reembolsos.procesamiento.validacion.ValidadorDocumentosSoporte;
import com.empresa.reembolsos.procesamiento.validacion.ValidadorDuplicados;
import com.empresa.reembolsos.procesamiento.validacion.ValidadorMontoMaximo;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SolicitudReembolsoServiceTest {

    private RepositorioSolicitudesJpa repositorio;
    private ObservadorCambioEstado observador1;
    private ObservadorCambioEstado observador2;
    private SolicitudReembolsoService servicio;

    @BeforeEach
    void preparar() {
        repositorio = mock(RepositorioSolicitudesJpa.class);
        observador1 = mock(ObservadorCambioEstado.class);
        observador2 = mock(ObservadorCambioEstado.class);
        when(repositorio.save(any(SolicitudReembolso.class))).thenAnswer(inv -> inv.getArgument(0));
        servicio = new SolicitudReembolsoService(
                new CadenaValidadores(new ValidadorDocumentosSoporte(), new ValidadorMontoMaximo(), new ValidadorDuplicados()),
                new SelectorCalculadora(List.of(new CalculadoraViaje(), new CalculadoraRepresentacion(), new CalculadoraCapacitacion())),
                List.of(observador1, observador2),
                repositorio);
    }

    @Test
    void crearValidaCalculaYGuardaComoPendiente() {
        when(repositorio.findByEmpleadoIdAndEstado("E-100", EstadoSolicitud.PENDIENTE)).thenReturn(List.of());
        var creada = servicio.crear(SolicitudDePrueba.valida(CategoriaGasto.CAPACITACION, "1000000"));
        assertEquals(EstadoSolicitud.PENDIENTE, creada.getEstado());
        assertTrue(SolicitudDePrueba.igualesPorValor("800000", creada.getMontoAprobado()));
        verify(repositorio).save(creada);
    }

    @Test
    void crearSinDocumentosLanzaExcepcionConMotivoYNoGuarda() {
        when(repositorio.findByEmpleadoIdAndEstado("E-100", EstadoSolicitud.PENDIENTE)).thenReturn(List.of());
        var sinDocs = SolicitudDePrueba.valida(CategoriaGasto.VIAJE, "100000");
        sinDocs.setDocumentosSoporte(false);
        var ex = assertThrows(SolicitudInvalidaException.class, () -> servicio.crear(sinDocs));
        assertEquals("Faltan documentos soporte", ex.getMessage());
        verify(repositorio, never()).save(any());
    }

    @Test
    void crearRechazaUnDuplicadoPendienteDelMismoPeriodo() {
        var existente = SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "100000");
        when(repositorio.findByEmpleadoIdAndEstado("E-100", EstadoSolicitud.PENDIENTE)).thenReturn(List.of(existente));
        var nueva = SolicitudDePrueba.valida(CategoriaGasto.REPRESENTACION, "200000");
        assertThrows(SolicitudInvalidaException.class, () -> servicio.crear(nueva));
    }

    @Test
    void cambiarEstadoNotificaATodosLosObservadoresConElEstadoAnterior() {
        var existente = SolicitudDePrueba.valida(CategoriaGasto.VIAJE, "100000");
        existente.setId(5L);
        when(repositorio.findById(5L)).thenReturn(Optional.of(existente));
        var resultado = servicio.cambiarEstado(5L, EstadoSolicitud.APROBADA);
        assertEquals(EstadoSolicitud.APROBADA, resultado.getEstado());
        verify(observador1).onCambioEstado(resultado, EstadoSolicitud.PENDIENTE);
        verify(observador2).onCambioEstado(resultado, EstadoSolicitud.PENDIENTE);
    }

    @Test
    void cambiarEstadoDeUnaSolicitudInexistenteFallaSinNotificar() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> servicio.cambiarEstado(99L, EstadoSolicitud.APROBADA));
        verify(observador1, never()).onCambioEstado(any(), any());
    }
}
