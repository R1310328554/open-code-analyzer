/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.nacos.core.cluster.health;

/**
 * 模块就绪探针抽象基类：构造时自动注册到 {@link ModuleHealthCheckerHolder}。
 * Health checker.
 *
 * @author xiweng.yy
 */
public abstract class AbstractModuleHealthChecker {
    
    protected AbstractModuleHealthChecker() {
        ModuleHealthCheckerHolder.getInstance().registerChecker(this);
    }
    
    /**
     * 检查本模块是否已就绪可对外服务。
     *
     * @return 就绪返回 {@code true}，否则 {@code false}
     */
    public abstract boolean readiness();
    
    /**
     * 返回模块名称，用于就绪失败时的聚合提示。
     *
     * @return 模块名
     */
    public abstract String getModuleName();
}
