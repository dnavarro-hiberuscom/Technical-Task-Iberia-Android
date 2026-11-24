package es.technical_task_iberia_android.data.cache

import es.technical_task_iberia_android.domain.model.SearchQuery
import es.technical_task_iberia_android.domain.model.StarshipPage
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/** How long a successful search stays valid, counted from the moment the response arrives. */
internal val SearchCacheTtl: Duration = 3.minutes

/**
 * In-memory results per normalised query and page.
 *
 * Time comes from an injected monotonic [timeSource], so tests move it without waiting.
 * An entry is valid while its age is strictly lower than [ttl].
 */
internal class SearchCache(
    private val timeSource: TimeSource,
    private val ttl: Duration = SearchCacheTtl,
) {
    private data class Key(val query: String, val page: Int)
    private class Entry(val result: StarshipPage, val storedAt: TimeMark)

    private val entries = ConcurrentHashMap<Key, Entry>()

    /** The stored result for [query], or `null` when absent or expired. */
    fun get(query: SearchQuery, page: Int): StarshipPage? {
        val key = Key(query.value.lowercase(), page)
        val entry = entries[key] ?: return null
        if (entry.storedAt.elapsedNow() >= ttl) {
            entries.remove(key, entry)
            return null
        }
        return entry.result
    }

    /** Stores [result] (including an empty page), starting its lifetime now. */
    fun put(query: SearchQuery, page: Int, result: StarshipPage) {
        entries[Key(query.value.lowercase(), page)] = Entry(result, timeSource.markNow())
    }
}
