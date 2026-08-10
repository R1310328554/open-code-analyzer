/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.keycloak.quarkus.runtime.storage.database.liquibase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liquibase.exception.ServiceNotFoundException;
import liquibase.servicelocator.StandardServiceLocator;

/**
 * Liquibase 服务定位器加速实现：启动时预注册服务类名，避免运行时 SPI 扫描。
 */
public class FastServiceLocator extends StandardServiceLocator {

    /** 接口全限定名 → 实现类全限定名列表。 */
    private Map<String, List<String>> services = new HashMap<>();

    /** 略高于默认优先级，使 Quarkus 运行时优先选用本定位器。 */
    @Override
    public int getPriority() {
        return super.getPriority() + 1;
    }

    /** 若已预注册则直接实例化，否则回退父类 SPI 发现。 */
    @Override
    @SuppressWarnings("unchecked")
    public <T> List<T> findInstances(Class<T> interfaceType) throws ServiceNotFoundException {
        List<String> found = services.get(interfaceType.getName());

        if (found == null) {
            return super.findInstances(interfaceType);
        }

        List<T> ret = new ArrayList<>();
        for (String i : found) {
            try {
                ret.add((T) Class.forName(i, false, Thread.currentThread().getContextClassLoader())
                        .getDeclaredConstructor().newInstance());
            } catch (Exception e) {
                throw new RuntimeException("Failed to find Liquibase implementation", e);
            }
        }
        return ret;
    }

    public FastServiceLocator() {
    }

    /** 注入预解析的 Liquibase 服务映射（Quarkus 构建期生成）。 */
    public void initServices(final Map<String, List<String>> services) {
        this.services = services;
    }
}