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

import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.consistency.Config;
import com.alibaba.nacos.consistency.ap.APProtocol;
import com.alibaba.nacos.consistency.cp.CPProtocol;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.MemberChangeListener;
import com.alibaba.nacos.core.cluster.MemberMetaDataConstants;
import com.alibaba.nacos.core.cluster.MemberUtil;
import com.alibaba.nacos.core.cluster.MembersChangeEvent;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.utils.ClassUtils;
import com.alibaba.nacos.sys.utils.ApplicationUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 一致性协议管理器：负责 Nacos 中 CP（Raft）与 AP（Distro）两类一致性协议的生命周期，包括懒加载初始化、集群成员注入、节点变更通知与销毁关闭。
 * Conformance protocol management, responsible for managing the lifecycle of conformance protocols in Nacos.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
@SuppressWarnings("all")
@Component(value = "ProtocolManager")
public class ProtocolManager extends MemberChangeListener implements DisposableBean {
    
    /** CP 一致性协议实现（如 JRaft）。 */
    private CPProtocol cpProtocol;
    
    /** AP 一致性协议实现（如 Distro）。 */
    private APProtocol apProtocol;
    
    /** 集群成员管理器，提供本机与全量成员信息。 */
    private final ServerMemberManager memberManager;
    
    /** AP 协议是否已完成初始化。 */
    private volatile boolean apInit = false;
    
    /** CP 协议是否已完成初始化。 */
    private volatile boolean cpInit = false;
    
    /** CP 协议懒加载互斥锁。 */
    private final Object cpLock = new Object();
    
    /** AP 协议懒加载互斥锁。 */
    private final Object apLock = new Object();
    
    /** 上一次成员快照（预留字段）。 */
    private Set<Member> oldMembers;
    
    /** 构造并注册为集群成员变更订阅者。 */
    public ProtocolManager(ServerMemberManager memberManager) {
        this.memberManager = memberManager;
        NotifyCenter.registerSubscriber(this);
    }
    
    /** 将成员集合转换为 AP 协议所需的地址集合（{@code ip:port}）。 */
    public static Set<String> toAPMembersInfo(Collection<Member> members) {
        Set<String> nodes = new HashSet<>();
        members.forEach(member -> nodes.add(member.getAddress()));
        return nodes;
    }
    
    /** 将成员集合转换为 CP 协议所需的 Raft 地址集合（{@code ip:raftPort}）。 */
    public static Set<String> toCPMembersInfo(Collection<Member> members) {
        Set<String> nodes = new HashSet<>();
        members.forEach(member -> {
            final String ip = member.getIp();
            final int raftPort = MemberUtil.calculateRaftPort(member);
            nodes.add(ip + ":" + raftPort);
        });
        return nodes;
    }
    
    /** 懒加载并返回 CP 协议实例（双重检查锁）。 */
    public CPProtocol getCpProtocol() {
        if (!cpInit) {
            synchronized (cpLock) {
                if (!cpInit) {
                    initCPProtocol();
                    cpInit = true;
                }
            }
        }
        return cpProtocol;
    }
    
    /** 懒加载并返回 AP 协议实例（双重检查锁）。 */
    public APProtocol getApProtocol() {
        if (!apInit) {
            synchronized (apLock) {
                if (!apInit) {
                    initAPProtocol();
                    apInit = true;
                }
            }
        }
        return apProtocol;
    }
    
    /** 返回 CP 协议是否已初始化。 */
    public boolean isCpInit() {
        return cpInit;
    }
    
    /** 返回 AP 协议是否已初始化。 */
    public boolean isApInit() {
        return apInit;
    }
    
    /** 容器销毁时依次关闭 AP 与 CP 协议。 */
    @PreDestroy
    @Override
    public void destroy() {
        if (Objects.nonNull(apProtocol)) {
            apProtocol.shutdown();
        }
        if (Objects.nonNull(cpProtocol)) {
            cpProtocol.shutdown();
        }
    }
    
    /** 从 Spring 容器查找 AP 协议 Bean，注入成员列表并初始化。 */
    private void initAPProtocol() {
        ApplicationUtils.getBeanIfExist(APProtocol.class, protocol -> {
            Class configType = ClassUtils.resolveGenericType(protocol.getClass());
            Config config = (Config) ApplicationUtils.getBean(configType);
            injectMembers4AP(config);
            protocol.init(config);
            ProtocolManager.this.apProtocol = protocol;
        });
    }
    
    /** 从 Spring 容器查找 CP 协议 Bean，注入成员列表并初始化。 */
    private void initCPProtocol() {
        ApplicationUtils.getBeanIfExist(CPProtocol.class, protocol -> {
            Class configType = ClassUtils.resolveGenericType(protocol.getClass());
            Config config = (Config) ApplicationUtils.getBean(configType);
            injectMembers4CP(config);
            protocol.init(config);
            ProtocolManager.this.cpProtocol = protocol;
        });
    }
    
    /** 向 CP 配置注入本机 Raft 地址与集群其他节点地址。 */
    private void injectMembers4CP(Config config) {
        final Member selfMember = memberManager.getSelf();
        final String self = selfMember.getIp() + ":" + Integer
            .parseInt(String.valueOf(selfMember.getExtendVal(MemberMetaDataConstants.RAFT_PORT)));
        Set<String> others = toCPMembersInfo(memberManager.allMembers());
        config.setMembers(self, others);
    }
    
    /** 向 AP 配置注入本机服务地址与集群其他节点地址。 */
    private void injectMembers4AP(Config config) {
        final String self = memberManager.getSelf().getAddress();
        Set<String> others = toAPMembersInfo(memberManager.allMembers());
        config.setMembers(self, others);
    }
    
    /** 集群成员变更时，异步通知 AP/CP 协议更新成员视图。 */
    @Override
    public void onEvent(MembersChangeEvent event) {
        // 节点变更事件的时序很重要：例如 T1 发生事件 A、T2 发生事件 B（T1 < T2）。
        // 不同协议的变更通知互不阻塞，且各自通过单线程池投递，避免并发冲击一致性层。
        if (Objects.nonNull(apProtocol)) {
            ProtocolExecutor
                .apMemberChange(() -> apProtocol.memberChange(toAPMembersInfo(event.getMembers())));
        }
        if (Objects.nonNull(cpProtocol)) {
            ProtocolExecutor
                .cpMemberChange(() -> cpProtocol.memberChange(toCPMembersInfo(event.getMembers())));
        }
    }
}
