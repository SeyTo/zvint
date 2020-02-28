package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.Context
import com.zls.zlsinterpreter.Token
import com.zls.zlsinterpreter.expressionFactory

internal class ConditionalComplexExpr(val token: Token):
    Expr {
    override fun eval(context: Context): Any? {
        return expressionFactory(
            token.constant as Token,
            context
        )
    }
}