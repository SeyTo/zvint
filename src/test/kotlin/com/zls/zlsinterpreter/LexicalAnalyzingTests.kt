package com.zls.zlsinterpreter

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.Error

class LexicalAnalyzingTests {

    private val codes = arrayListOf(
            "hello.test.go",
            "if (true == false) a elif(true == false) 'b' else 'go'",
            "if (true == false) a else 'b'",
            "a",
            "a[10]",
            "b ?: a",
            "b != a",
            "(true == false)",
            "null ?: a",
            "'10'",
            "true"
    )

    private val globalContext = Context()

    @Before
    fun before() {
        globalContext.poolVariables["a"] = "hello"
        globalContext.poolVariables["hello"] = "world"
    }

    @Test
    fun functionSyntaxTest() {
        val code = "'helloApi'(a, '10')"
        val tokens = LexicalAnalyzer.eval(code)

        println("Hello")
        assertEquals(
            "evaluated function token should be equivalent",
            Token(TokenGen.FUNCTION, arrayListOf(
                Token(TokenGen.STR, "'helloApi'"),
                Token(TokenGen.VAR, "a"),
                Token(TokenGen.STR, "'10'")
            )),
            tokens[0]
        )
    }

    @Test
    fun functionTest2() {
        val code = "helloApi('10')"
        val tokens = LexicalAnalyzer.eval(code)

        assertEquals(
            "token should be valid",
            Token(TokenGen.FUNCTION, arrayListOf(
                Token(TokenGen.VAR, "helloApi"),
                Token(TokenGen.STR, "'10'")
            )),
            tokens[0]
        )
    }

    @Test
    fun basicTest() {
        val tokens = LexicalAnalyzer.eval("a")

        assertEquals(
            "token should be valid",
            Token(TokenGen.VAR, "a"),
            tokens[0]
        )
    }

    @Test
    fun stringLATest() {
        val tokens = LexicalAnalyzer.eval("'10'")

        assertEquals(
            "token should be string",
            Token(TokenGen.STR, "'10'"),
            tokens[0]
        )
    }

    @Test
    fun ifLATest2() {
        // IMPORTANT FIX is needed: a space was needed in Token(TokenGen.STR, " 'bb'"), fix it.
        val tokens = LexicalAnalyzer.eval("if (a == b) 'bb' else a")

        assertEquals(
            "token should be 'if'",
            Token(TokenGen.IF_COMPLEX, arrayListOf(
                Token(TokenConditional.IF_CONDITIONAL, arrayListOf(
                    Token(TokenGen.COMPLEX,
                        Token(TokenGen.CONDITIONAL, arrayListOf(
                            Token(TokenGen.VAR, "a"),
                            Token(TokenConditional.EQUALS, "EQUALS"),
                            Token(TokenGen.VAR, "b")
                        ))
                    ),
                    Token(TokenGen.STR, " 'bb'")
                )),
                Token(TokenConditional.ELSE_VALUE, arrayListOf(
                    Token(TokenGen.VAR, "a")
                )))
            ),
            tokens[0]
        )
    }

    @Test
    fun trueTest() {
        val tokens = LexicalAnalyzer.eval("true")

        assertEquals(
            "token should be boolean",
            Token(TokenGen.BOOLEAN, "true"),
            tokens[0]
        )
    }

    /**
     * Test array types inside the pool.
     */
    @Test
    fun arrayPoolTest() {
        val code = "v[2]"

        val globalContext = Context().apply {
            poolVariables["v"] = arrayListOf("a1", "a2", "a3")
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

        Assert.assertTrue(
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