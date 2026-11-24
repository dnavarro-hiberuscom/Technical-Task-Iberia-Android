package es.technical_task_iberia_android.domain.model

const val FirstSearchPage = 1

data class StarshipPage(
    val starships: List<Starship>,
    val nextPage: Int? = null,
)
