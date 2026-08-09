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

package org.springframework.dao.annotation;

import java.lang.annotation.Annotation;

import org.springframework.aop.framework.autoproxy.AbstractBeanFactoryAwareAdvisingPostProcessor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

/**
 * Bean 后处理器，自动对标记 Spring @{@link org.springframework.stereotype.Repository Repository}
 * 注解的 Bean 应用持久化异常转换，
 * 向暴露的代理（现有 AOP 代理或新生成、实现目标全部接口的代理）
 * 添加对应的 {@link PersistenceExceptionTranslationAdvisor}。
 *
 * <p>将原生资源异常转换为 Spring 的
 * {@link org.springframework.dao.DataAccessException DataAccessException} 层次结构。
 * 自动检测实现
 * {@link org.springframework.dao.support.PersistenceExceptionTranslator
 * PersistenceExceptionTranslator} 接口的 Bean，并委托其转换候选异常。
 *
 * <p>Spring 所有适用的资源工厂（例如
 * {@link org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean}）
 * 均开箱实现 {@code PersistenceExceptionTranslator} 接口。
 * 因此，启用自动异常转换通常只需为所有受影响的 Bean（如 Repository 或 DAO）
 * 标记 {@code @Repository} 注解，并在应用上下文中定义本后处理器。
 *
 * <p>{@code PersistenceExceptionTranslator} Bean 按 Spring 依赖排序规则排序：
 * 参见 {@link org.springframework.core.Ordered} 和
 * {@link org.springframework.core.annotation.Order}。注意，此类 Bean 可从任意作用域检索，
 * 不限于单例作用域。
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @since 2.0
 * @see PersistenceExceptionTranslationAdvisor
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.dao.DataAccessException
 * @see org.springframework.dao.support.PersistenceExceptionTranslator
 */
@SuppressWarnings("serial")
public class PersistenceExceptionTranslationPostProcessor extends AbstractBeanFactoryAwareAdvisingPostProcessor {

	private Class<? extends Annotation> repositoryAnnotationType = Repository.class;


	/**
	 * 设置“repository”注解类型。
	 * 默认 repository 注解类型为 {@link Repository} 注解。
	 * <p>提供此 setter 以便开发者使用自定义（非 Spring 专用）注解类型
	 * 标识类具有 repository 角色。
	 * @param repositoryAnnotationType 所需的注解类型
	 */
	public void setRepositoryAnnotationType(Class<? extends Annotation> repositoryAnnotationType) {
		Assert.notNull(repositoryAnnotationType, "'repositoryAnnotationType' must not be null");
		this.repositoryAnnotationType = repositoryAnnotationType;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) {
		super.setBeanFactory(beanFactory);

		if (!(beanFactory instanceof ListableBeanFactory lbf)) {
			throw new IllegalArgumentException(
					"Cannot use PersistenceExceptionTranslator autodetection without ListableBeanFactory");
		}
		this.advisor = new PersistenceExceptionTranslationAdvisor(lbf, this.repositoryAnnotationType);
	}

}
