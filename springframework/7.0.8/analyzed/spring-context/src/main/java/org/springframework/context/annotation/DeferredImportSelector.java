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

package org.springframework.context.annotation;

import org.jspecify.annotations.Nullable;

import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link ImportSelector} 的变体，在所有 {@code @Configuration} Bean 处理完毕后再运行。
 * 当所选导入类带有 {@code @Conditional} 时尤其有用。
 *
 * <p>实现类还可实现 {@link org.springframework.core.Ordered} 接口或使用
 * {@link org.springframework.core.annotation.Order} 注解，以相对其他
 * {@link DeferredImportSelector DeferredImportSelector} 声明优先级。
 *
 * <p>实现类还可提供 {@link #getImportGroup() 导入分组}，在不同选择器之间提供额外的排序与过滤逻辑。
 *
 * @author Phillip Webb
 * @author Stephane Nicoll
 * @since 4.0
 */
public interface DeferredImportSelector extends ImportSelector {

	/**
	 * 返回特定的导入分组。
	 * <p>默认实现返回 {@code null}，表示无需分组。
	 * @return 导入分组类，或 {@code null} 表示无分组
	 * @since 5.0
	 */
	default @Nullable Class<? extends Group> getImportGroup() {
		return null;
	}


	/**
	 * 用于对不同导入选择器的结果进行分组的接口。
	 * @since 5.0
	 */
	interface Group {

		/**
		 * 使用指定 {@link DeferredImportSelector} 处理导入 @{@link Configuration} 类的
		 * {@link AnnotationMetadata}。
		 */
		void process(AnnotationMetadata metadata, DeferredImportSelector selector);

		/**
		 * 返回本分组应导入的 {@link Entry 条目}（类名）。
		 */
		Iterable<Entry> selectImports();


		/**
		 * 持有导入 @{@link Configuration} 类的 {@link AnnotationMetadata}
		 * 及要导入的类名的条目。
		 */
		class Entry {

			private final AnnotationMetadata metadata;

			private final String importClassName;

			public Entry(AnnotationMetadata metadata, String importClassName) {
				this.metadata = metadata;
				this.importClassName = importClassName;
			}

			/**
			 * 返回导入 @{@link Configuration} 类的 {@link AnnotationMetadata}。
			 */
			public AnnotationMetadata getMetadata() {
				return this.metadata;
			}

			/**
			 * 返回要导入类的全限定名。
			 */
			public String getImportClassName() {
				return this.importClassName;
			}

			@Override
			public boolean equals(@Nullable Object other) {
				if (this == other) {
					return true;
				}
				if (other == null || getClass() != other.getClass()) {
					return false;
				}
				Entry entry = (Entry) other;
				return (this.metadata.equals(entry.metadata) && this.importClassName.equals(entry.importClassName));
			}

			@Override
			public int hashCode() {
				return (this.metadata.hashCode() * 31 + this.importClassName.hashCode());
			}

			@Override
			public String toString() {
				return this.importClassName;
			}
		}
	}

}
