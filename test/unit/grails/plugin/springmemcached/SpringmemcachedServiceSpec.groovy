
/*
 * Copyright 2010 Rob Fletcher
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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import grails.plugin.spock.UnitSpec
import grails.spring.BeanBuilder
import spock.lang.Unroll

import net.spy.memcached.FailureMode;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.HashAlgorithm;
import net.spy.memcached.ConnectionFactoryBuilder.Locator;
import net.spy.memcached.ConnectionFactoryBuilder.Protocol;

import grails.plugin.springmemcached.test.MoreComplexObject

class SpringmemcachedServiceSpec extends UnitSpec {

	static final int DEFAULT_EXPIRES = 3600
	
	ConnectionFactory connectionFactory;
	MemcachedClient memcachedClient
	SpringmemcachedService service

	def setup() {
        ConnectionFactoryBuilder builder = new ConnectionFactoryBuilder();
        builder.setHashAlg(HashAlgorithm.KETAMA_HASH);
        builder.setLocatorType(Locator.CONSISTENT);
        builder.setProtocol(Protocol.BINARY);
        builder.setOpTimeout(1000);
		builder.setTimeoutExceptionThreshold(1998);
		builder.setFailureMode(FailureMode.Redistribute);
		builder.setUseNagleAlgorithm(false);
		builder.setTranscoder(new grails.plugin.springmemcached.transcoders.DefaultSerializingTranscoder(this.class.classLoader))
        connectionFactory = builder.build();
	
		def address = new InetSocketAddress("127.0.0.1",11211)
		memcachedClient = new MemcachedClient(connectionFactory, [address]);
		
		memcachedClient.delete("cache:key").get()
		
		mockLogging SpringmemcachedService, true
		service = new SpringmemcachedService(memcachedClient: memcachedClient)
	}

	def cleanup() {
		memcachedClient.delete("cache:key").get()
		memcachedClient.shutdown()
	}
	
	@Unroll("doWithCache retrieves #value from cache")
	def "doWithCache retrieves value from cache"() {
		given:
		setObjectCache("cache:key",DEFAULT_EXPIRES,value)

		when:
		def result = service.doWithCache("cache", "key", DEFAULT_EXPIRES) {
			fail "Closure should not have been invoked"
		}

		then:
		result == value

		where:
		value << ["value","value2"]
	}

	@Unroll("doWithCache stores complex object #value into cache")
	def "doWithCache stores complex objects"() {
		given:
		def complexObject = new MoreComplexObject(idRef:pidRef,description:pdescription, otherInfo:(potherInfo as List<String>), numbers:(pnumbers as Map<String,Long>))
		println "******* ${complexObject}"
		setObjectCache("cache:key",DEFAULT_EXPIRES, complexObject)

		when:
		def result = service.doWithCache("cache", "key", DEFAULT_EXPIRES) {
			fail "Closure should not have been invoked"
		}

		then:
		result.idRef == pidRef

		and:
		result.description == pdescription
		
		and:
		result.otherInfo == potherInfo
		
		and:
		result.numbers == pnumbers
		
		where:
		pidRef 	| pdescription 	| potherInfo 		| pnumbers
		1  		| "desc 1"    	| ["otherInfo 1"]	| ["a":1]
		2  		| "desc 2"    	| ["otherInfo 2"]	| ["a":2]

	}


	def "doWithCache stores value returned by closure if not found in cache"() {
		when:
		def result = service.doWithCache("cache", "key", DEFAULT_EXPIRES) {
			return "value"
		}

		then:
		result == "value"

		and:
		getObjectCache("cache:key") == "value"
	}

	def "doWithCache stores value returned by closure if cache element expired"() {
		given:
		setObjectCache("cache:key",1,"value")
		Thread.sleep 2000

		when:
		def result = service.doWithCache("cache", "key",DEFAULT_EXPIRES) {
			return "value"
		}

		then:
		result == "value"

		and:
		getObjectCache("cache:key") == "value"
		
	}

	def "doWithCache doesn't store null if closure returns null"() {
		when:
		def result = service.doWithCache("cache", "key", DEFAULT_EXPIRES) {
			return null
		}

		then:
		result == null

		and:
		getObjectCache("cache:key") == null
	}


	@Unroll("the #methodName method passes through when the plugin is disabled")
	def "caching methods pass through when the plugin is disabled"() {
		given:
		mockConfig "springmemcached.enabled = false"
		service.memcachedClient = null

		when:
		def result = service."$methodName"("cache1", "key", DEFAULT_EXPIRES) { "value" }

		then:
		result == "value"

		where:
		methodName << ["doWithCache"]
	}

	@Unroll("the #methodName method is a no-op when the plugin is disabled")
	def "caching methods are no-ops when the plugin is disabled"() {
		given:
		mockConfig "springmemcached.enabled = false"
		service.memcachedClient = null

		when:
		service."$methodName"("cache1", "key", DEFAULT_EXPIRES) { "value" }

		then:
		notThrown(Throwable)

		where:
		methodName << ["doWithCache"]
	}
	
	
	public Object getObjectCache(final String key)  {
		return memcachedClient.get(key);
	}


	public boolean setObjectCache(final String key, final int exp, final Object value)  {
		Future<Boolean> f = null;
		try {
			f = memcachedClient.set(key, exp, value);
			return f.get();
		} catch (InterruptedException e) {
			cancel(f);
			e.printStackTrace();
			throw e;
		} catch (ExecutionException e) {
			cancel(f);
			e.printStackTrace();
			throw e;
		}
	}

	private void cancel(final Future<?> f) {
		if (f != null) {
			f.cancel(true);
		}
	}

}

