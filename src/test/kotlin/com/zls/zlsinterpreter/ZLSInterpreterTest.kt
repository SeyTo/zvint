package com.zls.zlsinterpreter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.Error

class ZLSInterpreterTest {

    @Test
    fun arrayPoolTest() {
        val code = "v[2]"

        val globalContext = Context().apply {
            poolVariables["v"] = arrayListOf(
                "a1",
                "a2",
                "a3"
            )
        }

        val interpreter = ZLSInterpreter(globalContext)
        var value = interpreter.interpret(code)

        assertEquals(
            "should get array variable",
            (globalContext.poolVariables["v"] as ArrayList<*>)[2],
            value
        )

        value = interpreter.interpret("v[0]")

        assertEquals(
            "should get array variable",
            (globalContext.poolVariables["v"] as ArrayList<*>)[0],
            value
        )
    }

    @Test
    fun callbackTest() {
        val code = "value"

        val globalContext = Context().apply {
            eval = object : Evaluator {
                override fun eval(vararg variable: Any): Any? {
                    return when {
                        variable[0] == "value" -> {
                            object: InterpreterCallback {
                                override fun call(): Any? = "hello"
                            }
                        }
                        else -> {
                            throw Error("Not handled")
                        }
                    }
                }
            }
        }

        val interpreter = ZLSInterpreter(globalContext)
        val value = interpreter.interpret(code)

        assertTrue(
            "should get callback function",
            value is InterpreterCallback
        )

        assertEquals(
            "should get proper value",
            "hello",
            (value as InterpreterCallback).call()
        )
    }
}
