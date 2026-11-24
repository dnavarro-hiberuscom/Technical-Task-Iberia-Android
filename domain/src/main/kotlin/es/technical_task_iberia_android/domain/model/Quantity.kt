package es.technical_task_iberia_android.domain.model

/**
 * A measured value that the source may not know.
 *
 * Keeps "unknown" apart from a real figure so the UI never shows a missing value as if it were data.
 */
sealed interface Quantity {

    /** A real figure, kept as published (e.g. `12.5`, `1,600` or `30-165`). */
    data class Known(val value: String) : Quantity

    /** The source does not know the value. */
    data object Unknown : Quantity
}
