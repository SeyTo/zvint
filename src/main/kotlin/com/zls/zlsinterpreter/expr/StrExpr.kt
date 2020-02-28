package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.Context
import com.zls.zlsinterpreter.Token

internal class StrExpr(private val token: Token):
    Expr {
    override fun eval(context: Context): Any? {
        val str = token.constant as String
        return (str).substring(1, str.length - 1)
    }
}