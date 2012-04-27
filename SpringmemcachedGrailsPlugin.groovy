/*
* Copyright 2012 Tirant Lo Blanch
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
import org.codehaus.groovy.grails.commons.GrailsApplication

import org.slf4j.LoggerFactory

import net.spy.memcached.spring.MemcachedClientFactoryBean
import net.spy.memcached.transcoders.SerializingTranscoder

import grails.plugin.springmemcached.transcoders.DefaultSerializingTranscoder
import grails.plugin.springmemcached.aop.*
import grails.plugin.springmemcached.DefaultCacheResolver

class SpringmemcachedGrailsPlugin {
    // the plugin version
    def version = "0.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.0 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
	def pluginExcludes = [
			"grails-app/views/**",
			"web-app/**",
			"**/.gitignore",
			"**/.svn",
			"grails-app/*/grails/plugin/springmemcached/test/**",
			"src/java/grails/plugin/springmemcached/test/**",
	]
	def observe = ["groovyPages"]
	def loadAfter = ["groovyPages"]
	
	def author = "Daniel Soriano"
	def authorEmail = "soriano@tirant.com"
	def title = "Spring MemcachedCache Plugin"
	def description = "Provides annotation-driven caching of service methods and page fragments with memcached client"
	def documentation = ""

 	def doWithWebDescriptor = {xml ->
	}

	def doWithSpring = {
		if (!isEnabled(application)) {
			log.warn "Springcache plugin is disabled"
			 springmemcachedFilter(NoOpFilter)
		} else {
			if (application.config.grails.spring.disable.aspectj.autoweaving) {
				log.warn "Service method caching is not compatible with the config setting 'grails.spring.disable.aspectj.autoweaving = false'"
			}

			
			memcachedSerializingTranscoder(DefaultSerializingTranscoder,application.classLoader){
				compressionThreshold = application.config.grails.plugins.springmemcached.compressionThreshold ?: 1024							
			}
			
			memcachedClient(MemcachedClientFactoryBean) {
				servers = application.config.grails.plugins.springmemcached.servers ?: "127.0.0.1:11211"
				protocol = application.config.grails.plugins.springmemcached.protocol ?: "BINARY"
				transcoder = ref("memcachedSerializingTranscoder")
				opTimeout = application.config.grails.plugins.springmemcached.opTimeout ?: 1000
				timeoutExceptionThreshold = application.config.grails.plugins.springmemcached.timeoutExceptionThreshold ?: 1998
				hashAlg = application.config.grails.plugins.springmemcached.hashAlg ?: "KETAMA_HASH"
				locatorType = application.config.grails.plugins.springmemcached.locatorType ?: "CONSISTENT"
				failureMode = application.config.grails.plugins.springmemcached.failureMode ?: "Redistribute"
				useNagleAlgorithm = application.config.grails.plugins.springmemcached.useNagleAlgorithm ?: false
			}
		

			springmemcachedCachingAspect(CachingAspect) {
				springmemcachedService = ref("springmemcachedService")
			}

			springmemcachedDefaultCacheResolver(DefaultCacheResolver)
		}
	}

	def doWithDynamicMethods = {ctx ->
	}

	def doWithApplicationContext = { applicationContext ->
	}

	def onChange = { event ->
	}

	
	def onShutdown = { event ->
		memcachedClient.shutdown()
	}

	private static final log = LoggerFactory.getLogger("grails.plugin.springmemcached.SpringmemcachedGrailsPlugin")

	private boolean isEnabled(GrailsApplication application) {
		application.config.with {
			(springmemcached.enabled == null || springmemcached.enabled != false) && !springmemcached.disabled
		}
	}

}
