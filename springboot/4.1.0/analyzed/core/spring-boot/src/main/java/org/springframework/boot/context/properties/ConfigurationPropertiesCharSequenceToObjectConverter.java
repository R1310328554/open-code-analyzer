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

package org.springframework.boot.context.properties;

import java.util.Collections;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

/**
 * 包私有类 {@code org.springframework.boot.convert.CharSequenceToObjectConverter} 的副本，
 * 重命名以作区分。
 *
 * @author Phillip Webb
 * @author Andy Wilkinson
 */
final class ConfigurationPropertiesCharSequenceToObjectConverter implements ConditionalGenericConverter {

	private static final TypeDescriptor STRING = TypeDescriptor.valueOf(String.class);

	private static final TypeDescriptor BYTE_ARRAY = TypeDescriptor.valueOf(byte[].class);

	private static final Set<ConvertiblePair> TYPES;

	private final ThreadLocal<Boolean> disable = new ThreadLocal<>();

	static {
		TYPES = Collections.singleton(new ConvertiblePair(CharSequence.class, Object.class));
	}

	private final ConversionService conversionService;

	ConfigurationPropertiesCharSequenceToObjectConverter(ConversionService conversionService) {
		this.conversionService = conversionService;
	}

	@Override
	public Set<ConvertiblePair> getConvertibleTypes() {
		return TYPES;
	}

	@Override
	public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (sourceType.getType() == String.class || this.disable.get() == Boolean.TRUE) {
			return false;
		}
		this.disable.set(Boolean.TRUE);
		try {
			boolean canDirectlyConvertCharSequence = this.conversionService.canConvert(sourceType, targetType);
			if (canDirectlyConvertCharSequence && !isStringConversionBetter(sourceType, targetType)) {
				return false;
			}
			return this.conversionService.canConvert(STRING, targetType);
		}
		finally {
			this.disable.remove();
		}
	}

	/**
	 * 根据目标类型判断是否基于 String 的转换更优。
	 * 当 ObjectTo... 转换产生错误结果时需要此判断。
	 *
	 * @param sourceType 待测试的源类型
	 * @param targetType 待测试的目标类型
	 * @return 若 String 转换更优则为 {@code true}
	 */
	private boolean isStringConversionBetter(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (this.conversionService instanceof ApplicationConversionService applicationConversionService) {
			if (applicationConversionService.isConvertViaObjectSourceType(sourceType, targetType)) {
				// If an ObjectTo... converter is being used then there might be a
				// better StringTo... version
				return true;
			}
		}
		// StringToArrayConverter / StringToCollectionConverter are better than
		// ObjectToArrayConverter / ObjectToCollectionConverter
		return (targetType.isArray() || targetType.isCollection()) && !targetType.equals(BYTE_ARRAY);
	}

	@Override
	public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (source == null) {
			return null;
		}
		return this.conversionService.convert(source.toString(), STRING, targetType);
	}

}
