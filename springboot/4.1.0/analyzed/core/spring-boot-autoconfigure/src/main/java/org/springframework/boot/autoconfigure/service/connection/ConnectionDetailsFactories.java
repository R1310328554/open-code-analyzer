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

package org.springframework.boot.autoconfigure.service.connection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.io.support.SpringFactoriesLoader.FailureHandler;
import org.springframework.util.Assert;

/**
 * {@link ConnectionDetailsFactory} 实例的注册表。
 *
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 * @author Pedro Xavier Leite Cavadas
 * @since 3.1.0
 */
public class ConnectionDetailsFactories {

	private static final Log logger = LogFactory.getLog(ConnectionDetailsFactories.class);

	private final List<Registration<?, ?>> registrations = new ArrayList<>();

	/**
	 * 创建新的 {@link ConnectionDetailsFactories} 实例。
	 * @param classLoader 用于加载工厂的类加载器
	 * @since 3.5.0
	 */
	public ConnectionDetailsFactories(@Nullable ClassLoader classLoader) {
		this(SpringFactoriesLoader.forDefaultResourceLocation(classLoader));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	ConnectionDetailsFactories(SpringFactoriesLoader loader) {
		List<ConnectionDetailsFactory> factories = loader.load(ConnectionDetailsFactory.class,
				FailureHandler.logging(logger));
		Stream<Registration<?, ?>> registrations = factories.stream().map(Registration::get);
		registrations.filter(Objects::nonNull).forEach(this.registrations::add);
	}

	/**
	 * 返回由与给定源关联的工厂创建的 {@link ConnectionDetails} 接口类型到
	 * {@link ConnectionDetails} 实例的 {@link Map}。
	 * @param <S> 源类型
	 * @param source 源对象
	 * @param required 是否必须得到连接详情结果
	 * @return {@link ConnectionDetails} 实例映射
	 * @throws ConnectionDetailsFactoryNotFoundException 需要结果但未为该源注册连接详情工厂
	 * @throws ConnectionDetailsNotFoundException 需要结果但已注册工厂未创建连接详情实例
	 */
	public <S> Map<Class<?>, ConnectionDetails> getConnectionDetails(S source, boolean required)
			throws ConnectionDetailsFactoryNotFoundException, ConnectionDetailsNotFoundException {
		List<Registration<S, ?>> registrations = getRegistrations(source, required);
		Map<Class<?>, ConnectionDetails> result = new LinkedHashMap<>();
		for (Registration<S, ?> registration : registrations) {
			ConnectionDetails connectionDetails = registration.factory().getConnectionDetails(source);
			if (connectionDetails != null) {
				Class<?> connectionDetailsType = registration.connectionDetailsType();
				ConnectionDetails previous = result.put(connectionDetailsType, connectionDetails);
				Assert.state(previous == null, () -> "Duplicate connection details supplied for %s"
					.formatted(connectionDetailsType.getName()));
			}
		}
		if (required && result.isEmpty()) {
			throw new ConnectionDetailsNotFoundException(source);
		}
		return Map.copyOf(result);
	}

	@SuppressWarnings("unchecked")
	<S> List<Registration<S, ?>> getRegistrations(S source, boolean required) {
		Class<S> sourceType = (Class<S>) source.getClass();
		List<Registration<S, ?>> result = new ArrayList<>();
		for (Registration<?, ?> candidate : this.registrations) {
			if (candidate.sourceType().isAssignableFrom(sourceType)) {
				result.add((Registration<S, ?>) candidate);
			}
		}
		if (required && result.isEmpty()) {
			throw new ConnectionDetailsFactoryNotFoundException(source);
		}
		result.sort(Comparator.comparing(Registration::factory, AnnotationAwareOrderComparator.INSTANCE));
		return List.copyOf(result);
	}

	/**
	 * {@link ConnectionDetailsFactory} 注册项。
	 *
	 * @param <S> 源类型
	 * @param <D> 连接详情类型
	 * @param sourceType 源类型
	 * @param connectionDetailsType 连接详情类型
	 * @param factory 工厂
	 */
	record Registration<S, D extends ConnectionDetails>(Class<S> sourceType, Class<D> connectionDetailsType,
			ConnectionDetailsFactory<S, D> factory) {

		@SuppressWarnings("unchecked")
		private static <S, D extends ConnectionDetails> @Nullable Registration<S, D> get(
				ConnectionDetailsFactory<S, D> factory) {
			ResolvableType type = ResolvableType.forClass(ConnectionDetailsFactory.class, factory.getClass());
			@Nullable Class<?>[] generics = type.resolveGenerics();
			Class<S> sourceType = (Class<S>) generics[0];
			Class<D> connectionDetailsType = (Class<D>) generics[1];
			return (sourceType != null && connectionDetailsType != null)
					? new Registration<>(sourceType, connectionDetailsType, factory) : null;
		}

	}

}
