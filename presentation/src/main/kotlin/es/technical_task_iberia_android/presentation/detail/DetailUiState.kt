package es.technical_task_iberia_android.presentation.detail

import es.technical_task_iberia_android.domain.model.Starship

/** What the detail screen shows. */
internal sealed interface DetailUiState {

    data object Loading : DetailUiState

    data class Content(val starship: Starship) : DetailUiState

    data class Error(val reason: DetailError) : DetailUiState
}

/** Typed failure; the screen turns it into a localised message. */
internal enum class DetailError {
    Connectivity,
    NotFound,
    Unexpected,
}
