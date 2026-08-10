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

package com.alibaba.nacos.auth.annotation;

import com.alibaba.nacos.api.common.ApiType;
import com.alibaba.nacos.auth.parser.DefaultResourceParser;
import com.alibaba.nacos.auth.parser.ResourceParser;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.plugin.auth.constant.ActionTypes;
import com.alibaba.nacos.plugin.auth.constant.SignType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 标记 API 请求需经过鉴权的注解。
 *
 * <p>声明操作类型、资源标识、模块类型及可选的自定义解析器等元数据。</p>
 *
 * @author nkorange
 * @author mai.jh
 * @since 1.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Secured {
    
    /**
     * 请求对应的操作类型。
     *
     * @return 操作类型，默认 {@link ActionTypes#READ}
     */
    ActionTypes action() default ActionTypes.READ;
    
    /**
     * 请求关联的资源名称。
     *
     * @return 资源名，空串表示由解析器推导
     */
    String resource() default StringUtils.EMPTY;
    
    /**
     * 资源所属模块/签名类型。
     *
     * @return 模块标识，默认 {@link SignType#NAMING}
     */
    String signType() default SignType.NAMING;
    
    /**
     * 自定义资源解析器，优先级低于 {@link #resource()} 与内置类型解析器。
     *
     * @return 资源解析器类型
     */
    Class<? extends ResourceParser> parser() default DefaultResourceParser.class;
    
    /**
     * 附加标签，将以键值对形式注入 {@link com.alibaba.nacos.plugin.auth.api.Resource} 的 properties。
     *
     * @return 标签数组
     */
    String[] tags() default {};
    
    /**
     * API 类型，用于区分管理端与开放 API 等场景。
     *
     * @return API 类型，默认 {@link ApiType#OPEN_API}
     */
    ApiType apiType() default ApiType.OPEN_API;
    
}
