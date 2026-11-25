package es.technical_task_iberia_android.presentation.search

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Quiet time after the last keystroke before a search starts. Waited inside the cancellable block.
 *
 * It exists to save API calls and avoid overloading the server (one request per keystroke while
 * typing), not to hide latency: the endpoint is fast, and an in-flight request is cancelled by
 * `collectLatest` as soon as the text changes, with or without this wait.
 */
internal val SearchDebounce: Duration = 300.milliseconds
