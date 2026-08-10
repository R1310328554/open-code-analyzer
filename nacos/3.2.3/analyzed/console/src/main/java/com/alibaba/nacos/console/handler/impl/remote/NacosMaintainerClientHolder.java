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

package com.alibaba.nacos.console.handler.impl.remote;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.utils.ContextPathUtil;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.console.cluster.RemoteServerMemberManager;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.MemberChangeListener;
import com.alibaba.nacos.core.cluster.MembersChangeEvent;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerFactory;
import com.alibaba.nacos.maintainer.client.ai.AiMaintainerService;
import com.alibaba.nacos.maintainer.client.config.ConfigMaintainerFactory;
import com.alibaba.nacos.maintainer.client.config.ConfigMaintainerService;
import com.alibaba.nacos.maintainer.client.naming.NamingMaintainerFactory;
import com.alibaba.nacos.maintainer.client.naming.NamingMaintainerService;
import com.alibaba.nacos.sys.env.EnvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

/**
 * Nacos Maintainer 客户端持有者：在 Console 部署模式下聚合 Naming/Config/AI 远程运维客户端，并在集群成员变更时重建连接。
 * Nacos maintainer client holder.
 *
 * @author xiweng.yy
 */
@Component
@EnabledRemoteHandler
public class NacosMaintainerClientHolder extends MemberChangeListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NacosMaintainerClientHolder.class);
    
    /** 远程 Nacos 服务 context-path 配置键 */
    private static final String REMOTE_SERVER_CONTEXT_PATH_KEY =
        "nacos.console.remote.server.context-path";
    
    /** 默认远程 context-path */
    private static final String DEFAULT_REMOTE_SERVER_CONTEXT_PATH = "/nacos";
    
    /** 路径分隔符 */
    private static final String PATH_SEPARATOR = "/";
    
    /** 根路径长度（仅含单个 {@code /}） */
    private static final int ROOT_PATH_LENGTH = 1;
    
    /** 远程集群成员管理器 */
    private final RemoteServerMemberManager memberManager;
    
    /** Naming 远程运维客户端（成员变更时重建） */
    private volatile NamingMaintainerService namingMaintainerService;
    
    /** Config 远程运维客户端（成员变更时重建） */
    private volatile ConfigMaintainerService configMaintainerService;
    
    /** AI 远程运维客户端（成员变更时重建） */
    private volatile AiMaintainerService aiMaintainerService;
    
    /** 根据当前集群成员初始化各 Maintainer 客户端并订阅成员变更事件 */
    public NacosMaintainerClientHolder(RemoteServerMemberManager memberManager)
        throws NacosException {
        this.memberManager = memberManager;
        buildMaintainerService();
        NotifyCenter.registerSubscriber(this);
    }
    
    /** 按最新集群地址与 context-path 重建 Naming/Config/AI Maintainer 客户端 */
    private void buildMaintainerService() throws NacosException {
        List<String> memberAddress =
            memberManager.allMembers().stream().map(Member::getAddress).toList();
        String memberAddressString = StringUtils.join(memberAddress, ",");
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, memberAddressString);
        String remoteContextPath = resolveRemoteContextPath();
        properties.setProperty(PropertyKeyConst.CONTEXT_PATH, remoteContextPath);
        namingMaintainerService = NamingMaintainerFactory.createNamingMaintainerService(properties);
        configMaintainerService = ConfigMaintainerFactory.createConfigMaintainerService(properties);
        aiMaintainerService = AiMaintainerFactory.createAiMaintainerService(properties);
    }
    
    /** 解析并规范化远程 Nacos 服务的 context-path */
    static String resolveRemoteContextPath() {
        String remoteContextPath =
            EnvUtil.getProperty(REMOTE_SERVER_CONTEXT_PATH_KEY, DEFAULT_REMOTE_SERVER_CONTEXT_PATH);
        remoteContextPath = StringUtils.trim(remoteContextPath);
        remoteContextPath = ContextPathUtil.normalizeContextPath(remoteContextPath);
        while (remoteContextPath.endsWith(PATH_SEPARATOR)
            && remoteContextPath.length() > ROOT_PATH_LENGTH) {
            remoteContextPath = remoteContextPath.substring(0, remoteContextPath.length() - 1);
        }
        return remoteContextPath;
    }
    
    /** 返回 Naming Maintainer 客户端 */
    public NamingMaintainerService getNamingMaintainerService() {
        return namingMaintainerService;
    }
    
    /** 返回 Config Maintainer 客户端 */
    public ConfigMaintainerService getConfigMaintainerService() {
        return configMaintainerService;
    }
    
    /** 返回 AI Maintainer 客户端 */
    public AiMaintainerService getAiMaintainerService() {
        return aiMaintainerService;
    }
    
    /** 集群成员变更时重建 Maintainer 客户端 */
    @Override
    public void onEvent(MembersChangeEvent event) {
        try {
            buildMaintainerService();
        } catch (NacosException e) {
            LOGGER.warn("Nacos Server members changed, but build new maintain client failed with: ",
                e);
        }
    }
}
