package es.technical_task_iberia_android.data.cache

import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.Starship
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** How long a successful search stays valid, counted from the moment the response arrives. */
internal val SearchCacheTtl: Duration = 3.minutes

/**
 * In-memory results per normalised query.
 *
 * Time comes from an injected monotonic [timeSource], so tests move it without waiting.
 * An entry is valid while its age is strictly lower than [ttl].
 */
internal class SearchCache(
    private val timeSource: TimeSource,
    private val ttl: Duration = SearchCacheTtl,
) {
    private class Entry(val starships: List<Starship>, val storedAt: TimeMark)

    private val entries = ConcurrentHashMap<String, Entry>()

    /** The stored result for [query], or `null` when absent or expired. */
    fun get(query: SearchQuery): List<Starship>? {
        val key = query.cacheKey()
        val entry = entries[key] ?: return null
        if (entry.storedAt.elapsedNow() >= ttl) {
            entries.remove(key, entry)
            return null
        }
        return entry.starships
    }

    /** Stores [starships] (also an empty list) for [query], starting its lifetime now. */
    fun put(query: SearchQuery, starships: List<Starship>) {
        entries[query.cacheKey()] = Entry(starships, timeSource.markNow())
    }

    /** Same key for any letter case; outer spaces are already removed by [SearchQuery]. */
    private fun SearchQuery.cacheKey(): String = value.lowercase()
}
