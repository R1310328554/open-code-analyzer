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

import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.config.convert.NacosConfigConverter;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.alibaba.nacos.api.common.Constants.DEFAULT_GROUP;

/**
 * 标记方法为 Nacos 配置变更监听器。
 *
 * <p>配置变更时框架回调标注方法，可配合 {@link NacosConfigConverter} 做类型转换。</p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 0.2.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface NacosConfigListener {
    
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
     * 配置内容类型。
     *
     * @return 默认 {@link ConfigType#UNSET}
     */
    ConfigType type() default ConfigType.UNSET;
    
    /**
     * 指定 {@link NacosConfigConverter} 实现类，将配置字符串转为方法参数类型。
     *
     * @return 转换器实现类
     */
    Class<? extends NacosConfigConverter> converter() default NacosConfigConverter.class;
    
    /**
     * 监听器使用的 {@link NacosProperties}；未指定时使用全局 Nacos 属性。
     *
     * @return Nacos 客户端属性
     */
    NacosProperties properties() default @NacosProperties;
    
    /**
     * 监听器回调最大执行超时（毫秒），防止长时间阻塞影响其他配置。
     *
     * @return 默认 1000 毫秒
     */
    long timeout() default 1000L;
    
}
