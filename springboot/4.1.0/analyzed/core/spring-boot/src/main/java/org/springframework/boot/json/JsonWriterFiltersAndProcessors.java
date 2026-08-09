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

package org.springframework.boot.json;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.boot.json.JsonWriter.MemberPath;
import org.springframework.boot.json.JsonWriter.NameProcessor;
import org.springframework.boot.json.JsonWriter.ValueProcessor;

/**
 * 内部 record，用于持有 {@link NameProcessor} 与 {@link ValueProcessor} 实例。
 *
 * @author Phillip Webb
 * @param pathFilters the path filters 路径过滤器
 * @param nameProcessors the name processors 名称处理器
 * @param valueProcessors the value processors 值处理器
 */
record JsonWriterFiltersAndProcessors(List<Predicate<MemberPath>> pathFilters, List<NameProcessor> nameProcessors,
		List<ValueProcessor<?>> valueProcessors) {

	JsonWriterFiltersAndProcessors() {
		this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
	}

}
