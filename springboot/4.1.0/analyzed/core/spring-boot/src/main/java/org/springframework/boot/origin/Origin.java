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

package org.springframework.boot.origin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * 唯一表示某项来源的接口。例如，从 {@link File} 加载的项
 * 其来源可能由文件名以及行号/列号组成。
 * <p>
 * 实现类必须提供合理的 {@code hashCode()}、{@code equals(...)} 与
 * {@code #toString()} 实现。
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 * @since 2.0.0
 * @see OriginProvider
 * @see TextResourceOrigin
 */
public interface Origin {

	/**
	 * 若存在则返回此实例的父来源。父来源表示创建当前项的上级项的来源。
	 *
	 * @return the parent origin or {@code null} 父来源，或 {@code null}
	 * @since 2.4.0
	 * @see Origin#parentsFrom(Object)
	 */
	default @Nullable Origin getParent() {
		return null;
	}

	/**
	 * 查找对象所对应的 {@link Origin}。若源对象是 {@link Origin} 或
	 * {@link OriginProvider} 则直接解析，并会沿异常堆栈继续查找。
	 *
	 * @param source the source object or {@code null} 源对象或 {@code null}
	 * @return an {@link Origin} or {@code null} {@link Origin} 或 {@code null}
	 */
	static @Nullable Origin from(@Nullable Object source) {
		if (source instanceof Origin origin) {
			return origin;
		}
		Origin origin = null;
		if (source instanceof OriginProvider originProvider) {
			origin = originProvider.getOrigin();
		}
		if (origin == null && source instanceof Throwable throwable) {
			return from(throwable.getCause());
		}
		return origin;
	}

	/**
	 * 查找对象所对应 {@link Origin} 的全部父来源。若源对象是 {@link Origin} 或
	 * {@link OriginProvider} 则解析，并会沿异常堆栈继续查找。
	 * 返回从最近父来源到根 {@link Origin} 的列表。
	 *
	 * @param source the source object or {@code null} 源对象或 {@code null}
	 * @return a list of parents or an empty list if the source is {@code null}, has no
	 * origin, or no parent 父来源列表；若源对象为 {@code null}、无来源或无父来源则返回空列表
	 * @since 2.4.0
	 */
	static List<Origin> parentsFrom(@Nullable Object source) {
		Origin origin = from(source);
		if (origin == null) {
			return Collections.emptyList();
		}
		Set<Origin> parents = new LinkedHashSet<>();
		origin = origin.getParent();
		while (origin != null && !parents.contains(origin)) {
			parents.add(origin);
			origin = origin.getParent();
		}
		return Collections.unmodifiableList(new ArrayList<>(parents));
	}

}
