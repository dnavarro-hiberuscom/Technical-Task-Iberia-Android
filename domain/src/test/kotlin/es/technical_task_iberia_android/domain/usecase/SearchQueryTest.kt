package es.technical_task_iberia_android.domain.usecase

import es.technical_task_iberia_android.domain.model.SearchQuery
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SearchQueryTest {

    @Test
    fun `blank text is not a query`() {
        assertNull(SearchQuery.from(""))
        assertNull(SearchQuery.from("   "))
    }

    @Test
    fun `outer whitespace is removed and inner text is kept`() {
        assertEquals("death star", SearchQuery.from("  death star ")?.value)
    }
}
