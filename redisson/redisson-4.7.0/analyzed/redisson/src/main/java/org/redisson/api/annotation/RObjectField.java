/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.redisson.client.codec.BaseCodec;
import org.redisson.client.codec.Codec;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;
import org.redisson.liveobject.resolver.DefaultNamingScheme;
import org.redisson.liveobject.resolver.NamingScheme;

/**
 * 默认情况下 {@link REntity} 上的 {@code namingScheme} 与 {@code codec} 会应用到每个 Live Object 字段。
 * 
 * 本注解允许为除 {@link RId} 主键外的任意字段单独指定 {@code namingScheme} 和/或 {@code codec}。
 *
 * @author Rui Gu (https://github.com/jackygurui)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface RObjectField{

    /**
     * （可选）字段级 Live Object 命名方案；定义嵌套对象在 Redis 中的键名规则。
     * 用于引用已有对象或物化新实例。默认为 {@link DefaultNamingScheme}。
     * 
     * @return 命名方案实现类
     */
    Class<? extends NamingScheme> namingScheme() default DefaultNamingScheme.class;

    /**
     * （可选）字段级 Live Object 状态编解码器。
     * 为 {@code null} 时使用 Redisson 配置中的默认 {@link Codec}。
     * 
     * @return 编解码器实现类
     */
    Class<? extends Codec> codec() default DEFAULT.class;
    
    final class DEFAULT extends BaseCodec {
        @Override
        public Decoder<Object> getValueDecoder() {
            return null;
        }

        @Override
        public Encoder getValueEncoder() {
            return null;
        }
    }
    
}
