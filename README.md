# reembolsos-integrado — Sistema de Solicitudes de Reembolso de Gastos Corporativos

Proyecto integrador de cierre (Unidad 12, Patrones de Diseño de Software). Spring Boot 3 / Java 17 / Maven. El área de Finanzas reemplaza su hoja de cálculo por un sistema que valida, calcula y notifica solicitudes de reembolso.

## Diseño Propuesto

Análisis hecho **antes de escribir código de patrones**. Para cada requisito se identifica qué varía, qué patrón lo resuelve y por qué ese y no otro (test de explicabilidad: si no cabe en una oración, el patrón probablemente sobra).

| Requisito | Qué varía (punto de variabilidad) | Patrón | Justificación en una oración |
|---|---|---|---|
| **RF1** Validación en varias etapas | El **conjunto y el orden de verificaciones** previas a la aprobación: hoy 3, y Finanzas ya anunció una cuarta (límite anual acumulado) | **Chain of Responsibility** | Cada verificación decide por sí misma si rechaza o pasa a la siguiente sin conocer a las demás, así que agregar el límite anual es agregar un eslabón sin modificar los existentes. |
| **RF2** Cálculo distinto por categoría | La **fórmula del monto aprobado**, distinta para viáticos, representación y capacitación, y con categorías nuevas previstas (bienestar) | **Strategy** (con selección por categoría) | Son fórmulas intercambiables detrás de una misma interfaz que se elige según un dato (la categoría), así que una categoría nueva es una clase nueva y no un `case` más en un método existente. |
| **RF3** Notificación simultánea | **Quién se entera y cómo reacciona** ante un cambio de estado: empleado (todo), Contabilidad (solo aprobaciones), auditoría (todo) y pronto un panel de métricas | **Observer** | Los interesados reaccionan de forma distinta al mismo evento y su número crece, así que el servicio publica el cambio a una lista de observadores sin conocer a ninguno en concreto. |

**Por qué no otro patrón.**

- **RF1.** *Strategy* elige una sola variante; aquí se aplican todas en secuencia y la primera que falla corta el proceso. Un `if` encadenado dentro del servicio obliga a editar ese método con cada verificación nueva. Una `List<Validador>` recorrida con un `for` sería la versión mínima equivalente; se prefiere la cadena porque el corte vive en cada eslabón (no en el orquestador) y el enunciado describe *etapas* encadenadas.
- **RF2.** Un `switch` sobre la categoría obliga a modificar código ya escrito con cada categoría nueva, que es justo lo que Finanzas pide evitar. Una jerarquía por herencia (*Template Method*) no aporta: las tres fórmulas no comparten un esqueleto de pasos, solo un resultado.
- **RF3.** Llamar a cada notificador desde el servicio acopla el servicio a cada interesado y exige editarlo con cada uno nuevo. Un bus de eventos de Spring es más indirección de la necesaria para tres observadores fijos (ver «Qué se descartó y por qué»).

No se propone ningún patrón adicional: ningún requisito de la especificación lo pide.
