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

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

/**
 * 支持链式组合的 {@link PersistenceExceptionTranslator} 实现，
 * 允许按顺序添加 PersistenceExceptionTranslator 实例。
 * 在首次（若有）匹配时返回 {@code non-null}。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ChainedPersistenceExceptionTranslator implements PersistenceExceptionTranslator {

	/** PersistenceExceptionTranslator 列表。 */
	private final List<PersistenceExceptionTranslator> delegates = new ArrayList<>(4);


	/**
	 * 向链式委托列表添加 PersistenceExceptionTranslator。
	 */
	public final void addDelegate(PersistenceExceptionTranslator pet) {
		Assert.notNull(pet, "PersistenceExceptionTranslator must not be null");
		this.delegates.add(pet);
	}

	/**
	 * 返回所有已注册的 PersistenceExceptionTranslator 委托（数组形式）。
	 */
	public final PersistenceExceptionTranslator[] getDelegates() {
		return this.delegates.toArray(new PersistenceExceptionTranslator[0]);
	}


	@Override
	public @Nullable DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		for (PersistenceExceptionTranslator pet : this.delegates) {
			DataAccessException translatedDex = pet.translateExceptionIfPossible(ex);
			if (translatedDex != null) {
				return translatedDex;
			}
		}
		return null;
	}

}
