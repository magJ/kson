/*
 * Copyright (c) 2014 Andrew O'Malley
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.github.andrewoma.kson

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.andrewoma.kson.ext.js
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Before
import org.junit.Test

class JsValueTest {
    val mapper = ObjectMapper()

    @Before fun setUp() {
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.registerModule(KsonModule())
    }

    val value = JsObject(
            "firstName" to "Andrew".js,
            "lastName" to "O'Malley".js,
            "age" to 21.js,
            "adult" to true.js,
            "address" to JsObject(
                    "number" to "88".js,
                    "street" to "Chapel Street".js,
                    "suburb" to "Windsor".js,
                    "state" to "VIC".js,
                    "postCode" to "3181".js
            ),
            "pets" to JsArray(
                    JsObject(
                            "kind" to "dog".js,
                            "name" to "Rover".js
                    ),
                    JsObject(
                            "kind" to "cat".js,
                            "name" to "Kitty".js
                    )
            )
    )

    @Test fun testManualBuilding() {
        val actual = mapper.writeValueAsString(value)
        val expected = """
{
  "firstName" : "Andrew",
  "lastName" : "O'Malley",
  "age" : 21,
  "adult" : true,
  "address" : {
    "number" : "88",
    "street" : "Chapel Street",
    "suburb" : "Windsor",
    "state" : "VIC",
    "postCode" : "3181"
  },
  "pets" : [ {
    "kind" : "dog",
    "name" : "Rover"
  }, {
    "kind" : "cat",
    "name" : "Kitty"
  } ]
}"""
        assertEquals(expected.trim(), actual)
    }

    @Test fun testExtraction() {
        assertEquals("Andrew", value["firstName"].asString())
        assertEquals(21, value["age"].asInt())
        assertTrue(value["adult"].asBoolean()!!)

        assertEquals("Chapel Street", value["address"]["street"].asString())

        val petNames = value["pets"].map { it["name"].asString() }
        assertEquals(listOf("Rover", "Kitty"), petNames)
    }

    @Test fun testIterationOfSingleNode() {
        assertEquals(listOf(JsString("Andrew")), value["firstName"].toList())
    }

    @Test fun testConversions() {
        fun <T> assertConversion(value: T, f: (T) -> T) = assertEquals(value, f(value))

        assertConversion("A") { toJsValue(it).asString()!! }
        assertConversion(true) { toJsValue(it).asBoolean()!! }
        assertConversion(21) { toJsValue(it).asInt()!! }
        assertConversion(21L) { toJsValue(it).asLong()!! }
        assertConversion(21.3f) { toJsValue(it).asFloat()!! }
        assertConversion(21.3) { toJsValue(it).asDouble()!! }
    }
}
