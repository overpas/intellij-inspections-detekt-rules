package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousGetterForMutableObjectTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousGetterForMutableObject(Config.empty)

    @Test
    fun `a getter returning a new Job is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            class Service {
                val job get() = Job()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a block getter returning a new Job is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            class Service {
                val job: Job
                    get() {
                        return Job()
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter returning a new Job with a parent is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            class Service(private val parent: Job) {
                val job get() = Job(parent)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an overriding getter returning a new Job is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            interface HasJob {
                val job: Job
            }

            class Service : HasJob {
                override val job: Job get() = Job()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an extension property getter returning a new Job is reported`() {
        val code = """
            import kotlinx.coroutines.Job

            val String.job get() = Job()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter returning a new MutableStateFlow is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            class Service {
                val state get() = MutableStateFlow(0)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter returning a new Mutex is reported`() {
        val code = """
            import kotlinx.coroutines.sync.Mutex

            class Service {
                val lock get() = Mutex()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter returning a new Channel is reported`() {
        val code = """
            import kotlinx.coroutines.channels.Channel

            class Service {
                val channel get() = Channel<Int>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter returning a new empty mutable list is reported`() {
        val code = """
            class Service {
                val items get() = mutableListOf<String>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter returning a new empty hash map is reported`() {
        val code = """
            class Service {
                val items get() = hashMapOf<String, Int>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a getter returning a mutable list with elements passes`() {
        val code = """
            class Service {
                val items get() = mutableListOf("a", "b")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an overriding getter returning a new mutable list passes`() {
        val code = """
            interface HasItems {
                val items: MutableList<String>
            }

            class Service : HasItems {
                override val items: MutableList<String> get() = mutableListOf()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter returning a read-only list passes`() {
        val code = """
            class Service {
                val items get() = listOf("a")
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter calling a Java collection constructor passes`() {
        val code = """
            class Service {
                val items get() = ArrayList<String>()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter calling another function passes`() {
        val code = """
            import kotlinx.coroutines.Job

            class Service {
                val job get() = createJob()

                private fun createJob() = Job()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a getter with two statements passes`() {
        val code = """
            import kotlinx.coroutines.Job

            class Service {
                val job: Job
                    get() {
                        println("creating a job")
                        return Job()
                    }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a property initializer passes`() {
        val code = """
            import kotlinx.coroutines.Job

            class Service {
                val job = Job()
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
