package by.overpas.detekt.intellij.kotlin.javainterop.javacollectionwithnullabletypeargument

import org.jetbrains.kotlin.name.ClassId

internal val JAVA_COLLECTIONS_WITHOUT_NULL_SUPPORT = setOf(
    "java.util.concurrent.ConcurrentHashMap",
    "java.util.concurrent.ConcurrentSkipListMap",
    "java.util.concurrent.ConcurrentSkipListSet",
    "java.util.concurrent.ConcurrentLinkedQueue",
    "java.util.concurrent.ConcurrentLinkedDeque",
    "java.util.concurrent.ArrayBlockingQueue",
    "java.util.concurrent.BlockingQueue",
    "java.util.concurrent.LinkedTransferQueue",
    "java.util.concurrent.LinkedBlockingQueue",
    "java.util.concurrent.LinkedBlockingDeque",
    "java.util.concurrent.DelayQueue",
    "java.util.concurrent.PriorityBlockingQueue",
    "java.util.concurrent.SynchronousQueue",
    "java.util.concurrent.TransferQueue",
    "java.util.PriorityQueue",
)

internal class JavaCollectionWithNullableTypeArgumentUsage(
    private val collectionName: String,
    private val nullableTypeArgumentCount: Int,
) {

    val message: String
        get() {
            val nullability = if (nullableTypeArgumentCount > 1) "nullable types" else "a nullable type"
            return "Java collection '$collectionName' is parameterized with $nullability"
        }
}

internal fun ClassId.toNullableJavaCollectionUsage(
    nullableTypeArgumentCount: Int,
    collections: Set<String>,
): JavaCollectionWithNullableTypeArgumentUsage? =
    takeIf { asSingleFqName().asString() in collections }
        ?.run { JavaCollectionWithNullableTypeArgumentUsage(shortClassName.asString(), nullableTypeArgumentCount) }
