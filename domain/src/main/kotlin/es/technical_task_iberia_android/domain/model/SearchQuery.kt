package es.technical_task_iberia_android.domain.model

/**
 * A search text that is ready to be sent: trimmed and never blank.
 *
 * The only way to build one is [from], so the empty-query and text-cleaning rules live in one place.
 */
@JvmInline
value class SearchQuery private constructor(val value: String) {

    companion object {
        /** Returns the cleaned query, or `null` when [raw] is blank and nothing should be searched. */
        fun from(raw: String): SearchQuery? = raw.trim().takeIf { it.isNotEmpty() }?.let(::SearchQuery)
    }
}
