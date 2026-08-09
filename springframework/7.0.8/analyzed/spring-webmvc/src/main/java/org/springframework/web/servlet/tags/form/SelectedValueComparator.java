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

package org.springframework.web.servlet.tags.form;

import java.beans.PropertyEditor;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.support.BindStatus;

/* ===== [OCA 中文解析] =====
class SelectedValueComparator — 意图说明

class `SelectedValueComparator`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-webmvc/src/main/java/org/springframework/web/servlet/tags/form/SelectedValueComparator.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Utility class for testing whether a candidate value matches a {@link BindStatus#getValue data bound value}.
 * Eagerly attempts to prove a comparison through a number of avenues to deal with issues such as instance
 * inequality, logical (String-representation-based) equality and {@link PropertyEditor}-based comparison.
 *
 * <p>Full support is provided for comparing arrays, {@link Collection Collections} and {@link Map Maps}.
 *
 * <p><h1><a name="equality-contract">Equality Contract</a></h1>
 * For single-valued objects equality is first tested using standard {@link Object#equals Java equality}. As
 * such, user code should endeavor to implement {@link Object#equals} to speed up the comparison process. If
 * {@link Object#equals} returns {@code false} then an attempt is made at an
 * {@link #exhaustiveCompare exhaustive comparison} with the aim being to <strong>prove</strong> equality rather
 * than disprove it.
 *
 * <p>Next, an attempt is made to compare the {@code String} representations of both the candidate and bound
 * values. This may result in {@code true} in a number of cases due to the fact both values will be represented
 * as {@code Strings} when shown to the user.
 *
 * <p>Next, if the candidate value is a {@code String}, an attempt is made to compare the bound value to
 * result of applying the corresponding {@link PropertyEditor} to the candidate. This comparison may be
 * executed twice, once against the direct {@code String} instances, and then against the {@code String}
 * representations if the first comparison results in {@code false}.
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
abstract class SelectedValueComparator {

	/* ===== [OCA 中文解析] =====
方法 isSelected — 意图与阅读要点

方法 `isSelected` 复杂度较高（CCN≈12, NLOC≈33）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Returns {@code true} if the supplied candidate value is equal to the value bound to
	 * the supplied {@link BindStatus}. Equality in this case differs from standard Java equality and
	 * is described in more detail <a href="#equality-contract">here</a>.
	 */
	public static boolean isSelected(BindStatus bindStatus, @Nullable Object candidateValue) {
		// Check obvious equality matches with the candidate first,
		// both with the rendered value and with the original value.
		Object boundValue = bindStatus.getValue();
		if (ObjectUtils.nullSafeEquals(boundValue, candidateValue)) {
			return true;
		}
		Object actualValue = bindStatus.getActualValue();
		if (actualValue != null && actualValue != boundValue &&
				ObjectUtils.nullSafeEquals(actualValue, candidateValue)) {
			return true;
		}
		if (actualValue != null) {
			boundValue = actualValue;
		}
		else if (boundValue == null) {
			return false;
		}

		// Non-null value but no obvious equality with the candidate value:
		// go into more exhaustive comparisons.
		boolean selected = false;
		if (candidateValue != null) {
			if (boundValue.getClass().isArray()) {
				selected = collectionCompare(CollectionUtils.arrayToList(boundValue), candidateValue, bindStatus);
			}
			else if (boundValue instanceof Collection<?> collection) {
				selected = collectionCompare(collection, candidateValue, bindStatus);
			}
			else if (boundValue instanceof Map<?, ?> map) {
				selected = mapCompare(map, candidateValue, bindStatus);
			}
		}
		if (!selected) {
			selected = exhaustiveCompare(boundValue, candidateValue, bindStatus.getEditor(), null);
		}
		return selected;
	}

	private static boolean collectionCompare(
			Collection<?> boundCollection, Object candidateValue, BindStatus bindStatus) {
		try {
			if (boundCollection.contains(candidateValue)) {
				return true;
			}
		}
		catch (ClassCastException ex) {
			// Probably from a TreeSet - ignore.
		}
		return exhaustiveCollectionCompare(boundCollection, candidateValue, bindStatus);
	}

	private static boolean mapCompare(Map<?, ?> boundMap, Object candidateValue, BindStatus bindStatus) {
		try {
			if (boundMap.containsKey(candidateValue)) {
				return true;
			}
		}
		catch (ClassCastException ex) {
			// Probably from a TreeMap - ignore.
		}
		return exhaustiveCollectionCompare(boundMap.keySet(), candidateValue, bindStatus);
	}

	private static boolean exhaustiveCollectionCompare(
			Collection<?> collection, Object candidateValue, BindStatus bindStatus) {

		Map<PropertyEditor, Object> convertedValueCache = new HashMap<>();
		PropertyEditor editor = null;
		boolean candidateIsString = (candidateValue instanceof String);
		if (!candidateIsString) {
			editor = bindStatus.findEditor(candidateValue.getClass());
		}
		for (Object element : collection) {
			if (editor == null && element != null && candidateIsString) {
				editor = bindStatus.findEditor(element.getClass());
			}
			if (exhaustiveCompare(element, candidateValue, editor, convertedValueCache)) {
				return true;
			}
		}
		return false;
	}

	/* ===== [OCA 中文解析] =====
方法 exhaustiveCompare — 意图与阅读要点

方法 `exhaustiveCompare` 复杂度较高（CCN≈12, NLOC≈35）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。

	===== [OCA 中文解析结束] ===== */

	private static boolean exhaustiveCompare(@Nullable Object boundValue, @Nullable Object candidate,
			@Nullable PropertyEditor editor, @Nullable Map<PropertyEditor, Object> convertedValueCache) {

		String candidateDisplayString = ValueFormatter.getDisplayString(candidate, editor, false);
		if (boundValue != null && boundValue.getClass().isEnum()) {
			Enum<?> boundEnum = (Enum<?>) boundValue;
			String enumCodeAsString = ObjectUtils.getDisplayString(boundEnum.name());
			if (enumCodeAsString.equals(candidateDisplayString)) {
				return true;
			}
			String enumLabelAsString = ObjectUtils.getDisplayString(boundEnum.toString());
			if (enumLabelAsString.equals(candidateDisplayString)) {
				return true;
			}
		}
		else if (ObjectUtils.getDisplayString(boundValue).equals(candidateDisplayString)) {
			return true;
		}

		if (editor != null && candidate instanceof String candidateAsString) {
			// Try PE-based comparison (PE should *not* be allowed to escape creating thread)
			Object candidateAsValue;
			if (convertedValueCache != null && convertedValueCache.containsKey(editor)) {
				candidateAsValue = convertedValueCache.get(editor);
			}
			else {
				editor.setAsText(candidateAsString);
				candidateAsValue = editor.getValue();
				if (convertedValueCache != null) {
					convertedValueCache.put(editor, candidateAsValue);
				}
			}
			if (ObjectUtils.nullSafeEquals(boundValue, candidateAsValue)) {
				return true;
			}
		}
		return false;
	}

}
