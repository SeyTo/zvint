# ZVINT

__ZVINT__ (ZLS Value Interpreter) is a simple code parser for simple string statements. This is intended for
use with JSON properties where we can put meaning to value rather than raw type values.

```
value1 ?: value2
```

can be interpreted when given a context of
```
value1 = null
value2 = "hello"
```

to get return value of
```
hello
```
where `value1 ?: value2` meaning 'if value1 is null then get value2 else get value1'.

This feature can be used to implement get meaningful JSON. for example.

```JSON
  {
    "cakeWeight": "10",
    "isEnough": "$ cakeWeight == 10"
  }
```

Parsing the above property `"isEnough"` with context `cakeWeight = 10` to get `"isEnough" = true`.

## Known Issues
refer [gitlab issue tracker](https://gitlab.com/0ls/zvint-gradle/issues)

## Syntax Available


#### String:
While evaluating as a string you should put `'` & `'` to indicate a string.

        'this is a string'
        'thisstring'
        

#### Variables:
Simple variables that represent the context name like 

        myvar
        test.status
        array[10]
        
for the array to work the context must have a map with key `array` and its value as array of string values.

#### Boolean

Basic boolean values

        true
        false
        
#### Null

Only the string literal 'null' is interpreted as a null value
        
#### Operators & Conditionals

a handset of conditionals are acceptable

        a == b // a equals b
        v != a // v not equals a
        b ?: 'c' // if b is 'null' then return a string c

#### Complex (Brackets)

It is also possible to use brackets around simple statements that needs to be evaluated.

        (a == b)
        ((a != b) && (b != c))      // No tests have been written yet, but this should work
        
This is only in experimental state and recursive statements like the second one may not work.

#### Functions:

        'helloApi'(a, '10')

`'helloApi'` is interpreted literally. Which means that zvint will try to search for 'helloApi' inside the `eval` function.
`a` and `'10'` are passed as arguments to the `eval` function.

If you want the function name to be evaluated before being called then you can do
        
        myVar(a, '10')
        
Here, `myVar` is first evaluated from either the `Context`'s `pool` map or `eval` function and only then will zvint try to 
search for whatever value `myVar` will emit.
      

## How it works

ZVINT unlike most compilers uses only Lexical, Syntax & Value processors. But the only process the user must understand is
the 'context'. To make ZVINT return return a result a context value must be specified to `Context` and then passed to the
interpreter.

Initialize by creating a context.
```kotlin
val globalContext = Context().apply {
    poolVariables["value1"] = "hello"
    poolVariables["value2"] = "1"
    poolVariables["value3"] = "true"
    poolVariables["value4"] = arrayListOf("a1", "a2", "a3")
}
```

So when ZVINT looks for variable with name "value2" it should be able to get "1". However, for now, only string and array of strings are
supported for poolVariables.

One more thing is added for convenience which is called `eval`.

```kotlin
 val globalContext = Context().apply {
    eval = object: Evaluator {
        override fun eval(vararg variable: Any): Any? {
            return when {
                variable[0] == "value" -> {
                    object: InterpreterCallback {
                        override fun call(): Any? = "hello"
                    }
                }
            }
        }
    }
 }
```

if ZVINT cannot find value inside `poolVariables` then it will try to look inside `eval` function. The function then has to return
either a string or `InterpreterCallback`. In some cases where you have to call a function `poolVariables` will not be called instead
`eval` is called.

_eval function is subject to change_

After supplying a context you can simply do

```kotlin
val result = ZLSInterpreter(globalContext).interpret(code)
```

to get the desired result.
