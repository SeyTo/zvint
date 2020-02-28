package com.zls.zlsinterpreter

import com.zls.zlsinterpreter.expr.tokenFactory
import kotlin.collections.ArrayList

internal object LexicalAnalyzer {

    fun eval(code: String): ArrayList<Token> {
        // root token not yet needed, we are only analyzing one statement at a time
        // val token = arrayListOf<Token>()

        val tokenProbabilitySets = findTokenProbabilityList(code.trim())
        // start building tokens
        val rootToken = tokenFactory(
                findMostLikelyTokenType(tokenProbabilitySets).token, code
        )

        return arrayListOf(rootToken)
    }

}
