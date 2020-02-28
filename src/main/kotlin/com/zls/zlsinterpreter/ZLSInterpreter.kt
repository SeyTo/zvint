package com.zls.zlsinterpreter

import java.lang.Error

class ZLSInterpreter(var context: Context = Context()) {

    /**
     * Parsing happens in these steps.
     * 1. lexical analysis
     *  1. Syntax analysis
     * 2. Value analysis
     */
    fun interpret(code: String): Any? {
        val tokens = LexicalAnalyzer.eval(code)
        return ValueAnalyzer(context).eval(tokens)
    }
}

/**
 * Context container for variables and other functions.
 */
class Context {
    val poolVariables = hashMapOf<String, Any>()
    var eval: Evaluator? = null
}

/**
 * Callback for interpreter used with Context.eval.
 */
interface InterpreterCallback {
    fun call(): Any?
}

/**
 * Basic interface for eval.
 */
interface Evaluator {
    fun eval(vararg variable: Any): Any?
}
