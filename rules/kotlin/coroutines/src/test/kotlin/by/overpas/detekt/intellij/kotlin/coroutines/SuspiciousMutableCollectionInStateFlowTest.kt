package by.overpas.detekt.intellij.kotlin.coroutines

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousMutableCollectionInStateFlowTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousMutableCollectionInStateFlow(Config.empty)

    @Test
    fun `a mutable list in a MutableStateFlow is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            val state = MutableStateFlow(mutableListOf<Int>())
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an ArrayList in a MutableStateFlow is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            val state = MutableStateFlow(ArrayList<Int>())
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mutable map in a MutableStateFlow is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            val state = MutableStateFlow(hashMapOf("a" to 1))
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a mutable set in a MutableStateFlow with a named argument is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            val state = MutableStateFlow(value = mutableSetOf("a"))
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a MutableCollection type argument is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            val state = MutableStateFlow<MutableCollection<Int>>(mutableListOf())
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a MutableStateFlow with an import alias is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow as MutableState

            val state = MutableState(mutableListOf<Int>())
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a variable that holds a mutable list is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            fun test() {
                val items = mutableListOf<Int>()
                val state = MutableStateFlow(items)
                println(state)
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a subtype of a mutable list is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            interface MyList : MutableList<Int>

            fun test(list: MyList) {
                val state = MutableStateFlow(list)
                println(state)
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a toMutableList call in a MutableStateFlow passed to a function is reported`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            fun takeState(state: MutableStateFlow<MutableList<Int>>) {
                println(state)
            }

            fun test(source: List<Int>) {
                takeState(MutableStateFlow(source.toMutableList()))
            }
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an Int in a MutableStateFlow passes`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            val state = MutableStateFlow(0)
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a read-only list in a MutableStateFlow passes`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            val state = MutableStateFlow(listOf(1, 2))
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a mutable list in a MutableStateFlow of a read-only list type passes`() {
        val code = """
            import kotlinx.coroutines.flow.MutableStateFlow

            val state: MutableStateFlow<List<Int>> = MutableStateFlow(mutableListOf())
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a MutableSharedFlow of a mutable list passes`() {
        val code = """
            import kotlinx.coroutines.flow.MutableSharedFlow

            val state = MutableSharedFlow<MutableList<Int>>(replay = 1)
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a user-defined MutableStateFlow function passes`() {
        val code = """
            class MyFlow<T>(val value: T)

            fun <T> MutableStateFlow(value: T): MyFlow<T> = MyFlow(value)

            val state = MutableStateFlow(mutableListOf<Int>())
        """.trimIndent()
        val coroutines = """
            package kotlinx.coroutines.flow

            interface Flow<out T>

            interface StateFlow<out T> : Flow<T>

            interface MutableStateFlow<T> : StateFlow<T>

            interface MutableSharedFlow<T> : Flow<T>

            fun <T> MutableStateFlow(value: T): MutableStateFlow<T> = TODO()

            fun <T> MutableSharedFlow(replay: Int = 0): MutableSharedFlow<T> = TODO()
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code, coroutines)

        assertEquals(0, findings.size)
    }
}
