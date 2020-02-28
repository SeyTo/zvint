package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.Context
import com.zls.zlsinterpreter.Token

internal class VarExpr(private val token: Token):
    Expr {
    override fun eval(context: Context): Any? {
        // no object evaluation for now
        return context.poolVariables[token.constant]
            ?: return context.eval?.eval(token.constant as String)
    }
}