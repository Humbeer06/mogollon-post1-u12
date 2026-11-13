package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Rechaza la solicitud si el empleado ya tiene otra pendiente de la misma categoria en el mismo periodo
 * (mes y anio del gasto).
 *
 * <p>No consulta la base de datos: recibe la lista de pendientes ya cargada por el servicio, por lo que el
 * paquete de validacion queda libre de dependencias de infraestructura.</p>
 */
@Component
public class ValidadorDuplicados extends ValidadorSolicitud {

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        YearMonth periodo = YearMonth.from(solicitud.getFechaGasto());
        boolean existeDuplicado = pendientes.stream()
                .anyMatch(p -> p.getCategoria() == solicitud.getCategoria()
                        && p.getFechaGasto() != null
                        && YearMonth.from(p.getFechaGasto()).equals(periodo));
        return existeDuplicado
                ? ResultadoValidacion.rechazar("Ya existe una solicitud pendiente de la misma categoria en el periodo")
                : ResultadoValidacion.ok();
    }
}
