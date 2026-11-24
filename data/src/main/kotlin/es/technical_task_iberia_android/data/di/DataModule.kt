package es.technical_task_iberia_android.data.di

import org.koin.dsl.module

/**
 * Koin definitions of the data layer: the only declaration `:app` may import from `:data`.
 *
 * The network client and the caching `StarshipRepository` are registered here as `single`
 * once they exist (search PR).
 */
val dataModule = module {
}
