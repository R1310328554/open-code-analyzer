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

package org.springframework.boot.retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryPolicy.Builder;

/**
 * {@link RetryPolicy} 的配置项。
 * 通过 {@link PropertyMapper} 映射到 {@link RetryPolicy.Builder} 并构建重试策略。
 *
 * @author Stephane Nicoll
 * @since 4.0.0
 */
public final class RetryPolicySettings {

	/**
	 * 默认最大重试次数。
	 */
	public static final long DEFAULT_MAX_RETRIES = RetryPolicy.Builder.DEFAULT_MAX_RETRIES;

	/**
	 * 默认初始延迟。
	 */
	public static final Duration DEFAULT_DELAY = Duration.ofMillis(RetryPolicy.Builder.DEFAULT_DELAY);

	/**
	 * 默认乘数；值为 1.0 时等效于固定延迟。
	 */
	public static final double DEFAULT_MULTIPLIER = RetryPolicy.Builder.DEFAULT_MULTIPLIER;

	/**
	 * 默认最大延迟（无上限）。
	 */
	public static final Duration DEFAULT_MAX_DELAY = Duration.ofMillis(RetryPolicy.Builder.DEFAULT_MAX_DELAY);

	private List<Class<? extends Throwable>> exceptionIncludes = new ArrayList<>();

	private List<Class<? extends Throwable>> exceptionExcludes = new ArrayList<>();

	private @Nullable Predicate<Throwable> exceptionPredicate;

	private Long maxRetries = DEFAULT_MAX_RETRIES;

	private Duration delay = DEFAULT_DELAY;

	private @Nullable Duration jitter;

	private Double multiplier = DEFAULT_MULTIPLIER;

	private Duration maxDelay = DEFAULT_MAX_DELAY;

	private @Nullable Function<Builder, RetryPolicy> factory;

	/**
	 * 根据当前实例状态创建 {@link RetryPolicy}。
	 *
	 * @return a {@link RetryPolicy} {@link RetryPolicy} 实例
	 */
	public RetryPolicy createRetryPolicy() {
		PropertyMapper map = PropertyMapper.get();
		RetryPolicy.Builder builder = RetryPolicy.builder();
		map.from(this::getExceptionIncludes).to(builder::includes);
		map.from(this::getExceptionExcludes).to(builder::excludes);
		map.from(this::getExceptionPredicate).to(builder::predicate);
		map.from(this::getMaxRetries).to(builder::maxRetries);
		map.from(this::getDelay).to(builder::delay);
		map.from(this::getJitter).to(builder::jitter);
		map.from(this::getMultiplier).to(builder::multiplier);
		map.from(this::getMaxDelay).to(builder::maxDelay);
		return (this.factory != null) ? this.factory.apply(builder) : builder.build();
	}

	/**
	 * 返回应触发重试的异常类型列表。
	 * <p>
	 * 默认为空，表示任意异常都会尝试重试。
	 *
	 * @return the applicable exception types 适用异常类型
	 */
	public List<Class<? extends Throwable>> getExceptionIncludes() {
		return this.exceptionIncludes;
	}

	/**
	 * 用给定 {@code includes} 替换应触发重试的异常类型列表。
	 * 也可通过 {@link #getExceptionIncludes()} 直接修改现有列表。
	 *
	 * @param includes the applicable exception types 适用异常类型
	 */
	public void setExceptionIncludes(List<Class<? extends Throwable>> includes) {
		this.exceptionIncludes = new ArrayList<>(includes);
	}

	/**
	 * 返回不应触发重试的异常类型列表。
	 * <p>
	 * 默认为空，表示任意异常都会尝试重试。
	 *
	 * @return the non-applicable exception types 不适用异常类型
	 */
	public List<Class<? extends Throwable>> getExceptionExcludes() {
		return this.exceptionExcludes;
	}

	/**
	 * 用给定 {@code excludes} 替换不应触发重试的异常类型列表。
	 * 也可通过 {@link #getExceptionExcludes()} 直接修改现有列表。
	 *
	 * @param excludes the non-applicable types 不适用异常类型
	 */
	public void setExceptionExcludes(List<Class<? extends Throwable>> excludes) {
		this.exceptionExcludes = new ArrayList<>(excludes);
	}

	/**
	 * 返回用于根据 {@link Throwable} 判断是否重试的谓词。
	 *
	 * @return the predicate to use 判定谓词
	 */
	public @Nullable Predicate<Throwable> getExceptionPredicate() {
		return this.exceptionPredicate;
	}

	/**
	 * 设置用于根据 {@link Throwable} 判断是否重试的谓词。
	 *
	 * @param exceptionPredicate the predicate to use 判定谓词
	 */
	public void setExceptionPredicate(@Nullable Predicate<Throwable> exceptionPredicate) {
		this.exceptionPredicate = exceptionPredicate;
	}

	/**
	 * 返回最大重试次数。
	 *
	 * @return the maximum number of retry attempts 最大重试次数
	 * @see #DEFAULT_MAX_RETRIES
	 */
	public Long getMaxRetries() {
		return this.maxRetries;
	}

	/**
	 * 设置最大重试次数。
	 *
	 * @param maxRetries the maximum number of retry attempts (must be equal or greater
	 * than zero) 最大重试次数（必须大于等于 0）
	 */
	public void setMaxRetries(Long maxRetries) {
		this.maxRetries = maxRetries;
	}

	/**
	 * 返回首次调用后的基础延迟。
	 *
	 * @return the base delay 基础延迟
	 * @see #DEFAULT_DELAY
	 */
	public Duration getDelay() {
		return this.delay;
	}

	/**
	 * 设置首次调用后的基础延迟。
	 * <p>
	 * 若指定了 {@linkplain #getMultiplier() 乘数}，此值作为后续倍增的初始延迟。
	 *
	 * @param delay the base delay (must be greater than or equal to zero) 基础延迟（必须大于等于 0）
	 */
	public void setDelay(Duration delay) {
		this.delay = delay;
	}

	/**
	 * 返回用于随机化重试间隔的抖动周期。
	 *
	 * @return the jitter value 抖动值
	 */
	public @Nullable Duration getJitter() {
		return this.jitter;
	}

	/**
	 * 设置基础重试尝试的抖动周期，会在计算出的延迟上随机加减，
	 * 结果介于 {@code delay - jitter} 与 {@code delay + jitter} 之间，
	 * 但不会低于 {@linkplain #getDelay() 基础延迟} 或高于 {@linkplain #getMaxDelay() 最大延迟}。
	 * <p>
	 * 若指定了 {@linkplain #getMultiplier() 乘数}，抖动值也会相应倍增。
	 *
	 * @param jitter the jitter value (must be positive) 抖动值（必须为正数）
	 */
	public void setJitter(@Nullable Duration jitter) {
		this.jitter = jitter;
	}

	/**
	 * 返回每次重试将当前间隔乘以的系数。默认值 {@code 1.0} 等效于固定延迟。
	 *
	 * @return the value to multiply the current interval by for each attempt 间隔乘数
	 * @see #DEFAULT_MULTIPLIER
	 */
	public Double getMultiplier() {
		return this.multiplier;
	}

	/**
	 * 设置下次重试延迟的乘数。
	 *
	 * @param multiplier value to multiply the current interval by for each attempt (must
	 * be greater than or equal to 1) 间隔乘数（必须大于等于 1）
	 */
	public void setMultiplier(Double multiplier) {
		this.multiplier = multiplier;
	}

	/**
	 * 返回任意重试尝试的最大延迟。
	 *
	 * @return the maximum delay 最大延迟
	 */
	public Duration getMaxDelay() {
		return this.maxDelay;
	}

	/**
	 * 设置任意重试尝试的最大延迟，限制 {@linkplain #getJitter() 抖动} 与
	 * {@linkplain #getMultiplier() 乘数} 可将 {@linkplain #getDelay() 延迟} 放大的上限。
	 * <p>
	 * 默认无上限。
	 *
	 * @param maxDelay the maximum delay (must be positive) 最大延迟（必须为正数）
	 * @see #DEFAULT_MAX_DELAY
	 */
	public void setMaxDelay(Duration maxDelay) {
		this.maxDelay = maxDelay;
	}

	/**
	 * 设置用于创建 {@link RetryPolicy} 的工厂；为 {@code null} 时使用默认构建方式。
	 * 工厂接收已按当前实例状态初始化的 {@link Builder RetryPolicy.Builder}，
	 * 可进一步配置，也可忽略后从头构建。
	 *
	 * @param factory a factory to customize the retry policy. 自定义重试策略的工厂
	 */
	public void setFactory(@Nullable Function<RetryPolicy.Builder, RetryPolicy> factory) {
		this.factory = factory;
	}

}
