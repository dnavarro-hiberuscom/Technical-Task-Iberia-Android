package es.technical_task_iberia_android.domain.usecase

import es.technical_task_iberia_android.domain.model.Starship
import es.technical_task_iberia_android.domain.repository.StarshipRepository

/** Loads one starship by its id. Failures are reported as [es.technical_task_iberia_android.domain.model.StarshipException]. */
class GetStarshipUseCase(private val repository: StarshipRepository) {

    suspend operator fun invoke(id: Int): Starship = repository.getStarship(id)
}
