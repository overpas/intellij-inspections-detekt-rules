package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class SuspiciousCallOnCollectionToAddOrRemovePathTest {

    private val environment = createEnvironment()

    private val sut = SuspiciousCallOnCollectionToAddOrRemovePath(Config.empty)

    @Test
    fun `a path added to a list of paths is reported`() {
        val code = """
            import java.nio.file.Path

            fun test(list: List<Path>, path: Path) = list + path
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a path from a Java factory added to a list of paths is reported`() {
        val code = """
            import java.nio.file.Path

            fun test() = listOf(Path.of("/")) + Path.of("/a/b")
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a path removed from a list of paths is reported`() {
        val code = """
            import java.nio.file.Path

            fun test(list: List<Path>, path: Path) = list - path
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `non-operator plus and minus calls with a path are reported`() {
        val code = """
            import java.nio.file.Path

            fun test(list: List<Path>, path: Path) {
                list.plus(path)
                list.minus(path)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a path added to and removed from a sequence of paths is reported`() {
        val code = """
            import java.nio.file.Path

            fun test(seq: Sequence<Path>, path: Path) {
                seq + path
                seq - path
                seq.plus(path)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(3, findings.size)
    }

    @Test
    fun `a path added to a mutable collection, an iterable and a set of Any is reported`() {
        val code = """
            import java.nio.file.Path

            fun test(coll: MutableCollection<Path>, iterable: Iterable<Path>, set: Set<Any>, path: Path) {
                coll + path
                iterable + path
                set + path
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(3, findings.size)
    }

    @Test
    fun `a path added to a path is reported`() {
        val code = """
            import java.nio.file.Path

            fun test(path: Path, path2: Path) = path + path2
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a custom iterable of paths added to a list of paths is reported`() {
        val code = """
            import java.nio.file.Path

            class PathWrapper(val path: Path) : Iterable<Path> {
                override fun iterator(): Iterator<Path> = path.iterator()
            }

            fun test(list: List<Path>, seq: Sequence<Path>, wrapper: PathWrapper) {
                list + wrapper
                seq - wrapper
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(2, findings.size)
    }

    @Test
    fun `a custom iterable over itself added to a list is reported`() {
        val code = """
            class MyPath(val value: String) : Iterable<MyPath> {
                override fun iterator(): Iterator<MyPath> = value.split("/").map { MyPath(it) }.iterator()
            }

            fun test(list: List<MyPath>, path: MyPath) = list + path
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `collections and sequences added to each other pass`() {
        val code = """
            import java.nio.file.Path

            fun test(
                iterable: Iterable<String>,
                list: List<String>,
                paths: List<Path>,
                set: Set<Path>,
                seq1: Sequence<Path>,
                seq2: Sequence<Path>,
            ) {
                iterable + list
                list + iterable
                paths + set
                seq1 + paths
                seq1 + seq2
                seq1 - seq2
                seq1.plus(seq2)
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `plain elements added to lists pass`() {
        val code = """
            fun test(list: List<String>, ints: List<Int>, str: String) {
                list + str
                ints + 1
                list + "a"
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `an iterable over another type added to a list passes`() {
        val code = """
            class MyPath(val v: String) : Iterable<String> {
                override fun iterator(): Iterator<String> = v.split("/").iterator()
            }

            fun test(list: List<MyPath>, path: MyPath) = list + path
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a list of lists added to a list of lists passes`() {
        val code = """
            fun test(first: List<List<String>>, second: List<List<String>>) = first + second
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a list from an elvis expression added to a list passes`() {
        val code = """
            fun test(list: List<String>, nullableList: List<String>?) =
                list.filter { it.isNotEmpty() } + (nullableList ?: emptyList())
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
