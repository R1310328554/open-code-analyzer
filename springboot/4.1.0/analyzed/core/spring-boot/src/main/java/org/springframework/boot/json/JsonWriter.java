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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.json.JsonValueWriter.Series;
import org.springframework.boot.json.JsonWriter.Member.ValueExtractor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 用于写出 JSON 输出的接口。在无法假定完整序列化库（如 Jackson 或 Gson）依赖时，
 * 通常用于生成 JSON。
 * <p>
 * 对于标准 Java 类型，可使用 {@link #standard()} 工厂方法获取本接口实例。
 * 支持 {@link String}、{@link Number}、{@link Boolean}，以及 {@link Collection}、
 * {@code Array}、{@link Map} 和 {@link WritableJson}。典型用法：
 *
 * <pre class="code">
 * JsonWriter&lt;Map&lt;String,Object&gt;&gt; writer = JsonWriter.standard();
 * writer.write(Map.of("Hello", "World!"), out);
 * </pre>
 * <p>
 * 更复杂的映射可通过 {@link #of(Consumer)} 配合回调配置要写入的 {@link Members JSON 成员}。
 * 典型用法：
 *
 * <pre class="code">
 * JsonWriter&lt;Person&gt; writer = JsonWriter.of((members) -&gt; {
 *     members.add("first", Person::firstName);
 *     members.add("last", Person::lastName);
 *     members.add("dob", Person::dateOfBirth)
 *         .whenNotNull()
 *         .as(DateTimeFormatter.ISO_DATE::format);
 * });
 * writer.write(person, out);
 * </pre>
 * <p>
 * 若需直接写入 {@link String}，可使用 {@link #writeToString(Object)}。
 * 写入其他输出类型时，可通过 {@link #write(Object)} 获取 {@link WritableJson} 实例。
 *
 * @param <T> the type being written 待写入的类型
 * @author Phillip Webb
 * @author Moritz Halbritter
 * @since 3.4.0
 */
@FunctionalInterface
public interface JsonWriter<T> {

	/**
	 * 将给定实例写入提供的 {@link Appendable}。
	 *
	 * @param instance the instance to write (may be {@code null} 待写入实例（可为 {@code null}）
	 * @param out the output that should receive the JSON 接收 JSON 的输出
	 * @throws IOException on IO error IO 异常时
	 */
	void write(@Nullable T instance, Appendable out) throws IOException;

	/**
	 * 将给定实例写入 JSON 字符串。
	 *
	 * @param instance the instance to write (may be {@code null}) 待写入实例（可为 {@code null}）
	 * @return the JSON string JSON 字符串
	 */
	default String writeToString(@Nullable T instance) {
		return write(instance).toJsonString();
	}

	/**
	 * 提供 {@link WritableJson} 实现，用于将给定实例写入多种输出。
	 *
	 * @param instance the instance to write (may be {@code null}) 待写入实例（可为 {@code null}）
	 * @return a {@link WritableJson} instance that may be used to write the JSON 可用于写出 JSON 的 {@link WritableJson} 实例
	 */
	default WritableJson write(@Nullable T instance) {
		return WritableJson.of((out) -> write(instance, out));
	}

	/**
	 * 返回在 JSON 写入结束后追加换行符的新 {@link JsonWriter} 实例。
	 *
	 * @return a new {@link JsonWriter} instance that appends a new line after the JSON 写入后追加换行的新 {@link JsonWriter} 实例
	 */
	default JsonWriter<T> withNewLineAtEnd() {
		return withSuffix("\n");
	}

	/**
	 * 返回在 JSON 写入结束后追加给定后缀的新 {@link JsonWriter} 实例。
	 *
	 * @param suffix the suffix to write, if any 要追加的后缀（若有）
	 * @return a new {@link JsonWriter} instance that appends a suffix after the JSON 写入后追加后缀的新 {@link JsonWriter} 实例
	 */
	default JsonWriter<T> withSuffix(@Nullable String suffix) {
		if (!StringUtils.hasLength(suffix)) {
			return this;
		}
		return (instance, out) -> {
			write(instance, out);
			out.append(suffix);
		};
	}

	/**
	 * 返回适用于标准 Java 类型的 {@link JsonWriter} 工厂方法。
	 * 详见 {@link JsonValueWriter class-level javadoc}。
	 *
	 * @param <T> the type to write 待写入类型
	 * @return a {@link JsonWriter} instance {@link JsonWriter} 实例
	 */
	static <T> JsonWriter<T> standard() {
		return of(Members::add);
	}

	/**
	 * 返回带指定 {@link Members 成员映射} 的 {@link JsonWriter} 工厂方法。
	 * 详见 {@link JsonValueWriter class-level javadoc} 与 {@link Members}。
	 *
	 * @param <T> the type to write 待写入类型
	 * @param members a consumer, which should configure the members 用于配置成员的消费者
	 * @return a {@link JsonWriter} instance {@link JsonWriter} 实例
	 * @see Members
	 */
	static <T> JsonWriter<T> of(Consumer<Members<T>> members) {
		// 勿内联 'new Members'（须在 lambda 外部创建）
		Members<T> initializedMembers = new Members<>(members, false);
		return (instance, out) -> initializedMembers.write(instance, new JsonValueWriter(out));
	}

	/**
	 * 用于配置 JSON 成员的回调。可通过各类 {@code add(...)} 方法声明成员。
	 * 通常以 {@code "name"} 与从实例提取值的 {@link Function} 声明成员；
	 * 也可使用静态值或 {@link Supplier}。
	 * {@link #add(String)} 与 {@link #add()} 可访问正在写入的实际实例。
	 * <p>
	 * 使用 {@code Member.using(...)} 完成定义时，可无 {@code name} 添加成员。
	 * <p>
	 * 可通过 {@code Member.when} 方法过滤成员，并通过 {@link Member#as(Extractor) Member.as(...)} 适配不同类型。
	 *
	 * @param <T> the type that will be written 待写入类型
	 */
	final class Members<T> {

		private final List<Member<?>> members = new ArrayList<>();

		private final boolean contributesPair;

		private final @Nullable Series series;

		private final JsonWriterFiltersAndProcessors jsonProcessors = new JsonWriterFiltersAndProcessors();

		Members(Consumer<Members<T>> members, boolean contributesToExistingSeries) {
			Assert.notNull(members, "'members' must not be null");
			members.accept(this);
			Assert.state(!this.members.isEmpty(), "No members have been added");
			this.contributesPair = this.members.stream().anyMatch(Member::contributesPair);
			this.series = (this.contributesPair && !contributesToExistingSeries) ? Series.OBJECT : null;
			if (this.contributesPair || this.members.size() > 1) {
				this.members.forEach((member) -> Assert.state(member.contributesPair(),
						() -> String.format("%s does not contribute a named pair, ensure that all members have "
								+ "a name or call an appropriate 'using' method", member)));
			}
		}

		/**
		 * 添加可访问正在写入实例的新成员。
		 *
		 * @param name the member name 成员名称
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public Member<T> add(String name) {
			return add(name, (instance) -> instance);
		}

		/**
		 * 添加带静态值的新成员。
		 *
		 * @param <V> the value type 值类型
		 * @param name the member name 成员名称
		 * @param value the member value 成员值
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public <V> Member<V> add(String name, @Nullable V value) {
			return add(name, (instance) -> value);
		}

		/**
		 * 添加由 Supplier 提供值的新成员。
		 *
		 * @param <V> the value type 值类型
		 * @param name the member name 成员名称
		 * @param supplier a supplier of the value 值供应器
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public <V> Member<V> add(String name, Supplier<@Nullable V> supplier) {
			Assert.notNull(supplier, "'supplier' must not be null");
			return add(name, (instance) -> supplier.get());
		}

		/**
		 * 添加通过提取器获取值的新成员。
		 *
		 * @param <V> the value type 值类型
		 * @param name the member name 成员名称
		 * @param extractor {@link Extractor} to extract the value 用于提取值的 {@link Extractor}
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public <V> Member<V> add(String name, Extractor<T, V> extractor) {
			Assert.notNull(name, "'name' must not be null");
			Assert.notNull(extractor, "'extractor' must not be null");
			return addMember(name, extractor);
		}

		/**
		 * 添加可访问正在写入实例的新成员。成员无名称，
		 * 须通过 {@code Member.using(...)} 方法之一完成配置。
		 *
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public Member<T> add() {
			return from((value) -> value);
		}

		/**
		 * 将给定 {@link Map} 的所有条目添加到 JSON。
		 *
		 * @param <M> the map type Map 类型
		 * @param <K> the key type 键类型
		 * @param <V> the value type 值类型
		 * @param extractor {@link Extractor} to extract the map 用于提取 Map 的 {@link Extractor}
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public <M extends Map<K, V>, K, V> Member<M> addMapEntries(Extractor<T, M> extractor) {
			return from(extractor).usingPairs(Map::forEach);
		}

		/**
		 * 从静态值添加成员。须通过 {@code Member.using(...)} 方法之一完成配置。
		 *
		 * @param <V> the value type 值类型
		 * @param value the member value 成员值
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public <V> Member<V> from(@Nullable V value) {
			return from((instance) -> value);
		}

		/**
		 * 从 Supplier 提供的值添加成员。须通过 {@code Member.using(...)} 方法之一完成配置。
		 *
		 * @param <V> the value type 值类型
		 * @param supplier a supplier of the value 值供应器
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public <V> Member<V> from(Supplier<@Nullable V> supplier) {
			Assert.notNull(supplier, "'supplier' must not be null");
			return from((instance) -> supplier.get());
		}

		/**
		 * 从提取的值添加成员。须通过 {@code Member.using(...)} 方法之一完成配置。
		 *
		 * @param <V> the value type 值类型
		 * @param extractor {@link Extractor} to extract the value 用于提取值的 {@link Extractor}
		 * @return the added {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public <V> Member<V> from(Extractor<T, V> extractor) {
			Assert.notNull(extractor, "'extractor' must not be null");
			return addMember(null, extractor);
		}

		/**
		 * 添加用于限制写入 JSON 的成员的过滤器。
		 *
		 * @param predicate the predicate used to filter members 用于过滤成员的谓词
		 */
		public void applyingPathFilter(Predicate<MemberPath> predicate) {
			Assert.notNull(predicate, "'predicate' must not be null");
			this.jsonProcessors.pathFilters().add(predicate);
		}

		/**
		 * 添加写入 JSON 时应用的 {@link NameProcessor}。
		 *
		 * @param nameProcessor the name processor to add 要添加的名称处理器
		 */
		public void applyingNameProcessor(NameProcessor nameProcessor) {
			Assert.notNull(nameProcessor, "'nameProcessor' must not be null");
			this.jsonProcessors.nameProcessors().add(nameProcessor);
		}

		/**
		 * 添加写入 JSON 时应用的 {@link ValueProcessor}。
		 *
		 * @param valueProcessor the value processor to add 要添加的值处理器
		 */
		public void applyingValueProcessor(ValueProcessor<?> valueProcessor) {
			Assert.notNull(valueProcessor, "'valueProcessor' must not be null");
			this.jsonProcessors.valueProcessors().add(valueProcessor);
		}

		private <V> Member<V> addMember(@Nullable String name, Extractor<T, V> extractor) {
			Member<V> member = new Member<>(this.members.size(), name, ValueExtractor.of(extractor));
			this.members.add(member);
			return member;
		}

		/**
		 * 使用已配置的 {@link Member 成员} 写入给定实例。
		 *
		 * @param instance the instance to write 待写入实例
		 * @param valueWriter the JSON value writer to use 使用的 JSON 值写入器
		 */
		void write(@Nullable T instance, JsonValueWriter valueWriter) {
			valueWriter.pushProcessors(this.jsonProcessors);
			valueWriter.start(this.series);
			for (Member<?> member : this.members) {
				member.write(instance, valueWriter);
			}
			valueWriter.end(this.series);
			valueWriter.popProcessors();
		}

		/**
		 * 返回是否有任一成员向 JSON 贡献名值对。
		 *
		 * @return if a name/value pair is contributed 是否贡献名值对
		 */
		boolean contributesPair() {
			return this.contributesPair;
		}

	}

	/**
	 * 向 JSON 贡献内容的成员。通常基于提取值贡献单个名值对；
	 * 配置 {@code using(...)} 方法后也可贡献更复杂的 JSON 结构。
	 * <p>
	 * {@code when(...)} 方法可过滤成员（从 JSON 中完全省略）。
	 * {@link #as(Extractor)} 可将值适配为不同类型。
	 *
	 * @param <T> the member type 成员类型
	 */
	final class Member<T> {

		private final int index;

		private final @Nullable String name;

		private ValueExtractor<? extends @Nullable T> valueExtractor;

		private @Nullable BiConsumer<T, BiConsumer<?, ?>> pairs;

		private @Nullable Members<T> members;

		Member(int index, @Nullable String name, ValueExtractor<? extends @Nullable T> valueExtractor) {
			this.index = index;
			this.name = name;
			this.valueExtractor = valueExtractor;
		}

		/**
		 * 仅当值不为 {@code null} 时包含此成员。
		 *
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public Member<T> whenNotNull() {
			return when(Objects::nonNull);
		}

		/**
		 * 仅当提取的值不为 {@code null} 时包含此成员。
		 *
		 * @param extractor a function used to extract the value to test 用于提取待测值的函数
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public Member<T> whenNotNull(Function<@Nullable T, ?> extractor) {
			Assert.notNull(extractor, "'extractor' must not be null");
			return when((instance) -> Objects.nonNull(extractor.apply(instance)));
		}

		/**
		 * 仅当成员不为 {@code null} 且 {@link Object#toString() toString()} 非空时包含。
		 *
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 * @see StringUtils#hasLength(CharSequence)
		 */
		public Member<T> whenHasLength() {
			return when((instance) -> instance != null && StringUtils.hasLength(instance.toString()));
		}

		/**
		 * 仅当成员非空时包含（详见 {@link ObjectUtils#isEmpty(Object)}）。
		 *
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public Member<T> whenNotEmpty() {
			Predicate<@Nullable T> isEmpty = ObjectUtils::isEmpty;
			return whenNot(isEmpty);
		}

		/**
		 * 仅当给定谓词不匹配时包含此成员。
		 *
		 * @param predicate the predicate to test 待测谓词
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public Member<T> whenNot(Predicate<@Nullable T> predicate) {
			Assert.notNull(predicate, "'predicate' must not be null");
			return when(predicate.negate());
		}

		/**
		 * 仅当给定谓词匹配时包含此成员。
		 *
		 * @param predicate the predicate to test 待测谓词
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		public Member<T> when(Predicate<? super @Nullable T> predicate) {
			Assert.notNull(predicate, "'predicate' must not be null");
			this.valueExtractor = this.valueExtractor.when(predicate);
			return this;
		}

		/**
		 * 通过给定 {@link Function} 适配值。
		 *
		 * @param <R> the result type 结果类型
		 * @param extractor a {@link Extractor} to adapt the value 用于适配值的 {@link Extractor}
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 */
		@SuppressWarnings("unchecked")
		public <R> Member<R> as(Extractor<T, R> extractor) {
			Assert.notNull(extractor, "'adapter' must not be null");
			Member<R> result = (Member<R>) this;
			result.valueExtractor = this.valueExtractor.as(extractor::extract);
			return result;
		}

		/**
		 * 从一系列元素提取值并添加 JSON 名值对。
		 * 通常配合 {@link Iterable#forEach(Consumer)} 使用，例如：
		 *
		 * <pre class="code">
		 * members.add(Event::getTags).usingExtractedPairs(Iterable::forEach, pairExtractor);
		 * </pre>
		 * <p>
		 * 用于具名成员时，名值对作为新的 JSON 对象写入：
		 *
		 * <pre>
		 * {
		 *   "name": {
		 *     "p1": 1,
		 *     "p2": 2
		 *   }
		 * }
		 * </pre>
		 *
		 * 用于无名成员时，名值对合并到现有 JSON 对象：
		 *
		 * <pre>
		 * {
		 *   "p1": 1,
		 *   "p2": 2
		 * }
		 * </pre>
		 * @param <E> the element type 元素类型
		 * @param elements callback used to provide the elements 提供元素的回调
		 * @param extractor a {@link PairExtractor} used to extract the name/value pair 提取名值对的 {@link PairExtractor}
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 * @see #usingExtractedPairs(BiConsumer, Function, Function)
		 * @see #usingPairs(BiConsumer)
		 */
		public <E> Member<T> usingExtractedPairs(BiConsumer<T, Consumer<E>> elements, PairExtractor<E> extractor) {
			Assert.notNull(elements, "'elements' must not be null");
			Assert.notNull(extractor, "'extractor' must not be null");
			return usingExtractedPairs(elements, extractor::getName, extractor::getValue);
		}

		/**
		 * 从一系列元素提取值并添加 JSON 名值对。
		 * 通常配合 {@link Iterable#forEach(Consumer)} 使用，例如：
		 *
		 * <pre class="code">
		 * members.add(Event::getTags).usingExtractedPairs(Iterable::forEach, Tag::getName, Tag::getValue);
		 * </pre>
		 * <p>
		 * 用于具名成员时，名值对作为新的 JSON 对象写入：
		 *
		 * <pre>
		 * {
		 *   "name": {
		 *     "p1": 1,
		 *     "p2": 2
		 *   }
		 * }
		 * </pre>
		 *
		 * 用于无名成员时，名值对合并到现有 JSON 对象：
		 *
		 * <pre>
		 * {
		 *   "p1": 1,
		 *   "p2": 2
		 * }
		 * </pre>
		 * @param <E> the element type 元素类型
		 * @param <N> the name type 名称类型
		 * @param <V> the value type 值类型
		 * @param elements callback used to provide the elements 提供元素的回调
		 * @param nameExtractor {@link Function} used to extract the name 提取名称的 {@link Function}
		 * @param valueExtractor {@link Function} used to extract the value 提取值的 {@link Function}
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 * @see #usingExtractedPairs(BiConsumer, PairExtractor)
		 * @see #usingPairs(BiConsumer)
		 */
		public <E, N, V> Member<T> usingExtractedPairs(BiConsumer<T, Consumer<E>> elements,
				Function<E, N> nameExtractor, Function<E, V> valueExtractor) {
			Assert.notNull(elements, "'elements' must not be null");
			Assert.notNull(nameExtractor, "'nameExtractor' must not be null");
			Assert.notNull(valueExtractor, "'valueExtractor' must not be null");
			return usingPairs((instance, pairsConsumer) -> elements.accept(instance, (element) -> {
				N name = nameExtractor.apply(element);
				V value = valueExtractor.apply(element);
				pairsConsumer.accept(name, value);
			}));
		}

		/**
		 * 添加 JSON 名值对。通常配合 {@link Map#forEach(BiConsumer)} 使用，例如：
		 *
		 * <pre class="code">
		 * members.add(Event::getLabels).usingPairs(Map::forEach);
		 * </pre>
		 * <p>
		 * 用于具名成员时，名值对作为新的 JSON 对象写入：
		 *
		 * <pre>
		 * {
		 *   "name": {
		 *     "p1": 1,
		 *     "p2": 2
		 *   }
		 * }
		 * </pre>
		 *
		 * 用于无名成员时，名值对合并到现有 JSON 对象：
		 *
		 * <pre>
		 * {
		 *   "p1": 1,
		 *   "p2": 2
		 * }
		 * </pre>
		 * @param <N> the name type 名称类型
		 * @param <V> the value type 值类型
		 * @param pairs callback used to provide the pairs 提供名值对的回调
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 * @see #usingExtractedPairs(BiConsumer, PairExtractor)
		 * @see #usingPairs(BiConsumer)
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <N, V> Member<T> usingPairs(BiConsumer<T, BiConsumer<N, V>> pairs) {
			Assert.notNull(pairs, "'pairs' must not be null");
			Assert.state(this.pairs == null, "Pairs cannot be declared multiple times");
			Assert.state(this.members == null, "Pairs cannot be declared when using members");
			this.pairs = (BiConsumer) pairs;
			return this;
		}

		/**
		 * 基于进一步 {@link Members} 配置添加 JSON。例如：
		 *
		 * <pre class="code">
		 * members.add(User::getName).usingMembers((personMembers) -> {
		 *     personMembers.add("first", Name::first);
		 *     personMembers.add("last", Name::last);
		 * });
		 * </pre>
		 *
		 * <p>
		 * 用于具名成员时，结果作为新的 JSON 对象写入：
		 *
		 * <pre>
		 * {
		 *   "name": {
		 *     "first": "Jane",
		 *     "last": "Doe"
		 *   }
		 * }
		 * </pre>
		 *
		 * 用于无名成员时，结果合并到现有 JSON 对象：
		 *
		 * <pre>
		 * {
		 *   "first": "John",
		 *   "last": "Doe"
		 * }
		 * </pre>
		 * @param members callback to configure the members 配置成员的回调
		 * @return a {@link Member} which may be configured further 可继续配置的 {@link Member}
		 * @see #usingExtractedPairs(BiConsumer, PairExtractor)
		 * @see #usingPairs(BiConsumer)
		 */
		public Member<T> usingMembers(Consumer<Members<T>> members) {
			Assert.notNull(members, "'members' must not be null");
			Assert.state(this.members == null, "Members cannot be declared multiple times");
			Assert.state(this.pairs == null, "Members cannot be declared when using pairs");
			this.members = new Members<>(members, this.name == null);
			return this;
		}

		/**
		 * 使用本成员配置的详情写入给定实例。
		 *
		 * @param instance the instance to write 待写入实例
		 * @param valueWriter the JSON value writer to use 使用的 JSON 值写入器
		 */
		void write(@Nullable Object instance, JsonValueWriter valueWriter) {
			T extracted = this.valueExtractor.extract(instance);
			if (ValueExtractor.skip(extracted)) {
				return;
			}
			Object value = getValueToWrite(extracted, valueWriter);
			valueWriter.write(this.name, value);
		}

		private @Nullable Object getValueToWrite(@Nullable T extracted, JsonValueWriter valueWriter) {
			WritableJson writableJson = getWritableJsonToWrite(extracted, valueWriter);
			return (writableJson != null) ? WritableJson.of(writableJson) : extracted;
		}

		private @Nullable WritableJson getWritableJsonToWrite(@Nullable T extracted, JsonValueWriter valueWriter) {
			BiConsumer<T, BiConsumer<?, ?>> pairs = this.pairs;
			if (pairs != null) {
				return (out) -> valueWriter.writePairs((outPairs) -> pairs.accept(extracted, outPairs));
			}
			Members<T> members = this.members;
			if (members != null) {
				return (out) -> members.write(extracted, valueWriter);
			}
			return null;
		}

		/**
		 * 是否向 JSON 贡献一个或多个名值对。
		 *
		 * @return whether a name/value pair is contributed 是否贡献名值对
		 */
		boolean contributesPair() {
			return this.name != null || this.pairs != null || (this.members != null && this.members.contributesPair());
		}

		@Override
		public String toString() {
			return "Member at index " + this.index + ((this.name != null) ? "{%s}".formatted(this.name) : "");
		}

		/**
		 * 管理成员值提取与过滤的内部类。
		 *
		 * @param <T> the member type 成员类型
		 */
		@FunctionalInterface
		interface ValueExtractor<T extends @Nullable Object> {

			/**
			 * 表示应跳过的值。
			 */
			Object SKIP = new Object();

			/**
			 * 从给定实例提取值。
			 *
			 * @param instance the source instance 源实例
			 * @return the extracted value or {@link #SKIP} 提取的值或 {@link #SKIP}
			 */
			@Nullable T extract(@Nullable Object instance);

			/**
			 * 仅当给定谓词匹配时提取。
			 *
			 * @param predicate the predicate to test 待测谓词
			 * @return a new {@link ValueExtractor} 新的 {@link ValueExtractor}
			 */
			default ValueExtractor<T> when(Predicate<? super @Nullable T> predicate) {
				return (instance) -> test(extract(instance), predicate);
			}

			@SuppressWarnings("unchecked")
			private @Nullable T test(@Nullable T extracted, Predicate<? super @Nullable T> predicate) {
				return (!skip(extracted) && predicate.test(extracted)) ? extracted : (T) SKIP;
			}

			/**
			 * 适配提取的值。
			 *
			 * @param <R> the result type 结果类型
			 * @param extractor the extractor to use 使用的提取器
			 * @return a new {@link ValueExtractor} 新的 {@link ValueExtractor}
			 */
			default <R> ValueExtractor<R> as(Extractor<T, R> extractor) {
				return (instance) -> apply(extract(instance), extractor);
			}

			@SuppressWarnings("unchecked")
			private <R> @Nullable R apply(@Nullable T value, Extractor<T, R> extractor) {
				if (skip(value)) {
					return (R) SKIP;
				}
				return (value != null) ? extractor.extract(value) : null;
			}

			/**
			 * 基于给定 {@link Function} 创建新的 {@link ValueExtractor}。
			 *
			 * @param <S> the source type 源类型
			 * @param <T> the extracted type 提取类型
			 * @param extractor the extractor to use 使用的提取器
			 * @return a new {@link ValueExtractor} instance 新的 {@link ValueExtractor} 实例
			 */
			@SuppressWarnings("unchecked")
			static <S, T> ValueExtractor<T> of(Extractor<S, T> extractor) {
				return (instance) -> {
					if (instance == null) {
						return null;
					}
					return (skip(instance)) ? (T) SKIP : extractor.extract((S) instance);
				};
			}

			/**
			 * 返回提取的值是否应跳过。
			 *
			 * @param <T> the value type 值类型
			 * @param extracted the value to test 待测值
			 * @return if the value is to be skipped 是否跳过
			 */
			static <T> boolean skip(@Nullable T extracted) {
				return extracted == SKIP;
			}

		}

	}

	/**
	 * 用于标识特定 JSON 成员的路径。路径可表示为字符串，
	 * 形式如 {@code "my.json[1].item"}，元素以 {@code '.'} 或 {@code [<index>]} 分隔。
	 * 保留字符使用 {@code '\'} 转义。
	 *
	 * @param parent the parent of this path 父路径
	 * @param name the name of the member or {@code null} if the member is indexed. Path
	 * names are provided as they were defined when the member was added and do not
	 * include any {@link NameProcessor name processing}. 成员名称；索引成员时为 {@code null}（为添加时的原始名称，不含 {@link NameProcessor 名称处理}）
	 * @param index the index of the member or {@link MemberPath#UNINDEXED} 成员索引或 {@link MemberPath#UNINDEXED}
	 */
	record MemberPath(@Nullable MemberPath parent, @Nullable String name, int index) {

		private static final String[] ESCAPED = { "\\", ".", "[", "]" };

		public MemberPath {
			Assert.isTrue((name != null && index < 0) || (name == null && index >= 0),
					"'name' and 'index' cannot be mixed");
		}

		/**
		 * 表示成员无索引。
		 */
		public static final int UNINDEXED = -1;

		/**
		 * 所有成员路径的根。
		 */
		static final MemberPath ROOT = new MemberPath(null, "", UNINDEXED);

		/**
		 * 创建带指定索引的子路径。
		 *
		 * @param index the index of the child 子路径索引
		 * @return a new {@link MemberPath} instance 新的 {@link MemberPath} 实例
		 */
		public MemberPath child(int index) {
			return new MemberPath(this, null, index);
		}

		/**
		 * 创建带指定名称的子路径。
		 *
		 * @param name the name of the child 子路径名称
		 * @return a new {@link MemberPath} instance 新的 {@link MemberPath} 实例
		 */
		public MemberPath child(String name) {
			return (!StringUtils.hasLength(name)) ? this : new MemberPath(this, name, UNINDEXED);
		}

		@Override
		public String toString() {
			return toString(true);
		}

		/**
		 * 返回未转义的路径字符串表示。
		 *
		 * @return the unescaped string representation 未转义的字符串表示
		 */
		public String toUnescapedString() {
			return toString(false);
		}

		private String toString(boolean escape) {
			StringBuilder string = new StringBuilder((this.parent != null) ? this.parent.toString(escape) : "");
			if (this.index >= 0) {
				string.append("[").append(this.index).append("]");
			}
			else {
				string.append((!string.isEmpty()) ? "." : "").append((!escape) ? this.name : escape(this.name));
			}
			return string.toString();
		}

		private @Nullable String escape(@Nullable String name) {
			if (name == null) {
				return null;
			}
			for (String escape : ESCAPED) {
				name = name.replace(escape, "\\" + escape);
			}
			return name;
		}

		/**
		 * 从给定字符串创建新的 {@link MemberPath} 实例。
		 *
		 * @param value the path value 路径字符串
		 * @return a new {@link MemberPath} instance 新的 {@link MemberPath} 实例
		 */
		public static MemberPath of(String value) {
			MemberPath path = MemberPath.ROOT;
			StringBuilder buffer = new StringBuilder();
			boolean escape = false;
			for (char ch : value.toCharArray()) {
				if (!escape && ch == '\\') {
					escape = true;
				}
				else if (!escape && (ch == '.' || ch == '[')) {
					path = path.child(buffer.toString());
					buffer.setLength(0);
				}
				else if (!escape && ch == ']') {
					path = path.child(Integer.parseUnsignedInt(buffer.toString()));
					buffer.setLength(0);
				}
				else {
					buffer.append(ch);
					escape = false;
				}
			}
			path = path.child(buffer.toString());
			return path;
		}

	}

	/**
	 * 从元素提取名值对的接口。
	 *
	 * @param <E> the element type 元素类型
	 */
	interface PairExtractor<E> {

		/**
		 * 提取名称。
		 *
		 * @param <N> the name type 名称类型
		 * @param element the source element 源元素
		 * @return the extracted name 提取的名称
		 */
		<N> N getName(E element);

		/**
		 * 提取值。
		 *
		 * @param <V> the value type 值类型
		 * @param element the source element 源元素
		 * @return the extracted value 提取的值
		 */
		<V> V getValue(E element);

		/**
		 * 使用独立的名称与值提取函数创建 {@link PairExtractor} 的工厂方法。
		 *
		 * @param <T> the element type 元素类型
		 * @param nameExtractor the name extractor 名称提取器
		 * @param valueExtractor the value extraction 值提取器
		 * @return a new {@link PairExtractor} instance 新的 {@link PairExtractor} 实例
		 */
		static <T> PairExtractor<T> of(Function<T, ?> nameExtractor, Function<T, ?> valueExtractor) {
			Assert.notNull(nameExtractor, "'nameExtractor' must not be null");
			Assert.notNull(valueExtractor, "'valueExtractor' must not be null");
			return new PairExtractor<>() {

				@Override
				@SuppressWarnings("unchecked")
				public <N> N getName(T instance) {
					return (N) nameExtractor.apply(instance);
				}

				@Override
				@SuppressWarnings("unchecked")
				public <V> V getValue(T instance) {
					return (V) valueExtractor.apply(instance);
				}

			};
		}

	}

	/**
	 * 可 {@link Members#applyingNameProcessor(NameProcessor) 应用}于 {@link Members} 的回调接口，
	 * 用于更改名称或过滤成员。
	 */
	@FunctionalInterface
	interface NameProcessor {

		/**
		 * 返回 JSON 成员的新名称；若应完全过滤该成员则返回 {@code null}。
		 *
		 * @param path the path of the member 成员路径
		 * @param existingName the existing and possibly already processed name. 现有名称（可能已处理）
		 * @return the new name 新名称
		 */
		@Nullable String processName(MemberPath path, String existingName);

		/**
		 * 为给定操作创建新 {@link NameProcessor} 的工厂方法。
		 *
		 * @param operation the operation to apply 要应用的操作
		 * @return a new {@link NameProcessor} instance 新的 {@link NameProcessor} 实例
		 */
		static NameProcessor of(UnaryOperator<String> operation) {
			Assert.notNull(operation, "'operation' must not be null");
			return (path, existingName) -> operation.apply(existingName);
		}

	}

	/**
	 * 可 {@link Members#applyingValueProcessor(ValueProcessor) 应用}于 {@link Members} 的回调接口，
	 * 在写入前处理值。通常用于过滤值，例如减少冗余信息或脱敏敏感数据。
	 *
	 * @param <T> the value type 值类型
	 */
	@FunctionalInterface
	interface ValueProcessor<T extends @Nullable Object> {

		/**
		 * 处理给定路径处的值。
		 *
		 * @param path the path of the member containing the value 包含该值的成员路径
		 * @param value the value being written (may be {@code null}) 待写入的值（可为 {@code null}）
		 * @return the processed value 处理后的值
		 */
		@Nullable T processValue(MemberPath path, @Nullable T value);

		/**
		 * 返回仅应用于给定路径（忽略转义字符）成员的新处理器。
		 *
		 * @param path the patch to match 要匹配的路径
		 * @return a new {@link ValueProcessor} that only applies when the path matches 路径匹配时才应用的新 {@link ValueProcessor}
		 */
		default ValueProcessor<T> whenHasUnescapedPath(String path) {
			return whenHasPath((candidate) -> candidate.toString(false).equals(path));
		}

		/**
		 * 返回仅应用于给定路径成员的新处理器。
		 *
		 * @param path the patch to match 要匹配的路径
		 * @return a new {@link ValueProcessor} that only applies when the path matches 路径匹配时才应用的新 {@link ValueProcessor}
		 */
		default ValueProcessor<T> whenHasPath(String path) {
			return whenHasPath(MemberPath.of(path)::equals);
		}

		/**
		 * 返回仅应用于匹配给定路径谓词的成员的新处理器。
		 *
		 * @param predicate the predicate that must match 必须匹配的谓词
		 * @return a new {@link ValueProcessor} that only applies when the predicate
		 * matches 谓词匹配时才应用的新 {@link ValueProcessor}
		 */
		default ValueProcessor<T> whenHasPath(Predicate<MemberPath> predicate) {
			return (path, value) -> (predicate.test(path)) ? processValue(path, value) : value;
		}

		/**
		 * 返回仅应用于给定类型值的新处理器。
		 *
		 * @param type the type that must match 必须匹配的类型
		 * @return a new {@link ValueProcessor} that only applies when value is the given
		 * type. 值类型匹配时才应用的新 {@link ValueProcessor}
		 */
		default ValueProcessor<T> whenInstanceOf(Class<?> type) {
			Predicate<@Nullable T> isInstance = type::isInstance;
			return when(isInstance);
		}

		/**
		 * 返回仅应用于匹配给定谓词的值的新处理器。
		 *
		 * @param predicate the predicate that must match 必须匹配的谓词
		 * @return a new {@link ValueProcessor} that only applies when the predicate
		 * matches 谓词匹配时才应用的新 {@link ValueProcessor}
		 */
		default ValueProcessor<T> when(Predicate<@Nullable T> predicate) {
			return (name, value) -> (predicate.test(value)) ? processValue(name, value) : value;
		}

		/**
		 * 创建应用给定操作的新 {@link ValueProcessor} 的工厂方法。
		 *
		 * @param <T> the value type 值类型
		 * @param type the value type 值类型
		 * @param action the action to apply 要应用的操作
		 * @return a new {@link ValueProcessor} instance 新的 {@link ValueProcessor} 实例
		 */
		static <T> ValueProcessor<T> of(Class<? extends T> type, UnaryOperator<@Nullable T> action) {
			return of(action).whenInstanceOf(type);
		}

		/**
		 * 创建应用给定操作的新 {@link ValueProcessor} 的工厂方法。
		 *
		 * @param <T> the value type 值类型
		 * @param action the action to apply 要应用的操作
		 * @return a new {@link ValueProcessor} instance 新的 {@link ValueProcessor} 实例
		 */
		static <T> ValueProcessor<T> of(UnaryOperator<@Nullable T> action) {
			Assert.notNull(action, "'action' must not be null");
			return (name, value) -> action.apply(value);
		}

	}

	/**
	 * 从一个值提取另一个值的接口。
	 *
	 * @param <T> the source type 源类型
	 * @param <R> the result type 结果类型
	 */
	@FunctionalInterface
	interface Extractor<T extends @Nullable Object, R extends @Nullable Object> {

		/**
		 * 从给定值提取。
		 *
		 * @param value the source value (never {@code null}) 源值（永不为 {@code null}）
		 * @return an extracted value or {@code null} 提取的值或 {@code null}
		 */
		@Nullable R extract(@NonNull T value);

	}

}
