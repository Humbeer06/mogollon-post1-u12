package com.empresa.reembolsos.procesamiento.calculo;

import com.empresa.reembolsos.dominio.CategoriaGasto;
import com.empresa.reembolsos.dominio.SolicitudReembolso;
import java.math.BigDecimal;

/**
 * Estrategia de calculo del monto aprobado (Strategy, RF2): una implementacion por categoria de gasto.
 * Agregar una categoria nueva es agregar una implementacion; las existentes no se tocan.
 */
public interface CalculadoraReembolso {

    /** Categoria de gasto que esta calculadora sabe resolver; es la clave de seleccion. */
    CategoriaGasto getCategoria();

    /** Calcula el monto que se aprueba para la solicitud segun la formula de la categoria. */
    BigDecimal calcular(SolicitudReembolso solicitud);
}
