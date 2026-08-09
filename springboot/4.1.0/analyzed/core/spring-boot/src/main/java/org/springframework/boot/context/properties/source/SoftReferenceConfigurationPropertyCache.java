/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.properties.source;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

/**
 * 使用 {@link SoftReference} 尽可能长时间缓存值的简单缓存。
 *
 * @param <T> 值类型
 * @author Phillip Webb
 */
class SoftReferenceConfigurationPropertyCache<T> implements ConfigurationPropertyCaching {

	private static final Duration UNLIMITED = Duration.ZERO;

	static final CacheOverride NO_OP_OVERRIDE = () -> {
	};

	private final boolean neverExpire;

	private volatile @Nullable Duration timeToLive;

	private volatile SoftReference<@Nullable T> value = new SoftReference<>(null);

	private volatile @Nullable Instant lastAccessed = now();

	SoftReferenceConfigurationPropertyCache(boolean neverExpire) {
		this.neverExpire = neverExpire;
	}

	@Override
	public void enable() {
		this.timeToLive = UNLIMITED;
	}

	@Override
	public void disable() {
		this.timeToLive = null;
	}

	@Override
	public void setTimeToLive(@Nullable Duration timeToLive) {
		this.timeToLive = (timeToLive == null || timeToLive.isZero()) ? null : timeToLive;
	}

	@Override
	public void clear() {
		this.lastAccessed = null;
	}

	@Override
	public CacheOverride override() {
		if (this.neverExpire) {
			return NO_OP_OVERRIDE;
		}
		ActiveCacheOverride override = new ActiveCacheOverride(this);
		if (override.timeToLive() == null) {
			// Ensure we don't use stale data on the first access
			clear();
		}
		this.timeToLive = UNLIMITED;
		return override;
	}

	void restore(ActiveCacheOverride override) {
		this.timeToLive = override.timeToLive();
		this.lastAccessed = override.lastAccessed();
	}

	/**
	 * 从缓存获取值，必要时创建。
	 *
	 * @param factory 无引用时用于创建项的工厂
	 * @param refreshAction 值过期时用于刷新的操作
	 * @return 缓存中的值
	 */
	T get(Supplier<T> factory, UnaryOperator<T> refreshAction) {
		T value = getValue();
		if (value == null) {
			value = refreshAction.apply(factory.get());
			setValue(value);
		}
		else if (hasExpired()) {
			value = refreshAction.apply(value);
			setValue(value);
		}
		if (!this.neverExpire) {
			this.lastAccessed = now();
		}
		return value;
	}

	private boolean hasExpired() {
		if (this.neverExpire) {
			return false;
		}
		Duration timeToLive = this.timeToLive;
		Instant lastAccessed = this.lastAccessed;
		if (timeToLive == null || lastAccessed == null) {
			return true;
		}
		return !UNLIMITED.equals(timeToLive) && now().isAfter(lastAccessed.plus(timeToLive));
	}

	protected Instant now() {
		return Instant.now();
	}

	protected @Nullable T getValue() {
		return this.value.get();
	}

	protected void setValue(T value) {
		this.value = new SoftReference<>(value);
	}

	/**
	 * 带有存储 TTL 的活动 {@link CacheOverride}。
	 */
	private record ActiveCacheOverride(SoftReferenceConfigurationPropertyCache<?> cache, @Nullable Duration timeToLive,
			@Nullable Instant lastAccessed, AtomicBoolean active) implements CacheOverride {

		ActiveCacheOverride(SoftReferenceConfigurationPropertyCache<?> cache) {
			this(cache, cache.timeToLive, cache.lastAccessed, new AtomicBoolean());
		}

		@Override
		public void close() {
			if (active().compareAndSet(false, true)) {
				this.cache.restore(this);
			}
		}

	}

}
