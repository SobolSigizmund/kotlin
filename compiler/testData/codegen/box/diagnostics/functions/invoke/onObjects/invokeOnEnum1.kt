// SKIP_TXT
enum class A {
    ONE,
    TWO;

    operator fun invoke(i: Int) = i
}

fun box() = if (A.ONE(42) == 42) "OK" else "fail"
