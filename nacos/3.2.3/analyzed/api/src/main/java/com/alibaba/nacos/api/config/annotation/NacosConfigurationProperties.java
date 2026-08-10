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

package com.alibaba.nacos.api.config.annotation;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;

/**
 * 将 Nacos 配置绑定到 POJO 的配置属性注解。
 *
 * <p>类似 Spring {@code @ConfigurationProperties}，支持前缀、自动刷新等选项。</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyKeyConst
 * @since 0.2.0
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NacosConfigurationProperties {
    
    /**
     * 配置键前缀，绑定字段时自动拼接。
     *
     * @return 默认空字符串
     */
    String prefix() default "";
    
    /**
     * Nacos 配置分组 ID。
     *
     * @return 默认 {@link Constants#DEFAULT_GROUP}
     */
    String groupId() default DEFAULT_GROUP;
    
    /**
     * Nacos 配置 Data ID（必填）。
     *
     * @return Data ID
     */
    String dataId();
    
    /**
     * 配置内容格式。
     *
     * @return 默认 {@link ConfigType#UNSET}
     */
    ConfigType type() default ConfigType.UNSET;
    
    /**
     * Nacos 配置变更时是否自动刷新绑定对象属性。
     *
     * @return 默认 {@code false}
     */
    boolean autoRefreshed() default false;
    
    /**
     * 绑定过程中是否忽略无效字段（类型不匹配或无法 coercion 的字段）。
     *
     * @return 默认 {@code false}
     */
    boolean ignoreInvalidFields() default false;
    
    /**
     * 是否忽略属性名中包含点号的嵌套字段。
     *
     * @return 默认 {@code false}
     */
    boolean ignoreNestedProperties() default false;
    
    /**
     * 是否忽略配置中存在但 POJO 中无对应字段的未知属性。
     *
     * @return 默认 {@code true}
     */
    boolean ignoreUnknownFields() default true;
    
    /**
     * 校验失败时是否抛出异常；为 {@code false} 时仅记录日志不向上传播。
     *
     * @return 默认 {@code true}
     */
    boolean exceptionIfInvalid() default true;
    
    /**
     * 绑定使用的 {@link NacosProperties}；未指定时使用全局 Nacos 属性。
     *
     * @return Nacos 客户端属性
     */
    NacosProperties properties() default @NacosProperties;
    
}
