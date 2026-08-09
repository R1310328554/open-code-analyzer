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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.InitializingBean;

/**
 * DAO 的通用基类，定义 DAO 初始化的模板方法。
 *
 * <p>由 Spring 特定 DAO 支持类扩展，例如 JdbcDaoSupport、JdoDaoSupport 等。
 *
 * @author Juergen Hoeller
 * @since 1.2.2
 * @see org.springframework.jdbc.core.support.JdbcDaoSupport
 * @deprecated as of 7.0, in favor of direct injection of client dependencies
 */
@Deprecated(since = "7.0", forRemoval = true)
public abstract class DaoSupport implements InitializingBean {

	/** 子类可用的 Logger。 */
	protected final Log logger = LogFactory.getLog(getClass());


	@Override
	public final void afterPropertiesSet() throws IllegalArgumentException, BeanInitializationException {
		// Let abstract subclasses check their configuration.
		checkDaoConfig();

		// Let concrete implementations initialize themselves.
		try {
			initDao();
		}
		catch (Exception ex) {
			throw new BeanInitializationException("Initialization of DAO failed", ex);
		}
	}

	/**
	 * 抽象子类必须重写此方法以检查其配置。
	 * <p>若具体子类不应自行重写此模板方法，实现者应标记为 {@code final}。
	 * @throws IllegalArgumentException 配置非法时
	 */
	protected abstract void checkDaoConfig() throws IllegalArgumentException;

	/**
	 * 具体子类可重写此方法以实现自定义初始化行为。
	 * 在本实例 Bean 属性填充后调用。
	 * @throws Exception DAO 初始化失败时
	 *（将重新抛出为 BeanInitializationException）
	 * @see org.springframework.beans.factory.BeanInitializationException
	 */
	protected void initDao() throws Exception {
	}

}
