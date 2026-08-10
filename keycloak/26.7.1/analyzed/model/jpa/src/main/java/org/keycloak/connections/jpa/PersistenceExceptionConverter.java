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

package org.keycloak.connections.jpa;

import org.keycloak.connections.jpa.support.EntityManagerProxy;
import org.keycloak.models.ModelException;

/**
 * 持久化异常转换工具：统一将 JPA/Hibernate 抛出的底层异常包装为 {@link ModelException}。
 *
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class PersistenceExceptionConverter {

    /**
     * 将任意 Throwable 转为 {@link ModelException}。
     * JTA 场景下数据库操作在 commit 阶段才真正执行，异常传播路径可能与资源本地事务不同，
     * 具体解析逻辑委托给 {@link EntityManagerProxy#convert(Throwable)}。
     */
    public static ModelException convert(Throwable t) {
        return EntityManagerProxy.convert(t);
    }

}
