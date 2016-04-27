class My(val x: Boolean?)

fun foo(my: My): Boolean {
    return <!TYPE_MISMATCH!>my.x<!>
}

fun bar(x: Boolean?): Boolean {
    return <!TYPE_MISMATCH!>x<!>
}