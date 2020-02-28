package com.zls.zlsinterpreter

import java.lang.Error


interface TokenType

internal data class TokenProbabilitySet(val token: TokenGen, var probability: Float)

/**
 *
 * @param literals the more condition
 */
internal enum class TokenGen(
    val separator: Regex? = null,
    val literals: Array<Any>? = null,
    val regex: Regex? = null
): TokenType {
    // This order must be preserved

    /** * Basic variable from context */
    VAR(
        regex = "([a-zA-Z]+[0-9]?\\.?)+".toRegex(),
        literals = arrayOf(object : TokenPresumption {
            override fun presume(preAssumptionToken: TokenType?, value: String): Boolean {
                val temp = value.toLowerCase()
                return temp != "true" && temp != "false"
            }
        })
    ),
    /** if conditional */
    IF_COMPLEX(
        literals = arrayOf("if", "else", "elif"),
        regex = "if ?\\(.*\\).*(elif)?.*else.*".toRegex()
    ),
    /** * String token type */
    STR(regex = "'[a-zA-Z0-9]*'".toRegex()),
    /** Array variable */
    ARR_VAR(regex = "([a-zA-Z]+[0-9]?\\.?(\\[[0-9]+]))+".toRegex()),
    /** * Only boolean variables */
    BOOLEAN(regex = "(true|false)".toRegex()),
    /** * Null */
    NULL(regex = "(null)".toRegex()),
    /** * Conditional like == or != etc */
    CONDITIONAL(
        regex = "(([a-zA-Z]+[0-9]?(\\[[0-9]+])?)+) ?(\\?:|==|!=) ?(([a-zA-Z]+[0-9]?(\\[[0-9]+])?)+)".toRegex()
    ),
    /** a complex which could contain further tokens */
    COMPLEX(regex = "\\(.*\\)".toRegex()),
    FUNCTION(regex = ("^(('?\\w+')|(\\w+))\\(.*\\)$").toRegex())
    ;

    interface TokenPresumption {
        fun presume(preAssumptionToken: TokenType? = null, value: String): Boolean
    }

    fun tokenProbabilitySize(): Int {
        var size = 1 // 1 because of regex
        size += literals?.size ?: 0
        return size
    }


}

internal enum class TokenConditional: TokenType {
    EQUALS,
    NOT_EQUALS,
    IF_CONDITIONAL,
    ELSE_IF_CONDITIONAL,
    ELSE_VALUE,
    IF_NULL;

    companion object {
        val separator = "(\\?:|==|!=)+".toRegex()
        val ifSeparator = "\\bif|elif|else".toRegex()

        /**
         * @param expr has to be 'if' pattern validated
         */
        fun expr(expr: String) {
        }

        fun evalCondition(expression: String): TokenConditional {
            val conditional = separator.find(expression)?.value ?: throw Error("no conditional symbol found")
            return when (conditional) {
                "?:" -> {
                    IF_NULL
                }
                "==" -> {
                    EQUALS
                }
                "!=" -> {
                    NOT_EQUALS
                }
                else -> {
                    throw Error("Unknown case")
                }
            }
        }
    }
}

internal data class Token(val token: TokenType, val constant: Any)

/**
 *
 */
internal fun findMostLikelyTokenType(tokenProbabilitySets: ArrayList<TokenProbabilitySet>): TokenProbabilitySet {
    if (tokenProbabilitySets.size == 0) throw Error("Empty token probability sets")
    else if (tokenProbabilitySets.size == 1) return tokenProbabilitySets[0]

    var mostLikely = tokenProbabilitySets[0]
    tokenProbabilitySets.forEach {
        if (it.probability >= mostLikely.probability) mostLikely = it
    }
    return mostLikely
}

/**
 * @param code code string to evaluate
 */
internal fun findTokenProbabilityList(
    code: String
): ArrayList<TokenProbabilitySet> {
    val tokenProbabilitySets = arrayListOf<TokenProbabilitySet>()

    for (value in TokenGen.values()) {
        var probability = 0F
        val tokenProbabilitySize = value.tokenProbabilitySize().toFloat()
        val tokenProbabilityBlock = 1F / tokenProbabilitySize

        if (value.regex != null) {
            if (value.regex.matches(code.trim())) {
                // regex match point
                probability += tokenProbabilityBlock

                // check with literals
                probability = literalsMatchProbability(value, code, tokenProbabilityBlock, probability)
            }
            // if don't match then leave to next
        } else { // try with literals
             probability = literalsMatchProbability(value, code, tokenProbabilityBlock, probability)
        }

        tokenProbabilitySets += TokenProbabilitySet(value, (probability / 1.0F))
    }

//    for (tokenProbabilitySet in tokenProbabilitySets) {
//        println("${tokenProbabilitySet.token} -> ${tokenProbabilitySet.probability}")
//    }

    if (tokenProbabilitySets.size == 0) println("Warning: TokenProbabilitySets size is 0 for $code")

    return tokenProbabilitySets
}

internal fun literalsMatchProbability(
    value: TokenGen,
    code: String,
    tokenProbabilityBlock: Float,
    probability: Float
): Float {
    var prob = probability
    value.literals?.forEach {
        when (it) {
            is TokenGen.TokenPresumption -> {
                if (it.presume(value = code)) prob += tokenProbabilityBlock
            }
            is String -> {
                if (code.contains(it)) prob += tokenProbabilityBlock
            }
        }
    }
    return prob
}

