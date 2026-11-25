package es.technical_task_iberia_android

import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

/** Checks the root graph built from the real modules of `:data` and `:presentation`. */
class KoinGraphTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `root graph is complete`() {
        // DetailViewModel receives the starship id with parametersOf(id), not from the graph.
        module { includes(appModules) }.verify(extraTypes = listOf(Int::class))
    }
}
