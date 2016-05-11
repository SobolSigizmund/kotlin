package org.jetbrains.kotlin.gradle.util


// TODO: Use scan function from stdlib when available
fun <T: Comparable<T>> Iterable<T>.runningMaximum(): List<T> = zip(drop(1)) { a, b -> if (a > b) a else b }
