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

package org.springframework.context.index;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.ClassUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * 提供对 {@code META-INF/spring.components} 组件索引文件中定义的候选类型
 * （参见 {@link #CandidateComponentsIndex(List)}），或以编程方式注册的候选类型
 * （参见 {@link #CandidateComponentsIndex()}）的访问能力。
 *
 * <p>索引上可注册（并查询）任意数量的构造型（stereotype）：
 * 典型示例是标记某类用于特定场景的注解全限定名。以下调用返回
 * {@code com.example} 包（及其子包）中所有 {@code @Component}
 * <b>候选</b>类型：
 * <pre class="code">
 * Set&lt;String&gt; candidates = index.getCandidateTypes(
 *         "com.example", "org.springframework.stereotype.Component");
 * </pre>
 *
 * <p>{@code type} 通常是类的全限定名，但并非硬性规则。同样，{@code stereotype}
 * 通常是注解类型的全限定名，但也可以是任意标记。
 *
 * @author Stephane Nicoll
 * @author Juergen Hoeller
 * @since 5.0
 */
public class CandidateComponentsIndex {

	private static final AntPathMatcher pathMatcher = new AntPathMatcher(".");

	/** 已注册的扫描基础包（或包模式）。 */
	private final Set<String> registeredScans = new LinkedHashSet<>();

	/** 构造型到候选类型条目的索引。 */
	private final MultiValueMap<String, Entry> index = new LinkedMultiValueMap<>();

	/** 索引是否来自完整解析的索引文件（而非编程式填充）。 */
	private final boolean complete;


	/**
	 * 根据已解析的组件索引文件创建新索引实例。
	 */
	CandidateComponentsIndex(List<Properties> content) {
		for (Properties entry : content) {
			entry.forEach((type, values) -> {
				String[] stereotypes = ((String) values).split(",");
				for (String stereotype : stereotypes) {
					this.index.add(stereotype, new Entry((String) type));
				}
			});
		}
		this.complete = true;
	}

	/**
	 * 创建用于编程式填充的新索引实例。
	 * @since 7.0
	 * @see #registerScan(String...)
	 * @see #registerCandidateType(String, String...)
	 */
	public CandidateComponentsIndex() {
		this.complete = false;
	}


	/**
	 * 以编程方式将给定基础包（或基础包模式）注册为已扫描。
	 * @since 7.0
	 * @see #registerCandidateType(String, String...)
	 */
	public void registerScan(String... basePackages) {
		Collections.addAll(this.registeredScans, basePackages);
	}

	/**
	 * 返回已注册的基础包（或基础包模式）。
	 * @since 7.0
	 * @see #registerScan(String...)
	 */
	public Set<String> getRegisteredScans() {
		return this.registeredScans;
	}

	/**
	 * 判断本索引是否包含给定基础包（或基础包模式）的条目。
	 * @since 7.0
	 */
	public boolean hasScannedPackage(String packageName) {
		return (this.complete ||
				this.registeredScans.stream().anyMatch(basePackage -> matchPackage(basePackage, packageName)));
	}

	/**
	 * 以编程方式为给定候选类型注册一个或多个构造型。
	 * <p>注意：候选类型所在包不会自动视为已扫描包。请确保调用
	 * {@link #registerScan(String...)} 注册相应的扫描基础包。
	 * @since 7.0
	 * @see #registerScan(String...)
	 */
	public void registerCandidateType(String type, String... stereotypes) {
		for (String stereotype : stereotypes) {
			this.index.add(stereotype, new Entry(type));
		}
	}

	/**
	 * 返回已注册的构造型集合（或基础包模式）。
	 * @since 7.0
	 */
	public Set<String> getRegisteredStereotypes() {
		return this.index.keySet();
	}

	/**
	 * 返回与指定构造型关联的候选类型。
	 * @param basePackage 要检查候选类型的包
	 * @param stereotype 要使用的构造型
	 * @return 与指定 {@code stereotype} 关联的候选类型；
	 * 若指定 {@code basePackage} 下未找到则返回空集合
	 */
	public Set<String> getCandidateTypes(String basePackage, String stereotype) {
		List<Entry> candidates = this.index.get(stereotype);
		if (candidates != null) {
			return candidates.stream()
					.filter(entry -> entry.match(basePackage))
					.map(entry -> entry.type)
					.collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}


	private static boolean matchPackage(String basePackage, String packageName) {
		if (pathMatcher.isPattern(basePackage)) {
			return pathMatcher.match(basePackage, packageName);
		}
		else {
			return packageName.equals(basePackage) || packageName.startsWith(basePackage + ".");
		}
	}


	private static class Entry {

		final String type;

		private final String packageName;

		Entry(String type) {
			this.type = type;
			this.packageName = ClassUtils.getPackageName(type);
		}

		public boolean match(String basePackage) {
			return matchPackage(basePackage, this.packageName);
		}
	}

}
