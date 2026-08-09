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

package org.springframework.boot.env;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * 通过 {@link SpringFactoriesLoader} 定位、用于加载 {@link PropertySource} 的策略接口。
 *
 * @author Dave Syer
 * @author Phillip Webb
 * @since 1.0.0
 */
public interface PropertySourceLoader {

	/**
	 * 返回加载器支持的文件扩展名（不含 {@code .}）。
	 *
	 * @return the file extensions 文件扩展名
	 */
	String[] getFileExtensions();

	/**
	 * 将资源加载为一个或多个属性源。实现可返回仅含单个源的列表，
	 * 对于 yaml 等多文档格式，可为资源中每个文档返回一个源。
	 *
	 * @param name 属性源的根名称；加载多个文档时，每个源应在名称上追加后缀
	 * @param resource 要加载的资源
	 * @return a list property sources 属性源列表
	 * @throws IOException if the source cannot be loaded 无法加载源时
	 */
	List<PropertySource<?>> load(String name, Resource resource) throws IOException;

	/**
	 * 将资源加载为一个或多个属性源。实现可返回仅含单个源的列表，
	 * 对于 yaml 等多文档格式，可为资源中每个文档返回一个源。
	 *
	 * @param name 属性源的根名称；加载多个文档时，每个源应在名称上追加后缀
	 * @param resource 要加载的资源
	 * @param encoding 资源编码
	 * @return a list property sources 属性源列表
	 * @throws IOException if the source cannot be loaded 无法加载源时
	 * @since 4.1.0
	 */
	default List<PropertySource<?>> load(String name, Resource resource, @Nullable Charset encoding)
			throws IOException {
		return load(name, resource);
	}

}
