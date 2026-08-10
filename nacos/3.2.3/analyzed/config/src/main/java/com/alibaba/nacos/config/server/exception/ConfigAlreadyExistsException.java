/*
 * Copyright 1999-$toady.year Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.exception.NacosException;

/**
 * 配置已存在异常：发布或导入时 dataId+group+namespace 冲突时抛出。
 * 错误码固定为 {@link com.alibaba.nacos.api.exception.NacosException#CONFIG_ALREADY_EXISTS}。
 * ConfigAlreadyExistsException.
 *
 * @author Nacos
 */
public class ConfigAlreadyExistsException extends NacosException {
    
    private static final long serialVersionUID = -8247262927932720692L;
    
    /** 包级私有无参构造 */
    ConfigAlreadyExistsException() {
        super();
    }
    
    /** 指定错误码与消息的构造 */
    public ConfigAlreadyExistsException(int errCode, String errMsg) {
        super(errCode, errMsg);
    }
    
    /** 使用 CONFIG_ALREADY_EXISTS 错误码与自定义消息 */
    public ConfigAlreadyExistsException(String errMsg) {
        super(NacosException.CONFIG_ALREADY_EXISTS, errMsg);
    }
    
    /** 指定错误码与根因异常 */
    public ConfigAlreadyExistsException(int errCode, Throwable throwable) {
        super(errCode, throwable);
    }
    
    /** 指定错误码、消息与根因异常 */
    public ConfigAlreadyExistsException(int errCode, String errMsg, Throwable throwable) {
        super(errCode, errMsg, throwable);
    }
}
