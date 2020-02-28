package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.*
import com.zls.zlsinterpreter.TokenGen.*

interface Expr {
    /**
     * Evaluate this given expression, expect the given GlobalContext or other context to be called.
     */
    fun eval(context: Context): Any?
}

internal fun tokenFactory(token: TokenType, expr: String): Token {
    return when(token) {
        VAR, ARR_VAR, STR, BOOLEAN, NULL -> {
            Token(token, expr)
        }
        COMPLEX -> {
            val newExpr = expr.substring(1, expr.length - 1).trim()
            // TODO handle recursive brackets
            Token(
                token,
                tokenFactory(
                    findMostLikelyTokenType(
                        findTokenProbabilityList(newExpr)
                    ).token,
                    newExpr
                )
            )
        }
        CONDITIONAL -> {
            // find string, val etc before after the condition
            val exprs = expr.split(TokenConditional.separator).map { it.trim() }
            // find that condition expressions
            val condition =
                TokenConditional.evalCondition(
                    expr
                )
            assert(exprs.size == 2)

            Token(
                token,
                arrayListOf(
                    tokenFactory(
                        findMostLikelyTokenType(
                            findTokenProbabilityList(exprs[0])
                        ).token, exprs[0]
                    ),
                    Token(condition, condition.name),
                    tokenFactory(
                        findMostLikelyTokenType(
                            findTokenProbabilityList(exprs[1])
                        ).token, exprs[1]
                    )
                )
            )
        }
        IF_COMPLEX -> {
            // assuming that if complex pattern has been matched by now
            IfExpr.iterator(expr)
        }
        FUNCTION -> {
            FunctionExpr.iterator(expr)
        }
        else -> {
            throw Error("Unknown token type")
        }

    }
}

