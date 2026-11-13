package com.empresa.reembolsos;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.empresa.reembolsos.dominio.EstadoSolicitud;
import com.empresa.reembolsos.procesamiento.notificacion.NotificadorAuditoria;
import com.empresa.reembolsos.procesamiento.notificacion.NotificadorContabilidad;
import com.empresa.reembolsos.procesamiento.notificacion.NotificadorEmpleado;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

/** Flujo completo: API REST, cadena de validacion, calculo por categoria, persistencia H2 y observadores. */
@SpringBootTest
@AutoConfigureMockMvc
class ReembolsosIntegracionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper json;

    @MockitoSpyBean
    private NotificadorEmpleado empleado;

    @MockitoSpyBean
    private NotificadorContabilidad contabilidad;

    @MockitoSpyBean
    private NotificadorAuditoria auditoria;

    private String cuerpo(String empleadoId, String categoria, String monto, String fecha, int dias, boolean docs) {
        return """
                {"empleadoId":"%s","categoria":"%s","montoSolicitado":%s,"fechaGasto":"%s",
                 "diasViaje":%d,"documentosSoporte":%s}
                """.formatted(empleadoId, categoria, monto, fecha, dias, docs);
    }

    private long crear(String cuerpo) throws Exception {
        String respuesta = mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode nodo = json.readTree(respuesta);
        return nodo.get("id").asLong();
    }

    @Test
    void creaSolicitudesDeLasTresCategoriasConSuMontoAprobado() throws Exception {
        mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ANA", "VIAJE", "300000", "2026-03-10", 3, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoAprobado").value(300000))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
        mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ANA", "REPRESENTACION", "750000", "2026-03-10", 0, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoAprobado").value(750000));
        mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("ANA", "CAPACITACION", "1000000", "2026-03-10", 0, true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montoAprobado").value(800000.0));
    }

    @Test
    void rechazaUnaSolicitudSinDocumentosConMensajeClaro() throws Exception {
        mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("BETO", "VIAJE", "100000", "2026-03-10", 1, false)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Faltan documentos soporte"));
    }

    @Test
    void rechazaUnDuplicadoPendienteDelMismoEmpleadoCategoriaYPeriodo() throws Exception {
        crear(cuerpo("CARLA", "REPRESENTACION", "100000", "2026-04-05", 0, true));
        mockMvc.perform(post("/api/solicitudes").contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo("CARLA", "REPRESENTACION", "200000", "2026-04-20", 0, true)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ya existe una solicitud pendiente de la misma categoria en el periodo"));
    }

    @Test
    void aprobarDisparaALosTresObservadoresYContabilidadActua() throws Exception {
        long id = crear(cuerpo("DIEGO", "CAPACITACION", "1000000", "2026-05-02", 0, true));
        mockMvc.perform(put("/api/solicitudes/" + id + "/estado").contentType(MediaType.APPLICATION_JSON).content("\"APROBADA\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"));
        verify(empleado).onCambioEstado(any(), eq(EstadoSolicitud.PENDIENTE));
        verify(contabilidad).onCambioEstado(any(), eq(EstadoSolicitud.PENDIENTE));
        verify(auditoria).onCambioEstado(any(), eq(EstadoSolicitud.PENDIENTE));
    }

    @Test
    void rechazarLlegaALosTresObservadoresPeroSoloContabilidadFiltra() throws Exception {
        long id = crear(cuerpo("ELENA", "VIAJE", "100000", "2026-06-02", 1, true));
        mockMvc.perform(put("/api/solicitudes/" + id + "/estado").contentType(MediaType.APPLICATION_JSON).content("\"RECHAZADA\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("RECHAZADA"));
        verify(empleado).onCambioEstado(any(), eq(EstadoSolicitud.PENDIENTE));
        verify(auditoria).onCambioEstado(any(), eq(EstadoSolicitud.PENDIENTE));
        // Contabilidad recibe el evento (es un observador mas) pero su logica ignora lo que no es APROBADA;
        // eso se comprueba a nivel unitario en NotificadoresTest.
        verify(contabilidad).onCambioEstado(any(), eq(EstadoSolicitud.PENDIENTE));
    }

    @Test
    void cambiarEstadoDeUnaSolicitudInexistenteDevuelve404SinNotificar() throws Exception {
        mockMvc.perform(put("/api/solicitudes/987654/estado").contentType(MediaType.APPLICATION_JSON).content("\"APROBADA\""))
                .andExpect(status().isNotFound());
        verify(empleado, never()).onCambioEstado(any(), any());
    }
}
