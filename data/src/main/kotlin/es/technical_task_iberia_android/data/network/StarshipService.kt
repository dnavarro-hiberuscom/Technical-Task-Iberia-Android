package es.technical_task_iberia_android.data.network

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * SWAPI starships resource. Both calls are `suspend`, so cancelling the calling coroutine
 * cancels the HTTP request.
 */
internal interface StarshipService {

    /** `GET /api/starships/?search=…`: first page of matches. */
    @GET("starships/")
    suspend fun searchStarships(@Query("search") query: String): StarshipPageDto

    /** `GET /api/starships/{id}/`. */
    @GET("starships/{id}/")
    suspend fun getStarship(@Path("id") id: Int): StarshipDto
}

/** Production endpoint of the API. */
internal const val SwapiBaseUrl = "https://swapi.dev/api/"
