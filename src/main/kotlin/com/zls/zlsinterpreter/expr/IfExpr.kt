package com.zls.zlsinterpreter.expr

import com.zls.zlsinterpreter.*
import com.zls.zlsinterpreter.Token
import com.zls.zlsinterpreter.TokenConditional
import com.zls.zlsinterpreter.TokenGen
import com.zls.zlsinterpreter.expressionFactory
import com.zls.zlsinterpreter.findMostLikelyTokenType
import com.zls.zlsinterpreter.findTokenProbabilityList

internal class IfExpr(private val token: Token):
    Expr {
    override fun eval(context: Context): Any? {
        // get if conditional part
        val ifConstant = token.constant as ArrayList<*>

        for (any in ifConstant) {
            val token = any as Token
            if (token.token is TokenConditional) {
                when (token.token) {
                    TokenConditional.IF_CONDITIONAL -> {
                        val tokenArrays = (token.constant as ArrayList<*>)
                        if (expressionFactory(
                                tokenArrays[0] as Token,
                                context
                            ) == "true") {
                            return expressionFactory(
                                tokenArrays[1] as Token,
                                context
                            )
                        }
                    }
                    TokenConditional.ELSE_IF_CONDITIONAL -> {
                        val tokenArrays = (token.constant as ArrayList<*>)
                        if (expressionFactory(
                                tokenArrays[0] as Token,
                                context
                            ) == "true") {
                            return expressionFactory(
                                tokenArrays[1] as Token,
                                context
                            )
                        }
                    }
                    TokenConditional.ELSE_VALUE -> {
                        val tokenArrays = (token.constant as ArrayList<*>)
                        return expressionFactory(
                            tokenArrays[0] as Token,
                            context
                        )
                    }
                    else -> {
                        throw Error("Unknown TokenConditional found inside ifConstant")
                    }
                }
            }
        }
        throw Error("Wrong syntax parsed. Something went wrong with if statement")
    }

    companion object {
        // FIXME values can contain a preceding space. e.g. if (a == b) 'hello' else 'go',
        /**
         * @return return IF_CONDITIONAL token with syntactic tokens.
         */
        fun iterator(expr: String): Token {
            // filter and clean split variables get only non keyword values
            var values = TokenConditional.ifSeparator.split(expr)
            values = values.map { it.trim() }.filter { it.isNotEmpty() }

            // this algorithm may cause problems
            val bracketRegex = "\\(.*?\\)".toRegex()

            // further filter the vars to get the conditionals as well (`(true == false)`)
            val conditionals = arrayListOf<String>()
            // get conditional contained values as well (`'a'`, `value`)
            val conditionalExpressions = arrayListOf<String>()
            for (value in values) {
                val conditionalExpr = bracketRegex.replaceFirst(value, "")
                val conditionalArea = bracketRegex.find(value)?.value
                if (conditionalArea != null && conditionalArea.isNotEmpty())
                    conditionals.add(conditionalArea)
                conditionalExpressions.add(conditionalExpr)
            }

            // evaluate the types of conditionals
            val conditionalTokens = arrayListOf<Token>()
            for (conditional in conditionals) {
                conditionalTokens.add(
                    tokenFactory(
                        findMostLikelyTokenType(
                            findTokenProbabilityList(
                                conditional
                            )
                        ).token,
                        conditional
                    )
                )
            }

            // evaluate the types of conditionalExpressions
            val conditionalExpressionTokens = arrayListOf<Token>()
            for (conditionalExpression in conditionalExpressions) {
                conditionalExpressionTokens.add(
                    tokenFactory(
                        findMostLikelyTokenType(
                            findTokenProbabilityList(
                                conditionalExpression
                            )
                        ).token, conditionalExpression
                    )
                )
            }

            // now structure the tokens into proper tree

            // if and its condition is always present, so simply add it
            val ifConditionalToken = Token(
                TokenConditional.IF_CONDITIONAL,
                arrayListOf(
                    conditionalTokens[0],
                    conditionalExpressionTokens[0]
                )
            )

            // put else if structures as well
            // we also assume that else
            val elseIfConditionalTokens = arrayListOf<Token>()
            if (conditionalTokens.size >= 2) {
                for (i in 1 until conditionalTokens.size) {
                    elseIfConditionalTokens.add(conditionalTokens[i])
                    elseIfConditionalTokens.add(conditionalExpressionTokens[i])
                }
            }
            val elseIfConditionalToken = Token(
                TokenConditional.ELSE_IF_CONDITIONAL,
                elseIfConditionalTokens
            )

            // put else structure as well
            val elseConditionalToken = Token(
                TokenConditional.ELSE_VALUE,
                arrayListOf(
                    conditionalExpressionTokens.last()
                )
            )

            // put all token into root token
            return Token(
                TokenGen.IF_COMPLEX,
                arrayListOf<Token>().apply {
                    add(ifConditionalToken)
                    if (elseIfConditionalTokens.size != 0) add(elseIfConditionalToken)
                    add(elseConditionalToken)
                })
        }
    }
}