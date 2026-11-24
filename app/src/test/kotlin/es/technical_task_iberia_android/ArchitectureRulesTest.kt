package es.technical_task_iberia_android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Small source-level checks for the two rules Gradle cannot enforce:
 * 1. only the composition root of `:app` references `:data`, and only `dataModule`;
 * 2. ViewModels reach data through use cases: no repository, no Koin lookups.
 */
class ArchitectureRulesTest {

    private val root: File = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "settings.gradle.kts").exists() }

    private fun kotlinSources(module: String): List<File> =
        File(root, "$module/src/main").walkTopDown().filter { it.extension == "kt" }.toList()

    private fun File.imports(): List<String> =
        readLines().map { it.trim() }.filter { it.startsWith("import ") }.map { it.removePrefix("import ") }

    @Test
    fun `sources are found`() {
        listOf("app", "presentation", "domain", "data").forEach { module ->
            assertTrue("No sources found in $module", kotlinSources(module).isNotEmpty())
        }
    }

    @Test
    fun `only the composition root of app imports data, and only dataModule`() {
        val dataImports = kotlinSources("app").flatMap { file ->
            file.imports().filter { it.startsWith(DataPackage) }.map { file.name to it }
        }

        assertEquals(
            listOf("StarshipsApplication.kt" to "$DataPackage.di.dataModule"),
            dataImports,
        )
    }

    @Test
    fun `presentation and domain never reference data`() {
        val offenders = (kotlinSources("presentation") + kotlinSources("domain"))
            .filter { file -> file.readText().contains(DataPackage) }
            .map { it.name }

        assertTrue("Data referenced from: $offenders", offenders.isEmpty())
    }

    @Test
    fun `app does not import domain`() {
        val offenders = kotlinSources("app").filter { file -> file.imports().any { it.startsWith(DomainPackage) } }
            .map { it.name }

        assertTrue("Domain imported by app: $offenders", offenders.isEmpty())
    }

    @Test
    fun `view models use only use cases and models from domain`() {
        val viewModels = kotlinSources("presentation").filter { it.name.endsWith("ViewModel.kt") }
        assertTrue("No ViewModels found", viewModels.isNotEmpty())

        viewModels.forEach { file ->
            val domainImports = file.imports().filter { it.startsWith(DomainPackage) }
            val notAllowed = domainImports.filterNot { it.startsWith("$DomainPackage.usecase.") || it.startsWith("$DomainPackage.model.") }
            assertTrue("${file.name} imports from domain: $notAllowed", notAllowed.isEmpty())

            val text = file.readText()
            ForbiddenInViewModels.forEach { forbidden ->
                assertTrue("${file.name} must not use $forbidden", !text.contains(forbidden))
            }
        }
    }

    private companion object {
        const val DataPackage = "es.technical_task_iberia_android.data"
        const val DomainPackage = "es.technical_task_iberia_android.domain"

        val ForbiddenInViewModels = listOf("StarshipRepository", "org.koin", "KoinComponent", "inject()", "get()")
    }
}
