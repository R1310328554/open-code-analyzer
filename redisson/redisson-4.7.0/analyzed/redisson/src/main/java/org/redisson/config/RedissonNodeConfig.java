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
package org.redisson.config;

import org.springframework.beans.factory.BeanFactory;

/**
 * Redisson Node（嵌入式计算节点）的编程式配置。
 * <p>在 {@link RedissonNodeFileConfig} 基础上增加 Spring {@link org.springframework.beans.factory.BeanFactory}，
 * 使 MapReduce/Executor 任务可注入 {@code @Autowired}、{@code @Value} 等依赖。
 *
 * @author Nikita Koksharov
 *
 */
public class RedissonNodeConfig extends RedissonNodeFileConfig {
    
    /** Spring 容器，供分布式任务执行时解析 Bean 依赖。 */
    private BeanFactory beanFactory;

    /** 默认构造。 */
    public RedissonNodeConfig() {
        super();
    }

    /** 从 {@link Config} 拷贝 Redis 连接等基础配置。 */
    public RedissonNodeConfig(Config oldConf) {
        super(oldConf);
    }
    
    /** 拷贝构造，含 BeanFactory。 */
    public RedissonNodeConfig(RedissonNodeConfig oldConf) {
        super(oldConf);
        this.beanFactory = oldConf.beanFactory;
    }
    
    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * 设置 Spring BeanFactory，使 Node 上执行的 MapReduce/Executor 任务
     * 支持 Spring {@code @Autowired}、{@code @Value} 或 JSR-330 {@code @Inject}。
     *
     * @param beanFactory Spring BeanFactory 实例
     */
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

}
