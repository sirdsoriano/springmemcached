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
import java.util.concurrent.TimeoutException;

import grails.spring.BeanBuilder
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import net.spy.memcached.MemcachedClient;

import org.slf4j.LoggerFactory
import org.springframework.context.*

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class SpringmemcachedService implements ApplicationContextAware {

	static private final log = LoggerFactory.getLogger(SpringmemcachedService.class)
	static transactional = false

	ApplicationContext applicationContext
	MemcachedClient memcachedClient
	
	def doWithCache(String cacheName, String key, int expires, Closure closure) {
		if (!enabled) return closure()
		return doWithCacheInternal(cacheName, key, expires, closure)
	}


	private doWithCacheInternal(String cacheName, String key, int expires, Closure closure) {
		def cacheKeyElement = cacheName+':'+key 
		def value = getObjectCache(cacheKeyElement)
		if (!value ) {
			if (log.isDebugEnabled()) log.debug "Cache '$cacheName' missed with key '$key'"
			value = closure()
			if (value)
				setObjectCache(cacheKeyElement,expires,value)
		} else {
			if (log.isDebugEnabled()) log.debug "Cache '$cache.name' hit with key '$key'"
		}
		return value
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
			throw e;
		} catch (ExecutionException e) {
			cancel(f);
			throw e;
		}
	}

	private void cancel(final Future<?> f) {
		if (f != null) {
			f.cancel(true);
		}
	}

	static boolean isEnabled() {
		ConfigurationHolder.with {
			(config?.springmemcached?.enabled == null || config?.springmemcached?.enabled != false) && !config?.springmemcached?.disabled
		}
	}
}

