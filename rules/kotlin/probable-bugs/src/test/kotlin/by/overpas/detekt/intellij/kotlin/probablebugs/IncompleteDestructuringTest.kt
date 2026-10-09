package by.overpas.detekt.intellij.kotlin.probablebugs

import dev.detekt.api.Config
import dev.detekt.test.lintWithContext
import dev.detekt.test.utils.createEnvironment
import kotlin.test.Test
import kotlin.test.assertEquals

class IncompleteDestructuringTest {

    private val environment = createEnvironment()

    private val sut = IncompleteDestructuring(Config.empty)

    @Test
    fun `a destructuring declaration with fewer entries than the data class components is reported`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            fun test(person: Person) {
                val (name) = person
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a destructuring declaration with a trailing comma and missing entries is reported`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            fun test(person: Person) {
                val (name,) = person
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a destructuring declaration with a typed entry and missing entries is reported`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            fun test(person: Person) {
                val (name: String) = person
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a destructuring declaration with an underscore and missing entries is reported`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            fun test(person: Person) {
                val (_, age) = person
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an incomplete destructuring lambda parameter is reported`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            fun test(persons: List<Person>) {
                persons.forEach { (name, age) -> }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an incomplete destructuring for loop parameter is reported`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            fun test(persons: List<Person>) {
                for ((name, age) in persons) {
                }
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an incomplete destructuring of a data class through a type alias is reported`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            typealias Human = Person

            fun test(human: Human) {
                val (name) = human
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `an incomplete destructuring declaration of a pair is reported`() {
        val code = """
            fun test(pair: Pair<String, Int>) {
                val (first) = pair
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(1, findings.size)
    }

    @Test
    fun `a complete destructuring declaration passes`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            fun test(person: Person) {
                val (name, age, address) = person
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a complete destructuring with underscores passes`() {
        val code = """
            data class Person(val name: String, val age: Int, val address: String)

            fun test(person: Person) {
                val (_, age, _) = person
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }

    @Test
    fun `a destructuring declaration of a class with component functions passes`() {
        val code = """
            class Point(val x: Int, val y: Int) {
                operator fun component1() = x
                operator fun component2() = y
            }

            fun test(point: Point) {
                val (x) = point
            }
        """.trimIndent()

        val findings = sut.lintWithContext(environment, code)

        assertEquals(0, findings.size)
    }
}
