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
package org.redisson.tomcat;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.catalina.LifecycleException;
import org.redisson.api.RedissonClient;

/**
 * 基于 JNDI 查找 {@link org.redisson.api.RedissonClient} 的 Tomcat Session 管理器。
 * <p>适用于应用服务器已托管 Redisson 实例、Tomcat 仅引用而不自行创建客户端的场景。
 * 不支持 {@code configPath} 配置文件方式。
 *
 * @author Nikita Koksharov
 */
public class JndiRedissonSessionManager extends RedissonSessionManager {

    private String jndiName;

    /** JNDI 模式下禁止使用配置文件路径。 */
    @Override
    public void setConfigPath(String configPath) {
        throw new IllegalArgumentException("configPath is unavaialble for JNDI based manager");
    }

    /** 从 {@code java:comp/env} 按 {@link #jndiName} 查找 {@link org.redisson.api.RedissonClient}。 */
    @Override
    protected RedissonClient buildClient() throws LifecycleException {
        InitialContext context = null;
        try {
            context = new InitialContext();
            Context envCtx = (Context) context.lookup("java:comp/env");
            return (RedissonClient) envCtx.lookup(jndiName);
        } catch (NamingException e) {
            throw new LifecycleException("Unable to locate Redisson instance by name: " + jndiName, e);
        } finally {
            if (context != null) {
                try {
                    context.close();
                } catch (NamingException e) {
                    throw new LifecycleException("Unable to close JNDI context", e);
                }
            }
        }
    }

    /** 返回 JNDI 环境条目名称。 */
    public String getJndiName() {
        return jndiName;
    }

    /** 设置 JNDI 环境条目名称（如 {@code redisson}）。 */
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    /** JNDI 托管的 Redisson 由容器生命周期管理，此处不关闭。 */
    @Override
    protected void shutdownRedisson() {
    }
    
}
