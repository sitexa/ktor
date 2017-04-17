package test

import java.util.Arrays
import javax.print.attribute.IntegerSyntax

/**
 * Created by open on 06/04/2017.
 *
 */


fun main(vararg: Array<String>) {
    //aList()
    rs()
    //r()
    //println(sum(2, 3))
    //println(c())
    //s("me")
    //c()
    //s("me again")
    //th()
    println(sum(1, 2))
    println(sum1(2, 3))
}

fun aList() {
    val list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
    list.forEach(::println)
}

fun rs() {
    val r = { println("hello world")}
}

fun th() {
    Thread { println("Hello from thread") }.start()
}

val r = { println("hello again") }

val sum: (a: Int, b: Int) -> Int = { a, b -> a + b }

val sum1 = { a: Int, b: Int -> a + b }

val c = { 42 }

val s = { s1: String -> println(s1) }

val p = { x: Int -> println(x) }

val pi = { 3.1415 }