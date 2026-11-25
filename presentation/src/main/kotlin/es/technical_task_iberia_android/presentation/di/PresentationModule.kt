package es.technical_task_iberia_android.presentation.di

import es.technical_task_iberia_android.domain.usecase.GetStarshipUseCase
import es.technical_task_iberia_android.domain.usecase.SearchStarshipsUseCase
import es.technical_task_iberia_android.presentation.detail.DetailViewModel
import es.technical_task_iberia_android.presentation.search.SearchViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Koin definitions of the presentation layer.
 *
 * Stateless use cases are `factory` and ViewModels use `viewModelOf`, so `:domain` stays free of
 * Koin and `:app` never imports a use case.
 */
val presentationModule = module {
    factoryOf(::SearchStarshipsUseCase)
    factoryOf(::GetStarshipUseCase)
    viewModelOf(::SearchViewModel)
    // The starship id is passed with parametersOf(id) when the screen asks for the ViewModel.
    viewModelOf(::DetailViewModel)
}
