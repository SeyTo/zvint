package com.zls.zlsinterpreter

import com.zls.zlsinterpreter.expr.ArrVarExpr
import com.zls.zlsinterpreter.expr.BooleanExpr
import com.zls.zlsinterpreter.expr.ComplexExpr
import com.zls.zlsinterpreter.expr.ConditionalExpr
import com.zls.zlsinterpreter.expr.FunctionExpr
import com.zls.zlsinterpreter.expr.IfExpr
import com.zls.zlsinterpreter.expr.NullExpr
import com.zls.zlsinterpreter.expr.StrExpr
import com.zls.zlsinterpreter.expr.VarExpr

/**
 * Analyzes the token with given global context and gets values.
 */
internal class ValueAnalyzer(var context: Context? = null) {

    /**
     * @return no execution flow, only return a single value for now
     */
    fun eval(tokens: ArrayList<Token>): Any? {
        // only eval the first value
        val token = tokens[0]
        if (context == null) throw Error("Context is null")
        return expressionFactory(token, context!!)
    }
}

internal fun expressionFactory(token: Token, context: Context): Any? {
    return when (token.token) {
        TokenGen.VAR -> {
            VarExpr(token).eval(context)
        }
        TokenGen.ARR_VAR -> {
            ArrVarExpr(token).eval(context)
        }
        TokenGen.STR -> {
            StrExpr(token).eval(context)
        }
        TokenGen.BOOLEAN -> {
            BooleanExpr(token).eval(context)
        }
        TokenGen.CONDITIONAL -> {
            ConditionalExpr(token).eval(context)
        }
        TokenGen.COMPLEX -> {
            ComplexExpr(token).eval(context)
        }
        TokenGen.NULL -> {
            NullExpr(token).eval(context)
        }
        TokenGen.IF_COMPLEX -> {
            IfExpr(token).eval(context)
        }
        TokenGen.FUNCTION -> {
            FunctionExpr(token).eval(context)
        }
        else -> {
            throw Error("unknown token type found")
        }
    }
}

enum class InterpreterResultType {
    VAR, ARR_VAR, STR, BOOLEAN, NULL, FUNCTION
}
