package es.technical_task_iberia_android.domain.model

/**
 * Failures a [es.technical_task_iberia_android.domain.repository.StarshipRepository] may report.
 *
 * Implementations translate their own errors into these types so callers never depend on
 * networking classes. Cancellation is not a failure and is never wrapped.
 */
sealed class StarshipException(cause: Throwable? = null) : Exception(cause) {

    /** The source could not be reached. */
    class Connectivity(cause: Throwable? = null) : StarshipException(cause)

    /** The requested starship does not exist. */
    class NotFound(cause: Throwable? = null) : StarshipException(cause)

    /** The source answered with an error or with content that could not be read. */
    class Unexpected(cause: Throwable? = null) : StarshipException(cause)
}
