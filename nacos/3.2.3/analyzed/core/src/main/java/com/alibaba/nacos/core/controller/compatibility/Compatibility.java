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

package com.alibaba.nacos.core.controller.compatibility;

import com.alibaba.nacos.api.common.ApiType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 旧版 API 兼容标记注解：标注即将废弃的接口，用户可通过 {@link com.alibaba.nacos.legacy.adapter.compatibility.ApiCompatibilityConfig} 配置临时继续启用。
 * Nacos old API compatibility annotation.
 * <p>
 *     Marked old API will be deprecated in future version, but for some users need time to refactor and move to new API.
 *     In this situation, change the configuration in {@link com.alibaba.nacos.legacy.adapter.compatibility.ApiCompatibilityConfig}
 *     to open the old API usage.
 * </p>
 *
 * @author xiweng.yy
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Compatibility {
    
    /**
     * API 类型，用于区分 {@link ApiType} 下的开放/管理/控制台等接口。
     *
     * @return the type of the API
     */
    ApiType apiType() default ApiType.OPEN_API;
    
    /**
     * 可替代本废弃 API 的新接口说明或路径列表。
     *
     * @return API list.
     */
    String alternatives() default "";
}
