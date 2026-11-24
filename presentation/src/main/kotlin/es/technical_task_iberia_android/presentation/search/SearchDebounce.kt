package es.technical_task_iberia_android.presentation.search

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/** Quiet time after the last keystroke before a search starts. Waited inside the cancellable block. */
internal val SearchDebounce: Duration = 300.milliseconds
