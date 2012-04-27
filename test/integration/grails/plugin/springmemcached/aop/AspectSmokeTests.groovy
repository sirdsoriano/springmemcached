/*
 * Copyright 2010 Luke Daley
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
package grails.plugin.springmemcached.aop

/**
 * Implicitly tests that spring beans in the application
 * that are annotated with out annotations get the correct
 * caching behaviour.
 */
class AspectSmokeTests extends GroovyTestCase {

	def cachingService // prototype scope, unique per test
	
	void setUp() {
		assert cachingService.value == 0
	}

	void tearDown() {
		
	}
	
	void testSimpleCachingAndFlushingBehaviour() {
		assert cachingService.addValueTo(1) == 1
		cachingService.value = 1
		assert cachingService.addValueTo(1) == 1
	}
	
	/**
	 * This is largely redundant as autoboxing means that the
	 * args actually go through as Objects, but it's here for
	 * good measure.
	 */
	void testThatCacheableMethodsCanReceivePrimitiveTypes() {
		assert cachingService.addValueTo(1 as int) == 1
		cachingService.value = 1
		assert cachingService.addValueTo(1 as int) == 1
	}
	
	
}