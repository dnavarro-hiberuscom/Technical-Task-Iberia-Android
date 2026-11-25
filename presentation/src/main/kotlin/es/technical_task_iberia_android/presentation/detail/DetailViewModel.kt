package es.technical_task_iberia_android.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.technical_task_iberia_android.domain.model.StarshipException
import es.technical_task_iberia_android.domain.usecase.GetStarshipUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** State holder of the detail screen for the starship [starshipId]. */
internal class DetailViewModel(
    private val starshipId: Int,
    private val getStarshipUseCase: GetStarshipUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    /** Tries again after a failure. */
    fun onRetry() = load()

    private fun load() {
        loadJob?.cancel()
        _uiState.value = DetailUiState.Loading
        loadJob = viewModelScope.launch {
            _uiState.value = try {
                DetailUiState.Content(getStarshipUseCase(starshipId))
            } catch (e: CancellationException) {
                throw e
            } catch (e: StarshipException) {
                DetailUiState.Error(e.toDetailError())
            }
        }
    }

    private fun StarshipException.toDetailError(): DetailError = when (this) {
        is StarshipException.Connectivity -> DetailError.Connectivity
        is StarshipException.NotFound -> DetailError.NotFound
        is StarshipException.Unexpected -> DetailError.Unexpected
    }
}
