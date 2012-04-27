/*
 * Copyright 2009 Rob Fletcher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.springmemcached

import static org.junit.Assert.*

import grails.test.mixin.*
import grails.test.mixin.support.*
import org.junit.*
import spock.lang.*;

class CacheKeySpec extends Specification {

	static final TARGET_1 = new Object()
	static final TARGET_2 = new Object()
	
	@Unroll("cache keys for #targetA.#methodA(#argsA) and #targetB.#methodB(#argsB) differ")
	def "cache keys differ based on target, method name and arguments"() {
		given:
		def key1 = CacheKey.generate(targetA, methodA, argsA)
		def key2 = CacheKey.generate(targetB, methodB, argsB)
		
		expect:
		key1 != key2
		key1.hashCode() != key2.hashCode()
		
		where:
		targetA  | methodA | argsA               | targetB  | methodB | argsB
		TARGET_1 | "x"     | []                  | TARGET_2 | "x"     | []
		TARGET_1 | "x"     | ["a"]               | TARGET_2 | "x"     | ["a"]
		TARGET_1 | "x"     | []                  | TARGET_1 | "y"     | []
		TARGET_1 | "x"     | ["a", "b"]          | TARGET_1 | "y"     | ["a", "c"]
		TARGET_1 | "x"     | []                  | TARGET_1 | "x"     | ["x"]
		TARGET_1 | "x"     | ["a", null]         | TARGET_1 | "x"     | ["a"]
		TARGET_1 | "x"     | ["a", null]         | TARGET_1 | "x"     | ["b", null]
		TARGET_1 | "x"     | ["a", "b"]          | TARGET_1 | "x"     | ["a", null, "b"]
		TARGET_1 | "x"     | [["a"] as Object[]] | TARGET_1 | "x"     | [["b"] as Object[]]
		TARGET_1 | "x"     | [["a"] as Object[]] | TARGET_1 | "x"     | ["a"]
		TARGET_1 | "x"     | [[1] as int[]]      | TARGET_1 | "x"     | [1]
	}
	
	@Unroll("cache keys for multiple calls to the same method passing #args are equal")
	def "cache keys are equal when target, method name and arguments are the same"() {
		given:
		def key1 = CacheKey.generate(TARGET_1, "x", args)
		def key2 = CacheKey.generate(TARGET_1, "x", args.clone())

		expect:
		key1 == key2
		key1.hashCode() == key2.hashCode()
		
		where:
		args << [
		[],
		["a", "b"],
		["a", null],
		["a", ["a"] as Object[]],
		["a", [null] as Object[]],
		[[1] as int[]],
		[[true] as boolean[]],
		["abc" as char[]]
		]
	}

}
