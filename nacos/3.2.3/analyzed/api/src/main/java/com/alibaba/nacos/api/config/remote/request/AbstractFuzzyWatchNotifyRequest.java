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

package com.alibaba.nacos.api.config.remote.request;

import com.alibaba.nacos.api.remote.request.ServerRequest;

import static com.alibaba.nacos.api.common.Constants.Config.CONFIG_MODULE;

/**
 * 模糊监听通知类服务端请求的抽象基类。
 *
 * <p>服务端向客户端推送模糊订阅变更或同步时使用。</p>
 *
 * @author stone-98
 * @date 2024/3/14
 */
public abstract class AbstractFuzzyWatchNotifyRequest extends ServerRequest {
    
    /** 无参构造，供序列化框架使用。 */
    public AbstractFuzzyWatchNotifyRequest() {
    }
    
    /** 返回配置模块标识。 */
    @Override
    public String getModule() {
        return CONFIG_MODULE;
    }
}
