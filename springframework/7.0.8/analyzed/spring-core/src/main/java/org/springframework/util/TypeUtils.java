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

package org.springframework.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

import org.jspecify.annotations.Nullable;

import org.springframework.lang.Contract;

/* ===== [OCA 中文解析] =====
class TypeUtils — 意图说明

class `TypeUtils`：请结合所属模块与调用方理解其在整体架构中的职责。；源文件: `spring-core/src/main/java/org/springframework/util/TypeUtils.java`

（本注释由 open-code-analyzer 生成，置于原有文档注释之前）
===== [OCA 中文解析结束] ===== */
/**
 * Utility to work with generic type parameters.
 *
 * <p>Mainly for internal use within the framework.
 *
 * @author Ramnivas Laddad
 * @author Juergen Hoeller
 * @author Chris Beams
 * @author Sam Brannen
 * @since 2.0.7
 */
public abstract class TypeUtils {

	private static final @Nullable Type[] IMPLICIT_LOWER_BOUNDS = { null };

	// [OCA] 字段 `IMPLICIT_UPPER_BOUNDS`：类成员状态。
	private static final Type[] IMPLICIT_UPPER_BOUNDS = { Object.class };


	/* ===== [OCA 中文解析] =====
方法 isAssignable — 意图与阅读要点

方法 `isAssignable` 复杂度较高（CCN≈16, NLOC≈45）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。
	===== [OCA 中文解析结束] ===== */
	/**
	 * Check if the right-hand side type may be assigned to the left-hand side
	 * type following the Java generics rules.
	 * @param lhsType the target type (left-hand side type)
	 * @param rhsType the value type (right-hand side type) that should
	 * be assigned to the target type
	 * @return {@code true} if {@code rhsType} is assignable to {@code lhsType}
	 * @see ClassUtils#isAssignable(Class, Class)
	 */
	public static boolean isAssignable(Type lhsType, Type rhsType) {
		Assert.notNull(lhsType, "Left-hand side type must not be null");
		Assert.notNull(rhsType, "Right-hand side type must not be null");

		// All types are assignable to themselves and to class Object.
		if (lhsType.equals(rhsType) || Object.class == lhsType) {
			return true;
		}

		if (lhsType instanceof Class<?> lhsClass) {
			// Just comparing two classes...
			if (rhsType instanceof Class<?> rhsClass) {
				return ClassUtils.isAssignable(lhsClass, rhsClass);
			}

			if (rhsType instanceof ParameterizedType rhsParameterizedType) {
				Type rhsRaw = rhsParameterizedType.getRawType();
				// A parameterized type is always assignable to its raw class type.
				if (rhsRaw instanceof Class<?> rhRawClass) {
					return ClassUtils.isAssignable(lhsClass, rhRawClass);
				}
			}
			else if (lhsClass.isArray() && rhsType instanceof GenericArrayType rhsGenericArrayType) {
				Type rhsComponent = rhsGenericArrayType.getGenericComponentType();
				return isAssignable(lhsClass.componentType(), rhsComponent);
			}
		}

		// Parameterized types are only assignable to other parameterized types and class types.
		if (lhsType instanceof ParameterizedType lhsParameterizedType) {
			if (rhsType instanceof Class<?> rhsClass) {
				Type lhsRaw = lhsParameterizedType.getRawType();
				if (lhsRaw instanceof Class<?> lhsClass) {
					return ClassUtils.isAssignable(lhsClass, rhsClass);
				}
			}
			else if (rhsType instanceof ParameterizedType rhsParameterizedType) {
				return isAssignable(lhsParameterizedType, rhsParameterizedType);
			}
		}

		if (lhsType instanceof GenericArrayType lhsGenericArrayType) {
			Type lhsComponent = lhsGenericArrayType.getGenericComponentType();
			if (rhsType instanceof Class<?> rhsClass && rhsClass.isArray()) {
				return isAssignable(lhsComponent, rhsClass.componentType());
			}
			else if (rhsType instanceof GenericArrayType rhsGenericArrayType) {
				Type rhsComponent = rhsGenericArrayType.getGenericComponentType();

				return isAssignable(lhsComponent, rhsComponent);
			}
		}

		if (lhsType instanceof WildcardType lhsWildcardType) {
			return isAssignable(lhsWildcardType, rhsType);
		}

		return false;
	}

	/* ===== [OCA 中文解析] =====
方法 isAssignable — 意图与阅读要点

方法 `isAssignable` 复杂度较高（CCN≈16, NLOC≈45）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。

	===== [OCA 中文解析结束] ===== */

	private static boolean isAssignable(ParameterizedType lhsType, ParameterizedType rhsType) {
		if (lhsType.equals(rhsType)) {
			return true;
		}

		Type[] lhsTypeArguments = lhsType.getActualTypeArguments();
		Type[] rhsTypeArguments = rhsType.getActualTypeArguments();
		if (lhsTypeArguments.length != rhsTypeArguments.length) {
			return false;
		}

		for (int size = lhsTypeArguments.length, i = 0; i < size; ++i) {
			Type lhsArg = lhsTypeArguments[i];
			Type rhsArg = rhsTypeArguments[i];
			if (!lhsArg.equals(rhsArg) &&
					!(lhsArg instanceof WildcardType wildcardType && isAssignable(wildcardType, rhsArg))) {
				return false;
			}
		}

		return true;
	}

	/* ===== [OCA 中文解析] =====
方法 isAssignable — 意图与阅读要点

方法 `isAssignable` 复杂度较高（CCN≈16, NLOC≈45）。阅读时建议先抓住主路径，再看分支/异常/缓存等旁路逻辑；关注它在调用链中上下游的契约（入参约束、返回值语义、抛出的异常）。

	===== [OCA 中文解析结束] ===== */

	private static boolean isAssignable(WildcardType lhsType, Type rhsType) {
		@Nullable Type[] lUpperBounds = getUpperBounds(lhsType);
		@Nullable Type[] lLowerBounds = getLowerBounds(lhsType);

		if (rhsType instanceof WildcardType rhsWcType) {
			// Both the upper and lower bounds of the right-hand side must be
			// completely enclosed in the upper and lower bounds of the left-hand side.
			@Nullable Type[] rUpperBounds = getUpperBounds(rhsWcType);
			@Nullable Type[] rLowerBounds = getLowerBounds(rhsWcType);

			for (Type lBound : lUpperBounds) {
				for (Type rBound : rUpperBounds) {
					if (!isAssignableBound(lBound, rBound)) {
						return false;
					}
				}
				for (Type rBound : rLowerBounds) {
					if (!isAssignableBound(lBound, rBound)) {
						return false;
					}
				}
			}

			for (Type lBound : lLowerBounds) {
				for (Type rBound : rUpperBounds) {
					if (!isAssignableBound(rBound, lBound)) {
						return false;
					}
				}
				for (Type rBound : rLowerBounds) {
					if (!isAssignableBound(rBound, lBound)) {
						return false;
					}
				}
			}
		}
		else {
			for (Type lBound : lUpperBounds) {
				if (!isAssignableBound(lBound, rhsType)) {
					return false;
				}
			}
			for (Type lBound : lLowerBounds) {
				if (!isAssignableBound(rhsType, lBound)) {
					return false;
				}
			}
		}

		return true;
	}

	private static @Nullable Type[] getLowerBounds(WildcardType wildcardType) {
		Type[] lowerBounds = wildcardType.getLowerBounds();
		// Supply the implicit lower bound if none are specified.
		return (lowerBounds.length == 0 ? IMPLICIT_LOWER_BOUNDS : lowerBounds);
	}

	private static Type[] getUpperBounds(WildcardType wildcardType) {
		Type[] upperBounds = wildcardType.getUpperBounds();
		// Supply the implicit upper bound if none are specified.
		return (upperBounds.length == 0 ? IMPLICIT_UPPER_BOUNDS : upperBounds);
	}

	@Contract("_, null -> true; null, _ -> false")
	public static boolean isAssignableBound(@Nullable Type lhsType, @Nullable Type rhsType) {
		if (rhsType == null) {
			return true;
		}
		if (lhsType == null) {
			return false;
		}
		return isAssignable(lhsType, rhsType);
	}

}
