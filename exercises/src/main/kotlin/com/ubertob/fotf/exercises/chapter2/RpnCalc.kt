package com.ubertob.fotf.exercises.chapter2

object RpnCalc {

    val operationsMap = mapOf<String, (Double, Double) -> Double>(
        "+" to Double::plus,
        "-" to Double::minus,
        "*" to Double::times,
        "/" to Double::div
    )

    val funStack = FunStack<Double>()

    fun calc(expr: String): Double =
        expr.split(" ")
            .fold(funStack, ::reduce)
            .pop().first


    private fun reduce(stack: FunStack<Double>, token: String): FunStack<Double> =
        if (operationsMap.containsKey(token)) {
            val (b, tempStack) = stack.pop()
            val (a, newStack) = tempStack.pop()
            newStack.push(operation(token, a, b))
        } else {
            stack.push(token.toDouble())
        }

    private fun operation(token: String, a: Double, b: Double) =
        operationsMap[token]?.invoke(a, b) ?: error("Unknown operation $token")

}
