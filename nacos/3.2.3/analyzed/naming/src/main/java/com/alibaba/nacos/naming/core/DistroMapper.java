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

package com.alibaba.nacos.naming.core;

import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.core.cluster.MemberChangeListener;
import com.alibaba.nacos.core.cluster.MemberUtil;
import com.alibaba.nacos.core.cluster.MembersChangeEvent;
import com.alibaba.nacos.api.common.NodeState;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.naming.misc.Loggers;
import com.alibaba.nacos.naming.misc.SwitchDomain;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Distro 分片映射器，判定本节点是否负责指定标签的数据。
 *
 * <p>标签在 v1 为 serviceName，v2 为 ip:port；healthyList 全集群排序后保证各节点一致。</p>
 *
 * @author nkorange
 */
@Component("distroMapper")
public class DistroMapper extends MemberChangeListener {
    
    /** 健康节点地址列表，全集群必须保持相同排序。 */
    /**
     * List of service nodes, you must ensure that the order of healthyList is the same for all nodes.
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    /** 当前 UP/SUSPICIOUS 成员的有序地址列表。 */
    private volatile List<String> healthyList = new ArrayList<>();
    
    /** 运维开关域，可关闭 Distro 分片。 */
    private final SwitchDomain switchDomain;
    
    /** 集群成员管理器。 */
    private final ServerMemberManager memberManager;
    
    public DistroMapper(ServerMemberManager memberManager, SwitchDomain switchDomain) {
        this.memberManager = memberManager;
        this.switchDomain = switchDomain;
    }
    
    public List<String> getHealthyList() {
        return healthyList;
    }
    
    /** 订阅成员变更并初始化 healthyList。 */
    /**
     * init server list.
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    @PostConstruct
    public void init() {
        NotifyCenter.registerSubscriber(this);
        this.healthyList = MemberUtil.simpleMembers(memberManager.allMembers());
    }
    
    /**
     * Judge whether current server is responsible for input tag.
     *
     * @param responsibleTag responsible tag, serviceName for v1 and ip:port for v2
     * @return true if input service is response, otherwise false
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    /** 判断本节点是否负责处理指定 responsibleTag 的请求/数据。 */
    public boolean responsible(String responsibleTag) {
        final List<String> servers = healthyList;
        
        if (!switchDomain.isDistroEnabled() || EnvUtil.getStandaloneMode()) {
            return true;
        }
        
        if (CollectionUtils.isEmpty(servers)) {
            // Distro 配置尚未就绪
            return false;
        }
        
        String localAddress = EnvUtil.getLocalAddress();
        int index = servers.indexOf(localAddress);
        int lastIndex = servers.lastIndexOf(localAddress);
        if (lastIndex < 0 || index < 0) {
            return true;
        }
        
        int target = distroHash(responsibleTag) % servers.size();
        return target >= index && target <= lastIndex;
    }
    
    /**
     * Calculate which other server response input tag.
     *
     * @param responsibleTag responsible tag, serviceName for v1 and ip:port for v2
     * @return server which response input service
      * <p>Nacos 命名模块控制器与核心运维接口；详见上方类/接口说明。</p>
     */
    /** 计算负责指定标签的对端节点地址。 */
    public String mapSrv(String responsibleTag) {
        final List<String> servers = healthyList;
        
        if (CollectionUtils.isEmpty(servers) || !switchDomain.isDistroEnabled()) {
            return EnvUtil.getLocalAddress();
        }
        
        try {
            int index = distroHash(responsibleTag) % servers.size();
            return servers.get(index);
        } catch (Throwable e) {
            Loggers.SRV_LOG
                .warn("[NACOS-DISTRO] distro mapper failed, return localhost: "
                    + EnvUtil.getLocalAddress(), e);
            return EnvUtil.getLocalAddress();
        }
    }
    
    /** 对 responsibleTag 做一致性哈希取模。 */
    private int distroHash(String responsibleTag) {
        return Math.abs(responsibleTag.hashCode() % Integer.MAX_VALUE);
    }
    
    @Override
    public void onEvent(MembersChangeEvent event) {
        // 节点列表必须排序，确保各 nacos-server 的 healthyList 顺序一致
        List<String> list =
            MemberUtil.simpleMembers(MemberUtil.selectTargetMembers(event.getMembers(),
                member -> NodeState.UP.equals(member.getState())
                    || NodeState.SUSPICIOUS.equals(member.getState())));
        Collections.sort(list);
        Collection<String> old = healthyList;
        healthyList = Collections.unmodifiableList(list);
        Loggers.SRV_LOG.info("[NACOS-DISTRO] healthy server list changed, old: {}, new: {}", old,
            healthyList);
    }
    
    @Override
    public boolean ignoreExpireEvent() {
        return true;
    }
}
