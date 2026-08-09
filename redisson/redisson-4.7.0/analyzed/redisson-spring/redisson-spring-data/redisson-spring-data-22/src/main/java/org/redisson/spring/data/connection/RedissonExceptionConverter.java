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
package org.redisson.spring.data.connection;

import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisException;
import org.redisson.client.RedisRedirectException;
import org.redisson.client.RedisTimeoutException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.ClusterRedirectException;
import org.springframework.data.redis.RedisConnectionFailureException;

/**
 * 将 Redisson 客户端异常映射为 Spring Data Redis {@link DataAccessException}。
 * <p>连接失败、集群重定向、超时与通用 Redis 错误分别对应不同 Spring 异常类型。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonExceptionConverter implements Converter<Exception, DataAccessException> {

    @Override
    public DataAccessException convert(Exception source) {
        // 连接层错误 -> RedisConnectionFailureException。
        if (source instanceof RedisConnectionException) {
            return new RedisConnectionFailureException(source.getMessage(), source);
        }
        // 集群 MOVED/ASK 重定向 -> ClusterRedirectException。
        if (source instanceof RedisRedirectException) {
            RedisRedirectException ex = (RedisRedirectException) source;
            return new ClusterRedirectException(ex.getSlot(), ex.getUrl().getHost(), ex.getUrl().getPort(), source);
        }

        // 命令超时 -> QueryTimeoutException。
        if (source instanceof RedisTimeoutException) {
            return new QueryTimeoutException(source.getMessage(), source);
        }

        // 其他 Redis 协议错误 -> InvalidDataAccessApiUsageException。
        if (source instanceof RedisException) {
            return new InvalidDataAccessApiUsageException(source.getMessage(), source);
        }
        
        if (source instanceof DataAccessException) {
            return (DataAccessException) source;
        }
        
        return null;
    }

}
