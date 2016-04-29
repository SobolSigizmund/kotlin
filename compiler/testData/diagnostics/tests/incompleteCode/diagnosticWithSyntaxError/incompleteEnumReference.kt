// SKIP_TXT
enum class E {
    A,
    B,
    C
}

fun foo() {
    val e = E.<!SYNTAX!><!>
}


