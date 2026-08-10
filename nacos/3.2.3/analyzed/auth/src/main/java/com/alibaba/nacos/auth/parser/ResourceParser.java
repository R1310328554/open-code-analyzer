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

package com.alibaba.nacos.auth.parser;

import com.alibaba.nacos.auth.annotation.Secured;
import com.alibaba.nacos.plugin.auth.api.Resource;

/**
 * 资源解析器接口。
 *
 * <p>将入站请求（HTTP 或 gRPC）与 {@link Secured} 注解信息组合，解析为授权插件可识别的
 * {@link Resource} 对象，供后续权限校验使用。</p>
 *
 * @author nkorange
 * @author mai.jh
 * @since 1.2.0
 */
public interface ResourceParser<R> {
    
    /**
     * 从请求中解析授权资源。
     *
     * @param request 原始请求对象
     * @param secured 接口上的 {@link Secured} 安全注解
     * @return 解析得到的 {@link Resource}，包含命名空间、分组、资源名及扩展属性
     */
    Resource parse(R request, Secured secured);
}
