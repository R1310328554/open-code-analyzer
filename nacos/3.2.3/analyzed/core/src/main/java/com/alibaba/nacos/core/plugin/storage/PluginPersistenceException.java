/*
 * Copyright 1999-2024 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.core.plugin.storage;

/**
 * 插件状态/配置持久化失败时抛出的运行时异常。
 * Exception thrown when plugin persistence operations fail.
 *
 * @author WangzJi
 * @since 3.2.0
 */
public class PluginPersistenceException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /** 以错误消息构造异常。 */
    public PluginPersistenceException(String message) {
        super(message);
    }
    
    /** 以错误消息与根因构造异常。 */
    public PluginPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
