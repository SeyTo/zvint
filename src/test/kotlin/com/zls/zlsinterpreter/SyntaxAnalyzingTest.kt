package com.zls.zlsinterpreter

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyntaxAnalyzingTest {

    private val context = Context()

    @Before
    fun before() {
        context.apply {
            poolVariables["a"] = "hello"
            poolVariables["b"] = "null"
            poolVariables["array2"] = arrayListOf("size a", "size b", "size c", "size d")
            poolVariables["empty"] = ""
            eval = object : Evaluator {
                override fun eval(vararg variable: Any): Any? {
                    return when {
                        variable.size == 1 && variable[0] == "app.user.const" -> {
                            "USER_CONSTANT_DATA"
                        }
                        variable.size == 2 && variable[1].toString().toIntOrNull() != null -> {
                            when (variable[0].toString()) {
                                "array" -> {
                                    (poolVariables["array2"] as ArrayList<*>)[variable[1].toString().toInt()] as String
                                }
                                else -> {
                                    (poolVariables[variable[0].toString()] as ArrayList<*>)[variable[1].toString().toInt()] as String
                                }
                            }
                        }
                        (variable[0] as InterpreterResultType) == InterpreterResultType.FUNCTION -> {
                            when (variable[1]) {
                                "helloApi" -> {
                                    object : InterpreterCallback {
                                        override fun call(): Any? = "hello api value"
                                    }
                                }
                                "hello" -> {
                                    object: InterpreterCallback {
                                        override fun call(): Any? = "hello api value"
                                    }
                                }
                                "a" -> {
                                    object: InterpreterCallback {
                                        override fun call(): Any? = "hello api value"
                                    }
                                }
                                else -> {
                                    "null"
                                }
                            }
                        }
                        else -> {
                            "null"
                        }
                    }
                }
            }
        }
    }

    @Test
    fun conditionalTest() {
        // "null ?: a",
        var token = Token(TokenGen.CONDITIONAL, arrayListOf(
            Token(TokenGen.NULL, "null"),
            Token(TokenConditional.IF_NULL, TokenConditional.IF_NULL.name),
            Token(TokenGen.VAR, "a")
        ))
        var expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            "conditional with null should evaluate to false",
            "hello",
            expr
        )

        // "b ?: a",
        token = Token(TokenGen.CONDITIONAL, arrayListOf(
            Token(TokenGen.VAR, "b"),
            Token(TokenConditional.IF_NULL, TokenConditional.IF_NULL.name),
            Token(TokenGen.VAR, "a")
        ))
        expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            "conditional should evaluate to value of a",
            "hello",
            expr
        )

        // "b != a",
        token = Token(TokenGen.CONDITIONAL, arrayListOf(
            Token(TokenGen.VAR, "b"),
            Token(TokenConditional.NOT_EQUALS, TokenConditional.NOT_EQUALS.name),
            Token(TokenGen.VAR, "a")
        ))
        expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            "conditional should evaluate to true",
            "true",
            expr
        )

        // "(true == false)",
        token = Token(TokenGen.CONDITIONAL, arrayListOf(
            Token(TokenGen.BOOLEAN, "true"),
            Token(TokenConditional.EQUALS, TokenConditional.EQUALS.name),
            Token(TokenGen.BOOLEAN, "true")
        ))
        expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            "conditional should evaluate to true",
            "true",
            expr
        )


    }

    @Test
    fun ifComplexTest() {
        val token = Token(TokenGen.IF_COMPLEX, arrayListOf(
            Token(TokenConditional.IF_CONDITIONAL,
                arrayListOf(
                    Token(TokenGen.COMPLEX,
                        Token(TokenGen.CONDITIONAL, arrayListOf(
                            Token(TokenGen.VAR, "a"),
                            Token(TokenConditional.EQUALS, TokenConditional.EQUALS.name),
                            Token(TokenGen.STR, "'hello'")
                        ))
                    ),
                    Token(TokenGen.VAR, "a")
                )
            ),
            Token(TokenConditional.ELSE_IF_CONDITIONAL,
                arrayListOf(
                    Token(TokenGen.COMPLEX,
                        Token(TokenGen.CONDITIONAL, arrayListOf(
                            Token(TokenGen.BOOLEAN, "true"),
                            Token(TokenConditional.EQUALS, TokenConditional.EQUALS.name),
                            Token(TokenGen.BOOLEAN, "false")
                        ))
                    ),
                    Token(TokenGen.STR, "'b'")
                )
            ),
            Token(TokenConditional.ELSE_VALUE,
                arrayListOf(
                    Token(TokenGen.STR, "'c'")
                )
            )
        ))
        val expr = ValueAnalyzer(context).eval(arrayListOf(token))

        assertEquals(
            "token should be valid",
            "hello",
            expr
        )

    }

    @Test
    fun ifElseComplexTest() {
        val token = Token(TokenGen.IF_COMPLEX, arrayListOf(
            Token(TokenConditional.IF_CONDITIONAL,
                arrayListOf(
                    Token(TokenGen.COMPLEX,
                        Token(TokenGen.CONDITIONAL, arrayListOf(
                            Token(TokenGen.VAR, "a"),
                            Token(TokenConditional.EQUALS, TokenConditional.EQUALS.name),
                            Token(TokenGen.BOOLEAN, "false")
                        ))
                    ),
                    Token(TokenGen.VAR, "a")
                )
            ),
            Token(TokenConditional.ELSE_IF_CONDITIONAL,
                arrayListOf(
                    Token(TokenGen.COMPLEX,
                        Token(TokenGen.CONDITIONAL, arrayListOf(
                            Token(TokenGen.BOOLEAN, "true"),
                            Token(TokenConditional.EQUALS, TokenConditional.EQUALS.name),
                            Token(TokenGen.BOOLEAN, "true")
                        ))
                    ),
                    Token(TokenGen.STR, "'b'")
                )
            ),
            Token(TokenConditional.ELSE_VALUE,
                arrayListOf(
                    Token(TokenGen.STR, "'c'")
                )
            )
        ))
        val expr = ValueAnalyzer(context).eval(arrayListOf(token))

        assertEquals(
            "token should be valid",
            "b",
            expr
        )

    }

    @Test
    fun varTest() {
        var token = Token(TokenGen.VAR, "a")
        var expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            context.poolVariables["a"],
            expr
        )

        token = Token(TokenGen.VAR, "app.user.const")
        expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            context.eval?.eval("app.user.const") as String,
            expr
        )
    }

    @Test
    fun literalTests() {
        var token = Token(TokenGen.STR, "'10'")
        var expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            "10",
            expr
        )

        token = Token(TokenGen.BOOLEAN, "true")
        expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            "true",
            expr
        )

        token = Token(TokenGen.CONDITIONAL, arrayListOf(
            Token(TokenGen.VAR, "empty"),
            Token(TokenConditional.EQUALS, TokenConditional.EQUALS.name),
            Token(TokenGen.STR, "''")
        ))
        expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            "true",
            expr
        )
    }

    @Test
    fun testArray() {
        var token = Token(TokenGen.ARR_VAR, "array[1]")
        var expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String

        assertEquals(
            "Should get array data from eval",
            context.eval?.eval("array", "1") as String,
            expr
        )

        token = Token(TokenGen.ARR_VAR, "array2[2]")
        expr = ValueAnalyzer(context).eval(arrayListOf(token)) as String
        assertEquals(
            "Should get array data from pool",
            context.eval?.eval("array2", "2") as String,
            expr
        )
    }

    @Test
    fun functionCallTest() {
        val token = Token(TokenGen.FUNCTION, arrayListOf(
            Token(TokenGen.STR, "'helloApi'"),
            Token(TokenGen.VAR, "a"),
            Token(TokenGen.STR, "'10'")
        ))

        val expr = ValueAnalyzer(context).eval(arrayListOf(token)) as InterpreterCallback
        val value = expr.call()

        assertEquals(
            "should get function value",
            "hello api value",
            value
        )



    }

    @Test
    fun functionCallTest2() {
        val token = Token(TokenGen.FUNCTION, arrayListOf(
            Token(TokenGen.STR, "'a'"),
            Token(TokenGen.VAR, "a"),
            Token(TokenGen.STR, "'10'")
        ))

        val expr = ValueAnalyzer(context).eval(arrayListOf(token)) as InterpreterCallback
        val value = expr.call()

        assertEquals(
            "should get function value",
            "hello api value",
            value
        )
    }
}
