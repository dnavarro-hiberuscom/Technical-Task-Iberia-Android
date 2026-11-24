package es.technical_task_iberia_android.presentation.di

import es.technical_task_iberia_android.domain.usecase.SearchStarships
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
    factoryOf(::SearchStarships)
    viewModelOf(::SearchViewModel)
}
