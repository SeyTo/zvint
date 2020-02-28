package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.Context
import com.zls.zlsinterpreter.Token

/**
 * Only one dimensional array supported.
 */

internal class ArrVarExpr(private val token: Token):
    Expr {
    override fun eval(context: Context): Any? {
        val indexMatch = bracketRegex.find(token.constant.toString())!!
        var index = indexMatch.value
        index = index.substring(1, index.length - 1)

        val variable = token.constant.toString().substring(0, indexMatch.range.first)
        val found = context.eval?.eval(variable, index)
        return if (found == null) { // then try finding inside the pool
            val poolFound = context.poolVariables[variable]
            return if (poolFound != null) (poolFound as ArrayList<*>)[index.toInt()] else null
        } else found
    }

    companion object {
        val bracketRegex = "\\[[0-9]+]+".toRegex()
    }
}