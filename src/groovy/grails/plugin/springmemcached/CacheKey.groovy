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

import grails.plugin.springmemcached.key.CacheKeyBuilder

/**
 * A generic key for storing items in and retrieving them from a cache.
 */
final class CacheKey implements Serializable {

	private final int hash
	private final long checksum

	static CacheKey generate(Object[] components) {
		def builder = new CacheKeyBuilder()
		for (component in components) {
			builder << component
		}
		builder.toCacheKey()
	}

	CacheKey(int hashCode, long checksum) {
		this.hash = hashCode
		this.checksum = checksum
	}

	@Override
	boolean equals(Object o) {
		if (this.is(o)) return true
		if (o == null) return false
		if (!(o in getClass())) return false
		hash == o.hash && checksum == o.checksum
	}

	@Override
	int hashCode() {
		int result = hash
		31 * result + (checksum ^ (checksum >>> 32))
	}

	@Override
	String toString() {
		"$hash|$checksum"
	}
}
