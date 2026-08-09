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
package org.redisson;

import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import javax.naming.*;
import javax.naming.spi.ObjectFactory;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * JNDI {@link ObjectFactory}：从 Reference 中的 YAML 配置路径创建 {@link RedissonClient}。
 * <p>适用于应用服务器将 Redisson 实例绑定到 JNDI 后供组件查找。
 *
 * @author Nikita Koksharov
 */
public class JndiRedissonFactory implements ObjectFactory {

    /** 解析 JNDI {@link Reference} 的 {@code configPath} 地址并构建客户端。 */
    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
            throws Exception {
        Reference ref = (Reference) obj;
        RefAddr addr = ref.get("configPath");
        return buildClient(addr.getContent().toString());
    }
    
    /** 从 YAML 文件加载 {@link Config} 并调用 {@link Redisson#create}。
     *
     * @param configPath Redisson YAML 配置文件路径
     * @throws NamingException 配置解析或客户端创建失败
     */
    protected RedissonClient buildClient(String configPath) throws NamingException {
        Config config = null;
        try {
            config = Config.fromYAML(new File(configPath), getClass().getClassLoader());
        } catch (IOException e) {
            throw new NamingException("Can't parse config " + configPath);
        }
        
        try {
            return Redisson.create(config);
        } catch (Exception e) {
            NamingException ex = new NamingException();
            ex.initCause(e);
            throw ex;
        }
    }

}
