package com.empresa.reembolsos;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.empresa.reembolsos.procesamiento.calculo.CalculadoraReembolso;
import com.empresa.reembolsos.procesamiento.notificacion.ObservadorCambioEstado;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Reglas de arquitectura que verifican los limites que si existen en el sistema. Ninguna regla asume un
 * puerto de persistencia: es coherente con ADR-001 (capas simples).
 *
 * <p>Se analizan solo las clases de produccion ({@link ImportOption.DoNotIncludeTests}); de lo contrario las
 * clases de prueba que viven en los mismos paquetes (por ejemplo CalculadorasTest) violarian las reglas 3 y 4.</p>
 */
@AnalyzeClasses(packages = "com.empresa.reembolsos", importOptions = ImportOption.DoNotIncludeTests.class)
public class ReglasArquitectura {

    /** Regla 1: procesamiento no depende de infraestructura, adaptadores ni JPA / Spring Data. */
    @ArchTest
    static final ArchRule procesamientoNoDependeDePersistenciaNiDeLaCapaWeb = noClasses()
            .that().resideInAPackage("..procesamiento..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "..infraestructura..", "..adaptadores..",
                    "org.springframework.data..", "jakarta.persistence..");

    /** Regla 2: el controlador solo accede al servicio de aplicacion, al dominio y a la API web. */
    @ArchTest
    static final ArchRule controladorRestSoloUsaServicioDeAplicacionYDominio = classes()
            .that().resideInAPackage("..adaptadores.rest..")
            .should().onlyAccessClassesThat()
            .resideInAnyPackage(
                    "..adaptadores.rest..", "..aplicacion..", "..dominio..",
                    "org.springframework.web..", "org.springframework.http..", "java..");

    /** Regla 3: toda calculadora del paquete de calculo implementa CalculadoraReembolso. */
    @ArchTest
    static final ArchRule calculadorasImplementanLaInterfazDeEstrategia = classes()
            .that().resideInAPackage("..procesamiento.calculo..")
            .and().areNotInterfaces()
            .and().haveSimpleNameStartingWith("Calculadora")
            .should().implement(CalculadoraReembolso.class);

    /** Regla 4: todo notificador implementa ObservadorCambioEstado. */
    @ArchTest
    static final ArchRule notificadoresImplementanLaInterfazDeObservador = classes()
            .that().resideInAPackage("..procesamiento.notificacion..")
            .and().areNotInterfaces()
            .should().implement(ObservadorCambioEstado.class);
}
