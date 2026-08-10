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

package com.alibaba.nacos.api.exception.runtime;

/**
 * Nacos 资源加载运行时异常。
 *
 * <p>在客户端或插件加载配置、类资源失败时使用，携带可读错误消息。</p>
 *
 * @author hujun
 */
public class NacosLoadException extends RuntimeException {
    
    private static final long serialVersionUID = 3513491993982295562L;
    
    /**
     * 构造带错误消息的加载异常。
     *
     * @param errMsg 错误描述
     */
    public NacosLoadException(String errMsg) {
        super(errMsg);
    }
    
}
