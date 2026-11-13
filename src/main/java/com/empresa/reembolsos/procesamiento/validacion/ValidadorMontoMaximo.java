package com.empresa.reembolsos.procesamiento.validacion;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Verifica que el monto no exceda el tope de la categoria.
 *
 * <p>Para VIAJE el tope es diario, asi que el tope total es el tope diario multiplicado por los dias del
 * viaje (coherente con RF2). Para las demas categorias el tope es un monto fijo.</p>
 */
@Component
public class ValidadorMontoMaximo extends ValidadorSolicitud {

    private static final BigDecimal TOPE_DIARIO_VIAJE = new BigDecimal("150000");

    private static final Map<CategoriaGasto, BigDecimal> TOPES_FIJOS = Map.of(
            CategoriaGasto.REPRESENTACION, new BigDecimal("2000000"),
            CategoriaGasto.CAPACITACION, new BigDecimal("3000000"));

    @Override
    protected ResultadoValidacion validarPropio(SolicitudReembolso solicitud, List<SolicitudReembolso> pendientes) {
        if (solicitud.getCategoria() == null) {
            return ResultadoValidacion.rechazar("La categoria del gasto es obligatoria");
        }
        if (solicitud.getMontoSolicitado() == null || solicitud.getMontoSolicitado().signum() <= 0) {
            return ResultadoValidacion.rechazar("El monto solicitado debe ser mayor que cero");
        }
        BigDecimal tope = topeDe(solicitud);
        return solicitud.getMontoSolicitado().compareTo(tope) > 0
                ? ResultadoValidacion.rechazar("Monto excede el tope de la categoria " + solicitud.getCategoria())
                : ResultadoValidacion.ok();
    }

    private BigDecimal topeDe(SolicitudReembolso solicitud) {
        if (solicitud.getCategoria() == CategoriaGasto.VIAJE) {
            return TOPE_DIARIO_VIAJE.multiply(BigDecimal.valueOf(solicitud.getDiasViaje()));
        }
        return TOPES_FIJOS.get(solicitud.getCategoria());
    }
}
