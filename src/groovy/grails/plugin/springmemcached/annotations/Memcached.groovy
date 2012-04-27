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
package grails.plugin.springmemcached.annotations;

import java.lang.annotation.*

/**
 * Based on org.springmodules.cache.annotations.Cacheable (see https://springmodules.dev.java.net/)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.METHOD, ElementType.TYPE, ElementType.FIELD])
@Inherited
@Documented
public @interface Memcached {
	String value() default ""
	String cache() default ""
	int    expires() default 3600
	String cacheResolver() default "springmemcachedDefaultCacheResolver"
}