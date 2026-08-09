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

package org.springframework.boot.autoconfigure.condition;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 与 {@link ConditionOutcome} 关联的消息。提供流式构建器风格 API，
 * 以促使所有条件消息保持一致。
 *
 * @author Phillip Webb
 * @since 1.4.1
 */
public final class ConditionMessage {

	private final @Nullable String message;

	private ConditionMessage() {
		this(null);
	}

	private ConditionMessage(@Nullable String message) {
		this.message = message;
	}

	private ConditionMessage(ConditionMessage prior, String message) {
		this.message = prior.isEmpty() ? message : prior + "; " + message;
	}

	/**
	 * 若消息为空则返回 {@code true}。
	 * @return 消息是否为空
	 */
	public boolean isEmpty() {
		return !StringUtils.hasLength(this.message);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof ConditionMessage other) {
			return ObjectUtils.nullSafeEquals(other.message, this.message);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(this.message);
	}

	@Override
	public String toString() {
		return (this.message != null) ? this.message : "";
	}

	/**
	 * 基于当前实例及追加消息返回新的 {@link ConditionMessage}。
	 * @param message 要追加的消息
	 * @return 新的 {@link ConditionMessage} 实例
	 */
	public ConditionMessage append(@Nullable String message) {
		if (!StringUtils.hasLength(message)) {
			return this;
		}
		if (!StringUtils.hasLength(this.message)) {
			return new ConditionMessage(message);
		}

		return new ConditionMessage(this.message + " " + message);
	}

	/**
	 * 基于当前实例及新的条件评估结果，返回用于构建新 {@link ConditionMessage} 的构建器。
	 * @param condition 条件
	 * @param details 条件详情
	 * @return {@link Builder} 构建器
	 * @see #andCondition(String, Object...)
	 * @see #forCondition(Class, Object...)
	 */
	public Builder andCondition(Class<? extends Annotation> condition, Object... details) {
		Assert.notNull(condition, "'condition' must not be null");
		return andCondition("@" + ClassUtils.getShortName(condition), details);
	}

	/**
	 * 基于当前实例及新的条件评估结果，返回用于构建新 {@link ConditionMessage} 的构建器。
	 * @param condition 条件
	 * @param details 条件详情
	 * @return {@link Builder} 构建器
	 * @see #andCondition(Class, Object...)
	 * @see #forCondition(String, Object...)
	 */
	public Builder andCondition(String condition, Object... details) {
		Assert.notNull(condition, "'condition' must not be null");
		String detail = StringUtils.arrayToDelimitedString(details, " ");
		if (StringUtils.hasLength(detail)) {
			return new Builder(condition + " " + detail);
		}
		return new Builder(condition);
	}

	/**
	 * 返回新的空 {@link ConditionMessage} 的工厂方法。
	 * @return 新的空 {@link ConditionMessage}
	 */
	public static ConditionMessage empty() {
		return new ConditionMessage();
	}

	/**
	 * 使用指定消息创建新 {@link ConditionMessage} 的工厂方法。
	 * @param message 源消息（若指定 {@code args} 则可为格式字符串）
	 * @param args 消息的格式参数
	 * @return 新的 {@link ConditionMessage} 实例
	 */
	public static ConditionMessage of(String message, Object... args) {
		if (ObjectUtils.isEmpty(args)) {
			return new ConditionMessage(message);
		}
		return new ConditionMessage(String.format(message, args));
	}

	/**
	 * 由指定消息组合创建新 {@link ConditionMessage} 的工厂方法。
	 * @param messages 源消息（可为 {@code null}）
	 * @return 新的 {@link ConditionMessage} 实例
	 */
	public static ConditionMessage of(@Nullable Collection<? extends ConditionMessage> messages) {
		ConditionMessage result = new ConditionMessage();
		if (messages != null) {
			for (ConditionMessage message : messages) {
				result = new ConditionMessage(result, message.toString());
			}
		}
		return result;
	}

	/**
	 * 为条件构建新 {@link ConditionMessage} 的构建器工厂方法。
	 * @param condition 条件
	 * @param details 条件详情
	 * @return {@link Builder} 构建器
	 * @see #forCondition(String, Object...)
	 * @see #andCondition(String, Object...)
	 */
	public static Builder forCondition(Class<? extends Annotation> condition, Object... details) {
		return new ConditionMessage().andCondition(condition, details);
	}

	/**
	 * 为条件构建新 {@link ConditionMessage} 的构建器工厂方法。
	 * @param condition 条件
	 * @param details 条件详情
	 * @return {@link Builder} 构建器
	 * @see #forCondition(Class, Object...)
	 * @see #andCondition(String, Object...)
	 */
	public static Builder forCondition(String condition, Object... details) {
		return new ConditionMessage().andCondition(condition, details);
	}

	/**
	 * 用于为条件创建 {@link ConditionMessage} 的构建器。
	 */
	public final class Builder {

		private final String condition;

		private Builder(String condition) {
			this.condition = condition;
		}

		/**
		 * 表示找到了精确结果。例如 {@code foundExactly("foo")} 生成消息 "found foo"。
		 * @param result 找到的结果
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage foundExactly(Object result) {
			return found("").items(result);
		}

		/**
		 * 表示找到一个或多个结果。例如 {@code found("bean").items("x")} 生成消息 "found bean x"。
		 * @param article 找到项的冠词
		 * @return {@link ItemsBuilder}
		 */
		public ItemsBuilder found(String article) {
			return found(article, article);
		}

		/**
		 * 表示找到一个或多个结果。例如 {@code found("bean", "beans").items("x", "y")}
		 * 生成消息 "found beans x, y"。
		 * @param singular 单数形式的冠词
		 * @param plural 复数形式的冠词
		 * @return {@link ItemsBuilder}
		 */
		public ItemsBuilder found(String singular, String plural) {
			return new ItemsBuilder(this, "found", singular, plural);
		}

		/**
		 * 表示未找到一个或多个结果。例如 {@code didNotFind("bean").items("x")}
		 * 生成消息 "did not find bean x"。
		 * @param article 未找到项的冠词
		 * @return {@link ItemsBuilder}
		 */
		public ItemsBuilder didNotFind(String article) {
			return didNotFind(article, article);
		}

		/**
		 * 表示未找到一个或多个结果。例如 {@code didNotFind("bean", "beans").items("x", "y")}
		 * 生成消息 "did not find beans x, y"。
		 * @param singular 单数形式的冠词
		 * @param plural 复数形式的冠词
		 * @return {@link ItemsBuilder}
		 */
		public ItemsBuilder didNotFind(String singular, String plural) {
			return new ItemsBuilder(this, "did not find", singular, plural);
		}

		/**
		 * 表示单一结果。例如 {@code resultedIn("yes")} 生成消息 "resulted in yes"。
		 * @param result 结果
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage resultedIn(Object result) {
			return because("resulted in " + result);
		}

		/**
		 * 表示某项可用。例如 {@code available("money")} 生成消息 "money is available"。
		 * @param item 可用的项
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage available(String item) {
			return because(item + " is available");
		}

		/**
		 * 表示某项不可用。例如 {@code notAvailable("time")} 生成消息 "time is not available"。
		 * @param item 不可用的项
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage notAvailable(String item) {
			return because(item + " is not available");
		}

		/**
		 * 表示原因。例如 {@code because("running Linux")} 生成消息 "running Linux"。
		 * @param reason 消息的原因
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage because(@Nullable String reason) {
			if (StringUtils.hasLength(reason)) {
				return new ConditionMessage(ConditionMessage.this,
						StringUtils.hasLength(this.condition) ? this.condition + " " + reason : reason);
			}
			return new ConditionMessage(ConditionMessage.this, this.condition);
		}

	}

	/**
	 * 用于为条件创建 {@link ItemsBuilder} 的构建器。
	 */
	public final class ItemsBuilder {

		private final Builder condition;

		private final String reason;

		private final String singular;

		private final String plural;

		private ItemsBuilder(Builder condition, String reason, String singular, String plural) {
			this.condition = condition;
			this.reason = reason;
			this.singular = singular;
			this.plural = plural;
		}

		/**
		 * 在无可用项时使用。例如 {@code didNotFind("any beans").atAll()}
		 * 生成消息 "did not find any beans"。
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage atAll() {
			return items(Collections.emptyList());
		}

		/**
		 * 指定项。例如 {@code didNotFind("bean", "beans").items("x", "y")}
		 * 生成消息 "did not find beans x, y"。
		 * @param items 项（可为 {@code null}）
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage items(Object @Nullable ... items) {
			return items(Style.NORMAL, items);
		}

		/**
		 * 指定项。例如 {@code didNotFind("bean", "beans").items("x", "y")}
		 * 生成消息 "did not find beans x, y"。
		 * @param style 渲染样式
		 * @param items 项（可为 {@code null}）
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage items(Style style, Object @Nullable ... items) {
			return items(style, (items != null) ? Arrays.asList(items) : null);
		}

		/**
		 * 指定项。例如 {@code didNotFind("bean", "beans").items(Collections.singleton("x")}
		 * 生成消息 "did not find bean x"。
		 * @param items 项的来源（可为 {@code null}）
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage items(@Nullable Collection<?> items) {
			return items(Style.NORMAL, items);
		}

		/**
		 * 使用 {@link Style} 指定项。例如
		 * {@code didNotFind("bean", "beans").items(Style.QUOTE, Collections.singleton("x")}
		 * 生成消息 "did not find bean 'x'"。
		 * @param style 渲染样式
		 * @param items 项的来源（可为 {@code null}）
		 * @return 构建完成的 {@link ConditionMessage}
		 */
		public ConditionMessage items(Style style, @Nullable Collection<?> items) {
			Assert.notNull(style, "'style' must not be null");
			StringBuilder message = new StringBuilder(this.reason);
			items = style.applyTo(items);
			if ((this.condition == null || items == null || items.size() <= 1)
					&& StringUtils.hasLength(this.singular)) {
				message.append(" ").append(this.singular);
			}
			else if (StringUtils.hasLength(this.plural)) {
				message.append(" ").append(this.plural);
			}
			if (!CollectionUtils.isEmpty(items)) {
				message.append(" ").append(StringUtils.collectionToDelimitedString(items, ", "));
			}
			return this.condition.because(message.toString());
		}

	}

	/**
	 * 渲染样式。
	 */
	public enum Style {

		/**
		 * 以普通样式渲染。
		 */
		NORMAL {

			@Override
			protected @Nullable Object applyToItem(@Nullable Object item) {
				return item;
			}

		},

		/**
		 * 以引号包裹项进行渲染。
		 */
		QUOTE {

			@Override
			protected @Nullable String applyToItem(@Nullable Object item) {
				return (item != null) ? "'" + item + "'" : null;
			}

		};

		public @Nullable Collection<?> applyTo(@Nullable Collection<?> items) {
			if (items == null) {
				return null;
			}
			List<Object> result = new ArrayList<>(items.size());
			for (Object item : items) {
				Object applied = applyToItem(item);
				if (applied != null) {
					result.add(applied);
				}
			}
			return result;
		}

		protected abstract @Nullable Object applyToItem(@Nullable Object item);

	}

}
