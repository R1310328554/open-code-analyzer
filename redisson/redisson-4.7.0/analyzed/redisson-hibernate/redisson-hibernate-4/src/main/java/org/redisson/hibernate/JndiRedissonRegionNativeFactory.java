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
package org.redisson.hibernate;

import org.hibernate.cache.CacheException;
import org.hibernate.engine.jndi.internal.JndiServiceImpl;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.redisson.api.RedissonClient;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * 基于 Redisson 原生 Map 缓存的 Hibernate Region 工厂。
 * 通过 JNDI 查找已部署的 {@link RedissonClient}，配合 {@link RedissonRegionNativeFactory} 使用原生存储。
 *
 * @author Nikita Koksharov
 *
 */
public class JndiRedissonRegionNativeFactory extends RedissonRegionNativeFactory {

    private static final long serialVersionUID = -4814502675083325567L;

    /** JNDI 中 {@link RedissonClient} 绑定名的 Hibernate 属性键。 */
    public static final String JNDI_NAME = CONFIG_PREFIX + "jndi_name";
    
    /** 从 JNDI 查找 {@link RedissonClient}；未配置 {@link #JNDI_NAME} 时抛出 {@link CacheException}。 */
    @Override
    protected RedissonClient createRedissonClient(Properties properties) {
        String jndiName = ConfigurationHelper.getString(JNDI_NAME, properties);
        if (jndiName == null) {
            throw new CacheException(JNDI_NAME + " property not set");
        }
        
        Properties jndiProperties = JndiServiceImpl.extractJndiProperties(properties);
        InitialContext context = null;
        try {
            context = new InitialContext(jndiProperties);
            return (RedissonClient) context.lookup(jndiName);
        } catch (NamingException e) {
            throw new CacheException("Unable to locate Redisson instance by name: " + jndiName, e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    throw new CacheException("Unable to close JNDI context", e);
                }
            }
        }
    }

    /** JNDI 模式下不关闭外部管理的 Redisson 实例。 */
    @Override
    public void stop() {
    }

}
