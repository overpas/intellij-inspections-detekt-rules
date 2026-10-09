package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class ForEachJoinOnCollectionOfJobTest {

    private val environment = createEnvironment()

    private val sut = ForEachJoinOnCollectionOfJob(Config.empty)

    private val coroutines = """
        package kotlinx.coroutines

        interface Job {
            suspend fun join()
        }

        interface Deferred<out T> : Job {
            suspend fun await(): T
        }

        fun Job(): Job = TODO()
    """.trimIndent()

    @Test
    fun `forEach joining a list of jobs is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            suspend fun test(jobs: List<Job>) {
                jobs.forEach { it.join() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `forEach joining with a named parameter is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            suspend fun test(jobs: List<Job>) {
                jobs.forEach { job -> job.join() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `forEach joining on an implicit receiver is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            suspend fun List<Job>.test() {
                forEach { it.join() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `forEach joining a list of deferred values is reported`() {
        val code = """
            import kotlinx.coroutines.Deferred

            suspend fun test(jobs: List<Deferred<String>>) {
                jobs.forEach { it.join() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `forEach joining an array of jobs passes`() {
        val code = """
            import kotlinx.coroutines.Job

            suspend fun test(jobs: Array<Job>) {
                jobs.forEach { it.join() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `forEach with several statements passes`() {
        val code = """
            import kotlinx.coroutines.Job

            suspend fun test(jobs: List<Job>) {
                jobs.forEach {
                    println("Joining job")
                    it.join()
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `forEach with a call on the join result passes`() {
        val code = """
            import kotlinx.coroutines.Job

            suspend fun test(jobs: List<Job>) {
                jobs.forEach { it.join().toString() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `forEach joining another job passes`() {
        val code = """
            import kotlinx.coroutines.Job

            suspend fun test(jobs: List<Job>, otherJob: Job) {
                jobs.forEach { otherJob.join() }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
