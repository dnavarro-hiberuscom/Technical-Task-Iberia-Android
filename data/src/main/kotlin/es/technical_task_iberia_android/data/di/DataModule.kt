package es.technical_task_iberia_android.data.di

import retrofit2.converter.kotlinx.serialization.asConverterFactory
import es.technical_task_iberia_android.data.cache.SearchCache
import es.technical_task_iberia_android.data.network.StarshipService
import es.technical_task_iberia_android.data.network.SwapiBaseUrl
import es.technical_task_iberia_android.data.repository.StarshipRepositoryImpl
import es.technical_task_iberia_android.domain.repository.StarshipRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import kotlin.time.TimeSource

private const val JsonMediaType = "application/json"

/**
 * Koin definitions of the data layer: the only declaration `:app` may import from `:data`.
 *
 * The network client and the caching repository are `single`, so the cache lives as long as the app.
 */
val dataModule = module {
    single { OkHttpClient() }
    single {
        Retrofit.Builder()
            .baseUrl(SwapiBaseUrl)
            .client(get<OkHttpClient>())
            .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory(JsonMediaType.toMediaType()))
            .build()
            .create(StarshipService::class.java)
    }
    single<StarshipRepository> {
        StarshipRepositoryImpl(
            service = get(),
            cache = SearchCache(timeSource = TimeSource.Monotonic),
        )
    }
}
