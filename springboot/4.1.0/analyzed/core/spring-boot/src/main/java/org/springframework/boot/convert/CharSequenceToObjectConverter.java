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

package org.springframework.boot.convert;

import java.util.Collections;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;

/**
 * 通过委托现有 {@link String} 转换器来转换 {@link CharSequence} 类型的
 * {@link ConditionalGenericConverter}。
 *
 * @author Phillip Webb
 */
class CharSequenceToObjectConverter implements ConditionalGenericConverter {

	private static final TypeDescriptor STRING = TypeDescriptor.valueOf(String.class);

	private static final TypeDescriptor BYTE_ARRAY = TypeDescriptor.valueOf(byte[].class);

	private static final Set<ConvertiblePair> TYPES;

	private final ThreadLocal<Boolean> disable = new ThreadLocal<>();

	static {
		TYPES = Collections.singleton(new ConvertiblePair(CharSequence.class, Object.class));
	}

	private final ConversionService conversionService;

	CharSequenceToObjectConverter(ConversionService conversionService) {
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
	 * 根据目标类型判断是否基于 String 的转换更优。当 ObjectTo... 转换产生错误结果时需要此方法。
	 *
	 * @param sourceType 待测试的源类型
	 * @param targetType 待测试的目标类型
	 * @return 基于 String 的转换是否更优
	 */
	private boolean isStringConversionBetter(TypeDescriptor sourceType, TypeDescriptor targetType) {
		if (this.conversionService instanceof ApplicationConversionService applicationConversionService) {
			if (applicationConversionService.isConvertViaObjectSourceType(sourceType, targetType)) {
				// If an ObjectTo... converter is being used then there might be a better
				// StringTo... version
				return true;
			}
		}
		if ((targetType.isArray() || targetType.isCollection()) && !targetType.equals(BYTE_ARRAY)) {
			// StringToArrayConverter / StringToCollectionConverter are better than
			// ObjectToArrayConverter / ObjectToCollectionConverter
			return true;
		}
		return false;
	}

	@Override
	public @Nullable Object convert(@Nullable Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
		return this.conversionService.convert((source != null) ? source.toString() : null, STRING, targetType);
	}

}
