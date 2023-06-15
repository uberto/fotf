package com.ubertob.fotf.exercises.chapter10

import com.ubertob.fotf.exercises.chapter2.RpnCalc
import com.ubertob.fotf.exercises.chapter9.ContextReader


fun main() {
    consoleRpnCalculator().runWith(SystemConsole())
}

tailrec fun consoleRpnCalculator(): ContextReader<ConsoleContext, String> =
    contextPrintln("Write an RPN expression to calculate the result or Q to quit.")
        .bind { _ -> contextReadln() }
        .bind { input ->
            if (input == "Q")
                contextPrintln("Bye!")
            else
                contextPrintln("The result is: ${RpnCalc.calc(input)}")
        }
        .bind { msg ->
            if (msg == "Bye!")
                contextPrintln("")
            else
                consoleRpnCalculator()
        }



