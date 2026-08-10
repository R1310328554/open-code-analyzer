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

package com.alibaba.nacos.config.server.exception;

/**
 * 配置模块内部运行时异常基类，用于非 API 层错误传播。
 * NacosConfigException.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class NacosConfigException extends RuntimeException {
    
    /** 无参构造 */
    public NacosConfigException() {
    }
    
    /** 带消息的构造 */
    public NacosConfigException(String message) {
        super(message);
    }
    
    /** 带消息与根因的构造 */
    public NacosConfigException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /** 仅根因的构造 */
    public NacosConfigException(Throwable cause) {
        super(cause);
    }
    
    /** 完整 {@link RuntimeException} 构造，可控制 suppression 与 stackTrace */
    public NacosConfigException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
