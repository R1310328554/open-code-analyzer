/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.config.constants;

/**
 * 配置变更插件执行时机枚举。
 *
 * <p>标识插件在配置变更切点方法执行前还是执行后被调用。</p>
 *
 * @author liyunfei
 */
public enum ConfigChangeExecuteTypes {
    /**
     * 在切点方法执行前调用插件。
     */
    EXECUTE_BEFORE_TYPE,
    /**
     * 在切点方法执行后调用插件。
     */
    EXECUTE_AFTER_TYPE
}
