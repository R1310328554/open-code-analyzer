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

package com.alibaba.nacos.api.annotation;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingService;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将 {@link ConfigService} 或 {@link NamingService} 实例注入目标 Bean 的注解。
 *
 * <p>可用于构造器、字段、方法或注解类型；
 * 通过 {@link #properties()} 指定连接参数，未配置时使用全局 Nacos 属性。</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigService
 * @see NamingService
 * @see NacosProperties
 * @since 0.2.1
 */
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD,
    ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NacosInjected {
    
    /**
     * {@link NacosProperties} 连接配置；未指定时使用全局 Nacos 属性。
     *
     * @return 默认值为 {@link NacosProperties}
     */
    NacosProperties properties() default @NacosProperties;
    
}
