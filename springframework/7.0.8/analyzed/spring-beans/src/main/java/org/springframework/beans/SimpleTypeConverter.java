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

package org.springframework.beans;

/**
 * {@link TypeConverter} 接口的简单实现，不绑定特定目标对象。
 * 在只需任意类型转换、又不想使用完整 BeanWrapperImpl 时可作为替代，
 * 底层使用完全相同的转换算法（包括委托给 {@link java.beans.PropertyEditor}
 * 与 {@link org.springframework.core.convert.ConversionService}）。
 *
 * <p><b>注意：</b>由于依赖 {@link java.beans.PropertyEditor PropertyEditor}，
 * SimpleTypeConverter <em>不是</em>线程安全的。每个线程应使用独立实例。
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see BeanWrapperImpl
 */
public class SimpleTypeConverter extends TypeConverterSupport {

	/**
	 * 创建 SimpleTypeConverter，并注册默认属性编辑器。
	 */
	public SimpleTypeConverter() {
		this.typeConverterDelegate = new TypeConverterDelegate(this);
		registerDefaultEditors();
	}

}
