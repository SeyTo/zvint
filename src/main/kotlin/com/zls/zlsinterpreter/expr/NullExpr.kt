package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.Context
import com.zls.zlsinterpreter.Token

internal class NullExpr(private val token: Token):
    Expr { override fun eval(context: Context): Any? {
        return "null"
    }
}