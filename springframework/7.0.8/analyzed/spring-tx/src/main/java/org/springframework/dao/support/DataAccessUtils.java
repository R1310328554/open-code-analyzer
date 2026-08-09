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

package org.springframework.dao.support;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.TypeMismatchDataAccessException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;

/**
 * DAO 实现的杂项工具方法。
 *
 * <p>适用于任何数据访问技术。
 *
 * @author Juergen Hoeller
 * @since 1.0.2
 */
public abstract class DataAccessUtils {

	/**
	 * 从给定 Collection 返回单个结果对象。
	 * <p>若找到 0 个结果对象则返回 {@code null}；
	 * 若找到多于 1 个元素则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}）
	 * @return 单个结果对象，若无则返回 {@code null}
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个元素时
	 */
	public static <T> @Nullable T singleResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
		if (CollectionUtils.isEmpty(results)) {
			return null;
		}
		if (results.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, results.size());
		}
		return results.iterator().next();
	}

	/**
	 * 从给定 Stream 返回单个结果对象。
	 * <p>若找到 0 个结果对象则返回 {@code null}；
	 * 若找到多于 1 个元素则抛出异常。
	 * @param results 结果 Stream（可为 {@code null}）
	 * @return 单个结果对象，若无则返回 {@code null}
	 * @throws IncorrectResultSizeDataAccessException 给定 Stream 中找到多于一个元素时
	 * @since 6.1
	 */
	public static <T> @Nullable T singleResult(@Nullable Stream<T> results) throws IncorrectResultSizeDataAccessException {
		if (results == null) {
			return null;
		}
		try (results) {
			List<T> resultList = results.limit(2).toList();
			if (resultList.size() > 1) {
				throw new IncorrectResultSizeDataAccessException(1);
			}
			return (!resultList.isEmpty() ? resultList.get(0) : null);
		}
	}

	/**
	 * 从给定 Iterator 返回单个结果对象。
	 * <p>若找到 0 个结果对象则返回 {@code null}；
	 * 若找到多于 1 个元素则抛出异常。
	 * @param results 结果 Iterator（可为 {@code null}）
	 * @return 单个结果对象，若无则返回 {@code null}
	 * @throws IncorrectResultSizeDataAccessException 给定 Iterator 中找到多于一个元素时
	 * @since 6.1
	 */
	public static <T> @Nullable T singleResult(@Nullable Iterator<T> results) throws IncorrectResultSizeDataAccessException {
		if (results == null) {
			return null;
		}
		T result = (results.hasNext() ? results.next() : null);
		if (results.hasNext()) {
			throw new IncorrectResultSizeDataAccessException(1);
		}
		return result;
	}

	/**
	 * 从给定 Collection 返回单个结果对象。
	 * <p>若找到 0 个结果对象则返回 {@code Optional.empty()}；
	 * 若找到多于 1 个元素则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}）
	 * @return 单个可选结果对象，若无则返回 {@code Optional.empty()}
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个元素时
	 * @since 6.1
	 */
	public static <T> Optional<T> optionalResult(@Nullable Collection<? extends @Nullable T> results)
			throws IncorrectResultSizeDataAccessException {

		return Optional.ofNullable(singleResult(results));
	}

	/**
	 * 从给定 Stream 返回单个结果对象。
	 * <p>若找到 0 个结果对象则返回 {@code Optional.empty()}；
	 * 若找到多于 1 个元素则抛出异常。
	 * @param results 结果 Stream（可为 {@code null}）
	 * @return 单个可选结果对象，若无则返回 {@code Optional.empty()}
	 * @throws IncorrectResultSizeDataAccessException 给定 Stream 中找到多于一个元素时
	 * @since 6.1
	 */
	public static <T> Optional<T> optionalResult(@Nullable Stream<T> results) throws IncorrectResultSizeDataAccessException {
		return Optional.ofNullable(singleResult(results));
	}

	/**
	 * 从给定 Iterator 返回单个结果对象。
	 * <p>若找到 0 个结果对象则返回 {@code Optional.empty()}；
	 * 若找到多于 1 个元素则抛出异常。
	 * @param results 结果 Iterator（可为 {@code null}）
	 * @return 单个可选结果对象，若无则返回 {@code Optional.empty()}
	 * @throws IncorrectResultSizeDataAccessException 给定 Iterator 中找到多于一个元素时
	 * @since 6.1
	 */
	public static <T> Optional<T> optionalResult(@Nullable Iterator<T> results) throws IncorrectResultSizeDataAccessException {
		return Optional.ofNullable(singleResult(results));
	}

	/**
	 * 从给定 Collection 返回单个结果对象。
	 * <p>若找到 0 个或多于 1 个元素则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}，
	 * 但不应包含 {@code null} 元素）
	 * @return 单个结果对象
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个元素时
	 * @throws EmptyResultDataAccessException 给定 Collection 中未找到任何元素时
	 */
	public static <T extends @Nullable Object> @NonNull T requiredSingleResult(@Nullable Collection<T> results)
			throws IncorrectResultSizeDataAccessException {

		if (CollectionUtils.isEmpty(results)) {
			throw new EmptyResultDataAccessException(1);
		}
		if (results.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, results.size());
		}
		T result = results.iterator().next();
		if (result == null) {
			throw new TypeMismatchDataAccessException("Result value is null but no null value expected");
		}
		return result;
	}

	/**
	 * 从给定 Collection 返回单个结果对象。
	 * <p>若找到 0 个或多于 1 个元素则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}，
	 * 且可能包含 {@code null} 元素）
	 * @return 单个结果对象
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个元素时
	 * @throws EmptyResultDataAccessException 给定 Collection 中未找到任何元素时
	 * @since 5.0.2
	 */
	public static <T extends @Nullable Object> T nullableSingleResult(@Nullable Collection<T> results)
			throws IncorrectResultSizeDataAccessException {

		// This is identical to the requiredSingleResult implementation but differs in the
		// semantics of the incoming Collection (which we currently can't formally express)
		if (CollectionUtils.isEmpty(results)) {
			throw new EmptyResultDataAccessException(1);
		}
		if (results.size() > 1) {
			throw new IncorrectResultSizeDataAccessException(1, results.size());
		}
		return results.iterator().next();
	}

	/**
	 * 从给定 Collection 返回唯一结果对象。
	 * <p>若找到 0 个结果对象则返回 {@code null}；
	 * 若找到多于 1 个实例则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}）
	 * @return 唯一结果对象，若无则返回 {@code null}
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个结果对象时
	 * @see org.springframework.util.CollectionUtils#hasUniqueObject
	 */
	public static <T> @Nullable T uniqueResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
		if (CollectionUtils.isEmpty(results)) {
			return null;
		}
		if (!CollectionUtils.hasUniqueObject(results)) {
			throw new IncorrectResultSizeDataAccessException(1, results.size());
		}
		return results.iterator().next();
	}

	/**
	 * 从给定 Collection 返回唯一结果对象。
	 * <p>若找到 0 个或多于 1 个实例则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}，
	 * 但不应包含 {@code null} 元素）
	 * @return 唯一结果对象
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个结果对象时
	 * @throws EmptyResultDataAccessException 给定 Collection 中未找到任何结果对象时
	 * @see org.springframework.util.CollectionUtils#hasUniqueObject
	 */
	public static <T> T requiredUniqueResult(@Nullable Collection<T> results) throws IncorrectResultSizeDataAccessException {
		if (CollectionUtils.isEmpty(results)) {
			throw new EmptyResultDataAccessException(1);
		}
		if (!CollectionUtils.hasUniqueObject(results)) {
			throw new IncorrectResultSizeDataAccessException(1, results.size());
		}
		T result = results.iterator().next();
		if (result == null) {
			throw new TypeMismatchDataAccessException("Result value is null but no null value expected");
		}
		return result;
	}

	/**
	 * 从给定 Collection 返回唯一结果对象。
	 * 若找到 0 个或多于 1 个结果对象，或唯一结果对象无法转换为
	 * 指定所需类型，则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}，
	 * 但不应包含 {@code null} 元素）
	 * @return 唯一结果对象
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个结果对象时
	 * @throws EmptyResultDataAccessException 给定 Collection 中未找到任何结果对象时
	 * @throws TypeMismatchDataAccessException 唯一对象与指定所需类型不匹配时
	 */
	@SuppressWarnings("unchecked")
	public static <T> T objectResult(@Nullable Collection<?> results, @Nullable Class<T> requiredType)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

		Object result = requiredUniqueResult(results);
		if (requiredType != null && !requiredType.isInstance(result)) {
			if (String.class == requiredType) {
				result = result.toString();
			}
			else if (Number.class.isAssignableFrom(requiredType) && result instanceof Number number) {
				try {
					result = NumberUtils.convertNumberToTargetClass(number, (Class<? extends Number>) requiredType);
				}
				catch (IllegalArgumentException ex) {
					throw new TypeMismatchDataAccessException(ex.getMessage());
				}
			}
			else {
				throw new TypeMismatchDataAccessException(
						"Result object is of type [" + result.getClass().getName() +
						"] and could not be converted to required type [" + requiredType.getName() + "]");
			}
		}
		return (T) result;
	}

	/**
	 * 从给定 Collection 返回唯一 int 结果。
	 * 若找到 0 个或多于 1 个结果对象，或唯一结果对象无法转换为 int，则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}，
	 * 但不应包含 {@code null} 元素）
	 * @return 唯一 int 结果
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个结果对象时
	 * @throws EmptyResultDataAccessException 给定 Collection 中未找到任何结果对象时
	 * @throws TypeMismatchDataAccessException 集合中唯一对象无法转换为 int 时
	 */
	public static int intResult(@Nullable Collection<?> results)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

		return objectResult(results, Number.class).intValue();
	}

	/**
	 * 从给定 Collection 返回唯一 long 结果。
	 * 若找到 0 个或多于 1 个结果对象，或唯一结果对象无法转换为 long，则抛出异常。
	 * @param results 结果 Collection（可为 {@code null}，
	 * 但不应包含 {@code null} 元素）
	 * @return 唯一 long 结果
	 * @throws IncorrectResultSizeDataAccessException 给定 Collection 中找到多于一个结果对象时
	 * @throws EmptyResultDataAccessException 给定 Collection 中未找到任何结果对象时
	 * @throws TypeMismatchDataAccessException 集合中唯一对象无法转换为 long 时
	 */
	public static long longResult(@Nullable Collection<?> results)
			throws IncorrectResultSizeDataAccessException, TypeMismatchDataAccessException {

		return objectResult(results, Number.class).longValue();
	}


	/**
	 * 若合适则返回转换后的异常，否则原样返回给定异常。
	 * @param rawException 可能需要转换的异常
	 * @param pet 用于执行转换的 PersistenceExceptionTranslator
	 * @return 若可转换则返回转换后的持久化异常，否则返回原始异常
	 */
	public static RuntimeException translateIfNecessary(
			RuntimeException rawException, PersistenceExceptionTranslator pet) {

		Assert.notNull(pet, "PersistenceExceptionTranslator must not be null");
		DataAccessException dae = pet.translateExceptionIfPossible(rawException);
		return (dae != null ? dae : rawException);
	}

}
