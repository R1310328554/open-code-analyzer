/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.connections.jpa.entityprovider;

import java.util.List;

import org.keycloak.provider.Provider;

/**
 * JPA 实体扩展 Provider：向 Keycloak 的 EntityManager 注册额外 JPA 实体类。
 * 实体以 {@link Class} 列表形式提供，并可附带 Liquibase 变更日志以同步表结构。
 *
 * @author <a href="mailto:erik.mulder@docdatapayments.com">Erik Mulder</a>
 */
public interface JpaEntityProvider extends Provider {

    /**
     * 返回应加入 EntityManager 持久化单元的实体类列表。
     *
     * @return list of class objects
     */
	List<Class<?>> getEntities();
	
	/**
	 * 返回与扩展实体配套的 Liquibase changelog  classpath 位置。
	 * 路径须与实体类位于同一 ClassLoader 可加载范围内。
	 *
	 * @return a changelog location or null if not needed
	 */
	String getChangelogLocation();

	/**
	 * 返回创建本 Provider 的工厂 ID，可用于推导 Liquibase changelog 跟踪表名。
	 * @return ID of provider factory
	 */
	String getFactoryId();

}
