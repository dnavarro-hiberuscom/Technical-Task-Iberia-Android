package es.technical_task_iberia_android.presentation.di

import org.koin.dsl.module

/**
 * Koin definitions of the presentation layer.
 *
 * Stateless use cases are registered as `factory` and ViewModels with `viewModelOf`, so `:domain`
 * stays free of Koin and `:app` never imports a use case.
 */
val presentationModule = module {
}
