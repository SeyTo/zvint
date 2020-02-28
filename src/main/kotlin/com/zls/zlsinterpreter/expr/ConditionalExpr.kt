package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.Context
import com.zls.zlsinterpreter.Token
import com.zls.zlsinterpreter.TokenConditional
import com.zls.zlsinterpreter.expressionFactory

internal class ConditionalExpr(private val token: Token):
    Expr {
    override fun eval(context: Context): Any? {
        // evaluate all the tokens first
        var evalCondition: Token? = null
        val varTokens = arrayListOf<Token>()
        for (any in (token.constant as ArrayList<*>)) {
            val token = any as Token
            if (token.token is TokenConditional) {
                evalCondition = token
            } else {
                varTokens += token
            }
        }

        val variables = arrayListOf<String>()
        // evaluate the variables first
        for (variable in varTokens) {
             variables += expressionFactory(
                 variable,
                 context
             ) as String
        }

        // find out the expression value
        return when (evalCondition?.token) {
            TokenConditional.EQUALS -> {
                (variables[0] == variables[1]).toString()
            }
            TokenConditional.NOT_EQUALS -> {
                (variables[0] != variables[1]).toString()
            }
            TokenConditional.IF_NULL -> {
                if (variables[0] == "null") variables[1] else variables[0]
            }
            else -> {
                throw Error("Unknown conditional case was caught")
            }
        }
    }
}