/*
 * Copyright 2002-present the original author or authors.
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

package org.springframework.http.codec;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import kotlin.reflect.KFunction;
import kotlin.reflect.KType;
import kotlin.reflect.full.KCallables;
import kotlin.reflect.jvm.ReflectJvmMapping;
import kotlinx.serialization.KSerializer;
import kotlinx.serialization.SerialFormat;
import kotlinx.serialization.SerializersKt;
import org.jspecify.annotations.Nullable;

import org.springframework.core.KotlinDetector;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.MimeType;

/* ===== [OCA 中文解析] =====
class KotlinSerializationSupport — 意图说明

class `KotlinSerializationSupport`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-web/src/main/java/org/springframework/http/codec/KotlinSerializationSupport.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Base class providing support methods for encoding and decoding with Kotlin
 * serialization.
 *
 * <p>As of Spring Framework 7.0, by default it only handles types annotated with
 * {@link kotlinx.serialization.Serializable @Serializable} at type or generics level
 * since it allows combined usage with other general purpose decoders without conflicts.
 * Alternative constructors with a {@code Predicate<ResolvableType>} parameter can be used
 * to customize this behavior.
 *
 * @author Sebastien Deleuze
 * @author Iain Henderson
 * @author Arjen Poutsma
 * @since 6.0
 * @param <T> the type of {@link SerialFormat}
 */
public abstract class KotlinSerializationSupport<T extends SerialFormat> {

	// [OCA] 字段 `typeSerializerCache`：类成员状态。
	private final Map<Type, KSerializer<Object>> typeSerializerCache = new ConcurrentReferenceHashMap<>();

	// [OCA] 字段 `kTypeSerializerCache`：类成员状态。
	private final Map<KType, KSerializer<Object>> kTypeSerializerCache = new ConcurrentReferenceHashMap<>();


	// [OCA] 字段 `format`：类成员状态。
	private final T format;

	// [OCA] 字段 `supportedMimeTypes`：类成员状态。
	private final List<MimeType> supportedMimeTypes;

	// [OCA] 字段 `typePredicate`：类成员状态。
	private final Predicate<ResolvableType> typePredicate;

	/**
	 * Creates a new instance with the given format and supported mime types
	 * which only handle types annotated with
	 * {@link kotlinx.serialization.Serializable @Serializable} at type or
	 * generics level.
	 */
	protected KotlinSerializationSupport(T format, MimeType... supportedMimeTypes) {
		this.format = format;
		this.typePredicate = KotlinDetector::hasSerializableAnnotation;
		this.supportedMimeTypes = Arrays.asList(supportedMimeTypes);
	}

	/**
	 * Creates a new instance with the given format and supported mime types
	 * which only encode types for which the specified predicate returns
	 * {@code true}.
	 * @since 7.0
	 */
	protected KotlinSerializationSupport(T format, Predicate<ResolvableType> typePredicate, MimeType... supportedMimeTypes) {
		this.format = format;
		this.typePredicate = typePredicate;
		this.supportedMimeTypes = Arrays.asList(supportedMimeTypes);
	}

	/**
	 * Returns the format.
	 */
	protected final T format() {
		return this.format;
	}

	/**
	 * Returns the supported mime types.
	 */
	protected final List<MimeType> supportedMimeTypes() {
		return this.supportedMimeTypes;
	}

	/**
	 * Indicates whether the given type can be serialized using Kotlin
	 * serialization.
	 * @param type the type to be serialized
	 * @param mimeType the mimetype to use (can be {@code null})
	 * @return {@code true} if {@code type} can be serialized; false otherwise
	 */
	protected final boolean canSerialize(ResolvableType type, @Nullable MimeType mimeType) {
		if (!this.typePredicate.test(type) || ResolvableType.NONE.equals(type)) {
			return false;
		}
		return serializer(type) != null && supports(mimeType) && !ServerSentEvent.class.isAssignableFrom(type.toClass());
	}

	private boolean supports(@Nullable MimeType mimeType) {
		if (mimeType == null) {
			return true;
		}
		for (MimeType candidate : this.supportedMimeTypes) {
			if (candidate.isCompatibleWith(mimeType)) {
				return true;
			}
		}
		return false;
	}


	/**
	 * Returns the serializer that can (de)serialize instances of the given
	 * type. If no serializer can be found, or if {@code resolvableType} is
	 * a <a href="https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#open-polymorphism">open polymorphic</a>
	 * type, {@code null} is returned.
	 * @param resolvableType the type to find a serializer for
	 * @return a resolved serializer for the given type, or {@code null}
	 */
	protected final @Nullable KSerializer<Object> serializer(ResolvableType resolvableType) {
		if (resolvableType.getSource() instanceof MethodParameter parameter) {
			Method method = parameter.getMethod();
			Assert.notNull(method, "Method must not be null");
			if (KotlinDetector.isKotlinType(method.getDeclaringClass())) {
				KFunction<?> function = ReflectJvmMapping.getKotlinFunction(method);
				if (function != null) {
					KType type = (parameter.getParameterIndex() == -1 ? function.getReturnType() :
							KCallables.getValueParameters(function).get(parameter.getParameterIndex()).getType());
					KSerializer<Object> serializer = this.kTypeSerializerCache.get(type);
					if (serializer == null) {
						try {
							serializer = SerializersKt.serializerOrNull(this.format.getSerializersModule(), type);
						}
						catch (IllegalArgumentException ignored) {
						}
						if (serializer != null) {
							this.kTypeSerializerCache.put(type, serializer);
						}
					}
					return serializer;
				}
			}
		}
		Type type = resolvableType.getType();
		KSerializer<Object> serializer = this.typeSerializerCache.get(type);
		if (serializer == null) {
			try {
				serializer = SerializersKt.serializerOrNull(this.format.getSerializersModule(), type);
			}
			catch (IllegalArgumentException ignored) {
			}
			if (serializer != null) {
				this.typeSerializerCache.put(type, serializer);
			}
		}
		return serializer;
	}
}
