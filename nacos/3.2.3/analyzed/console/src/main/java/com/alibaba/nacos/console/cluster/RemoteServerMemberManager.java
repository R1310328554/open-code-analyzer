/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.console.cluster;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.notify.Event;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.console.handler.impl.remote.EnabledRemoteHandler;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.MemberLookup;
import com.alibaba.nacos.core.cluster.MembersChangeEvent;
import com.alibaba.nacos.core.cluster.NacosMemberManager;
import com.alibaba.nacos.core.cluster.lookup.LookupFactory;
import com.alibaba.nacos.core.utils.Loggers;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * 远程 Nacos Server 集群成员管理器：仅在控制台远程模式下维护并同步后端 Server 节点列表。
 * Nacos remote server members manager. Only working on console mode to keep and update the remote server members.
 *
 * @author xiweng.yy
 */
@Service
@EnabledRemoteHandler
public class RemoteServerMemberManager implements NacosMemberManager {
    
    /**
     * 远程 Server 集群节点表，键为节点地址。
     * Nacos remote servers cluster node list.
     */
    private volatile ConcurrentSkipListMap<String, Member> serverList;
    
    /**
     * 成员寻址实现（如文件/地址服务器），负责发现与刷新 Server 列表。
     * Addressing pattern instances.
     */
    private MemberLookup lookup;
    
    /** 初始化空的 Server 成员映射 */
    public RemoteServerMemberManager() {
        this.serverList = new ConcurrentSkipListMap<>();
    }
    
    /** Spring 容器就绪后创建并启动成员寻址 */
    @PostConstruct
    public void init() throws NacosException {
        initAndStartLookup();
    }
    
    /** 通过 {@link LookupFactory} 创建寻址器并绑定本管理器 */
    private void initAndStartLookup() throws NacosException {
        this.lookup = LookupFactory.createLookUp();
        this.lookup.injectMemberManager(this);
        this.lookup.start();
    }
    
    /**
     * 用新成员集合原子替换本地 Server 列表，并发布 {@link MembersChangeEvent}。
     *
     * @param members 最新成员快照
     * @return 始终返回 {@code true}
     */
    @Override
    public synchronized boolean memberChange(Collection<Member> members) {
        ConcurrentSkipListMap<String, Member> newServerList = new ConcurrentSkipListMap<>();
        for (Member each : members) {
            newServerList.put(each.getAddress(), each);
        }
        Loggers.CLUSTER.info("[serverlist] nacos remote server members changed to : {}",
            newServerList);
        this.serverList = newServerList;
        Event event = MembersChangeEvent.builder().members(members).build();
        NotifyCenter.publishEvent(event);
        return true;
    }
    
    /** {@inheritDoc} 返回当前已知远程 Server 成员的副本集合 */
    @Override
    public Collection<Member> allMembers() {
        return new HashSet<>(serverList.values());
    }
}
