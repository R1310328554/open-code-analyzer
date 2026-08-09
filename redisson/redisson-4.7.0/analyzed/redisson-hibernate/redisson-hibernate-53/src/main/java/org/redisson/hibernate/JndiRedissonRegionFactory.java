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

import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.cache.CacheException;
import org.hibernate.engine.jndi.internal.JndiServiceImpl;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.redisson.api.RedissonClient;

/**
 * 基于 Redisson 的 Hibernate 缓存区域工厂。
 * <p>从 JNDI 查找已部署的 {@link RedissonClient}，而非自行创建实例。
 *
 * @author Nikita Koksharov
 */
public class JndiRedissonRegionFactory extends RedissonRegionFactory {

    private static final long serialVersionUID = -4814502675083325567L;

    /** JNDI 查找 Redisson 客户端所用的配置键（{@code hibernate.cache.redisson.jndi_name}）。 */
    public static final String JNDI_NAME = CONFIG_PREFIX + "jndi_name";
    
    /** 从 JNDI 按配置名查找 {@link RedissonClient}（Hibernate 5.3）。
     *
     * @param properties Hibernate 缓存属性
     * @return 已绑定的 Redisson 客户端
     * @throws CacheException JNDI 名未配置或查找/关闭上下文失败
     */
    @Override
    protected RedissonClient createRedissonClient(Map properties) {
        String jndiName = ConfigurationHelper.getString(JNDI_NAME, properties);
        // 未配置 JNDI 名则无法查找客户端。
        if (jndiName == null) {
            throw new CacheException(JNDI_NAME + " property not set");
        }
        
        Properties jndiProperties = JndiServiceImpl.extractJndiProperties(properties);
        InitialContext context = null;
        try {
            context = new InitialContext(jndiProperties);
            // 按 JNDI 名查找 Redisson 客户端。
            return (RedissonClient) context.lookup(jndiName);
        } catch (NamingException e) {
            throw new CacheException("Unable to locate Redisson instance by name: " + jndiName, e);
        } finally {
            // 查找完成后关闭 JNDI 上下文。
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    throw new CacheException("Unable to close JNDI context", e);
                }
            }
        }
    }

    /** JNDI 模式下不销毁外部管理的 Redisson 实例。 */
    @Override
    protected void releaseFromUse() {
    }

}
