package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutineContextWithJobTest {

    private val environment = createEnvironment()

    private val sut = CoroutineContextWithJob(Config.empty)

    private val coroutines = """
        package kotlinx.coroutines

        import kotlin.coroutines.AbstractCoroutineContextElement
        import kotlin.coroutines.ContinuationInterceptor
        import kotlin.coroutines.CoroutineContext
        import kotlin.coroutines.EmptyCoroutineContext

        interface Job : CoroutineContext.Element {
            companion object Key : CoroutineContext.Key<Job>

            suspend fun join()
        }

        fun Job(parent: Job? = null): Job = TODO()

        object NonCancellable : AbstractCoroutineContextElement(Job), Job {
            override suspend fun join() {}
        }

        abstract class CoroutineDispatcher : AbstractCoroutineContextElement(ContinuationInterceptor)

        object Dispatchers {
            val IO: CoroutineDispatcher = TODO()
        }

        interface CoroutineScope {
            val coroutineContext: CoroutineContext
        }

        interface Deferred<out T> : Job

        fun CoroutineScope.launch(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> Unit,
        ): Job = TODO()

        fun <T> CoroutineScope.async(
            context: CoroutineContext = EmptyCoroutineContext,
            block: suspend CoroutineScope.() -> T,
        ): Deferred<T> = TODO()

        suspend fun <T> withContext(context: CoroutineContext, block: suspend CoroutineScope.() -> T): T = TODO()

        suspend fun currentCoroutineContext(): CoroutineContext = TODO()
    """.trimIndent()

    private val flow = """
        package kotlinx.coroutines.flow

        import kotlin.coroutines.CoroutineContext

        interface Flow<out T>

        fun <T> Flow<T>.flowOn(context: CoroutineContext): Flow<T> = this
    """.trimIndent()

    @Test
    fun `a job factory call passed to launch is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.launch

            fun CoroutineScope.test() {
                launch(Job()) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a job parameter passed to withContext is reported`() {
        val code = """
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.withContext

            suspend fun test(job: Job) {
                withContext(job) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a job factory call passed to flowOn is reported`() {
        val code = """
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flowOn

            fun test(flow: Flow<Int>) {
                flow.flowOn(Job())
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines, flow)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the scope coroutine context passed to launch is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.launch

            fun CoroutineScope.test() {
                launch(coroutineContext) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the current coroutine context passed to async is reported`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.async
            import kotlinx.coroutines.currentCoroutineContext

            suspend fun CoroutineScope.test() {
                async(currentCoroutineContext()) { 42 }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the top-level coroutine context passed to launch is reported`() {
        val code = """
            import kotlin.coroutines.coroutineContext
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.launch

            suspend fun test(scope: CoroutineScope) {
                scope.launch(coroutineContext) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a job added to an empty context is reported`() {
        val code = """
            import kotlin.coroutines.EmptyCoroutineContext
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.launch

            fun CoroutineScope.test() {
                launch(EmptyCoroutineContext + Job()) {}
                launch(EmptyCoroutineContext.plus(Job())) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a job combined with a final context element is reported`() {
        val code = """
            import kotlin.coroutines.AbstractCoroutineContextElement
            import kotlin.coroutines.CoroutineContext
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.launch

            class MyElement : AbstractCoroutineContextElement(Companion) {
                companion object : CoroutineContext.Key<MyElement>
            }

            fun CoroutineScope.test(job: Job) {
                launch(job + MyElement()) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `the scope coroutine context without an unrelated key is reported`() {
        val code = """
            import kotlin.coroutines.CoroutineContext
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.launch

            interface NonJob : CoroutineContext.Element {
                companion object Key : CoroutineContext.Key<NonJob>
            }

            fun CoroutineScope.test() {
                launch(coroutineContext.minusKey(NonJob)) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `NonCancellable passed to launch is reported`() {
        val code = """
            import kotlin.coroutines.CoroutineContext
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.NonCancellable
            import kotlinx.coroutines.launch

            fun CoroutineScope.test(context: CoroutineContext) {
                launch(NonCancellable + context) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a job added after NonCancellable in withContext is reported`() {
        val code = """
            import kotlin.coroutines.EmptyCoroutineContext
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.NonCancellable
            import kotlinx.coroutines.withContext

            suspend fun test(job: Job) {
                withContext(EmptyCoroutineContext + NonCancellable + job) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `NonCancellable passed to withContext or flowOn passes`() {
        val code = """
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.NonCancellable
            import kotlinx.coroutines.flow.Flow
            import kotlinx.coroutines.flow.flowOn
            import kotlinx.coroutines.withContext

            suspend fun test(job: Job, flow: Flow<Int>) {
                withContext(NonCancellable) {}
                withContext(NonCancellable + Dispatchers.IO) {}
                withContext(job + (Dispatchers.IO + NonCancellable)) {}
                flow.flowOn(NonCancellable)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines, flow)

        assertEquals(0, findings.size)
    }

    @Test
    fun `the scope coroutine context without the job key passes`() {
        val code = """
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Job
            import kotlinx.coroutines.launch

            fun CoroutineScope.test() {
                launch(coroutineContext.minusKey(Job)) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `contexts without a job pass`() {
        val code = """
            import kotlin.coroutines.CoroutineContext
            import kotlin.coroutines.EmptyCoroutineContext
            import kotlinx.coroutines.CoroutineScope
            import kotlinx.coroutines.Dispatchers
            import kotlinx.coroutines.launch

            fun CoroutineScope.test(context: CoroutineContext) {
                launch(EmptyCoroutineContext) {}
                launch(EmptyCoroutineContext + EmptyCoroutineContext) {}
                launch(Dispatchers.IO) {}
                launch(context) {}
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
