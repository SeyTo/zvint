package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.*
import com.zls.zlsinterpreter.LexicalAnalyzer
import com.zls.zlsinterpreter.Token
import com.zls.zlsinterpreter.TokenGen
import com.zls.zlsinterpreter.expressionFactory

internal class FunctionExpr(private val token: Token):
    Expr {
    override fun eval(context: Context): Any? {
        val functionTokens = token.constant as ArrayList<*>
        val functionNameToken = functionTokens[0] as Token
        var functionNameEval = expressionFactory(
            functionNameToken,
            context
        ) as String
        val functionArgEval = arrayListOf<String>()

        for (index in 1 until functionTokens.size) {
            val token = functionTokens[index] as Token
            functionArgEval += expressionFactory(
                token,
                context
            ) as String
        }

        println(functionNameEval)
        println(functionArgEval)

        return context.eval?.eval(InterpreterResultType.FUNCTION, functionNameEval, functionArgEval)
    }

    companion object {
        fun iterator(code: String): Token {
            val funName = code.trim().takeWhile { it != '(' }
            val funNameToken = if (funName.startsWith("'") && funName.endsWith("'"))
                Token(
                    TokenGen.STR,
                    funName
                )
            else
                Token(
                    TokenGen.VAR,
                    funName
                )

            val functionArgs = code
                .substringAfter("(")
                .removeSuffix(")")
                .split(",")
                .map { it.trim() }
            val functionArgsTokens = arrayListOf<Token>()

            for (functionArg in functionArgs) {
                functionArgsTokens += LexicalAnalyzer.eval(
                    functionArg
                )[0]
            }

            val functionTokens = arrayListOf<Token>().apply {
                add(funNameToken)
                for (functionArgToken in functionArgsTokens) {
                    add(functionArgToken)
                }
            }

            return Token(
                TokenGen.FUNCTION,
                functionTokens
            )
        }

    }
}