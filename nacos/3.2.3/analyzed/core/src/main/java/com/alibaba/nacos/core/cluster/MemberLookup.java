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

package com.alibaba.nacos.core.cluster;

import com.alibaba.nacos.api.exception.NacosException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * 集群成员寻址策略接口：定义启动、注入成员管理器、发现节点与销毁等生命周期。
 * Member node addressing mode.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public interface MemberLookup {
    
    /**
     * 启动寻址组件并开始发现集群节点。
     *
     * @throws NacosException NacosException
     */
    void start() throws NacosException;
    
    /**
     * 是否通过独立 address-server 获取成员列表。
     *
     * @return using address server or not.
     */
    boolean useAddressServer();
    
    /**
     * 注入 {@link ServerMemberManager}，供寻址结果回写集群视图。
     *
     * @param memberManager {@link NacosMemberManager}
     */
    void injectMemberManager(NacosMemberManager memberManager);
    
    /**
     * 寻址完成后的回调，将发现的成员集合交给成员管理器处理。
     *
     * @param members {@link Collection}
     */
    void afterLookup(Collection<Member> members);
    
    /**
     * 关闭寻址模式并释放相关资源。
     *
     * @throws NacosException NacosException
     */
    void destroy() throws NacosException;
    
    /**
     * 返回当前寻址模式的诊断信息（默认空映射）。
     *
     * @return {@link Map}
     */
    default Map<String, Object> info() {
        return Collections.emptyMap();
    }
    
}
