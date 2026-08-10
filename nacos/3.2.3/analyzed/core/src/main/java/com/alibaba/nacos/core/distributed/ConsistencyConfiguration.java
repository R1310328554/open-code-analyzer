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

package com.alibaba.nacos.core.distributed;

import com.alibaba.nacos.common.spi.NacosServiceLoader;
import com.alibaba.nacos.consistency.cp.CPProtocol;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.distributed.raft.JRaftProtocol;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * 一致性协议 Spring 配置：通过 SPI 或默认 {@link JRaftProtocol} 注册 CP 强一致协议 Bean。
 * consistency configuration.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@Configuration
public class ConsistencyConfiguration {
    
    /**
     * 注册名为 strongAgreementProtocol 的 CP 协议 Bean。
     *
     * @param memberManager 集群成员管理器
     * @return CP 一致性协议实例
     * @throws Exception 协议初始化失败
     */
    @Bean(value = "strongAgreementProtocol")
    public CPProtocol strongAgreementProtocol(ServerMemberManager memberManager) throws Exception {
        final CPProtocol protocol =
            getProtocol(CPProtocol.class, () -> new JRaftProtocol(memberManager));
        return protocol;
    }
    
    /**
     * 通过 {@link NacosServiceLoader} 加载协议实现，无 SPI 时回退 builder。
     *
     * @param cls 协议接口类型
     * @param builder 默认实现工厂
     * @return 协议实例
     * @throws Exception 构造异常
     */
    private <T> T getProtocol(Class<T> cls, Callable<T> builder) throws Exception {
        Collection<T> protocols = NacosServiceLoader.load(cls);
        
        // 仅选用 SPI 列表中的第一个实现
        
        Iterator<T> iterator = protocols.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return builder.call();
        }
    }
    
}
