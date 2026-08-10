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

package com.alibaba.nacos.config.server.service.listener;

import com.alibaba.nacos.api.config.model.ConfigListenerInfo;
import com.alibaba.nacos.api.model.v2.Result;
import com.alibaba.nacos.auth.config.NacosAuthConfig;
import com.alibaba.nacos.auth.config.NacosAuthConfigHolder;
import com.alibaba.nacos.auth.util.AuthHeaderUtil;
import com.alibaba.nacos.common.constant.HttpHeaderConsts;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.common.model.RestResult;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.config.server.service.notify.HttpClientManager;
import com.alibaba.nacos.config.server.utils.LogUtil;
import com.alibaba.nacos.core.auth.NacosServerAuthConfig;
import com.alibaba.nacos.core.cluster.Member;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.sys.env.EnvUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;

import static com.alibaba.nacos.common.constant.RequestUrlConstants.HTTP_PREFIX;

/**
 * 远程集群成员监听状态查询：向除本节点外的各 Member 发起 HTTP 请求，
 * 聚合返回的 {@link ConfigListenerInfo}，用于集群级监听分布视图。
 * Local implementation for Config listener state service.
 *
 * @author xiweng.yy
 */
@Service
public class RemoteConfigListenerStateServiceImpl implements ConfigListenerStateService {
    
    /** 集群内查询单配置监听状态的 Admin V3 相对路径。 */
    private static final String CONFIG_LISTENER_STATE_URL =
        Constants.CONFIG_ADMIN_V3_PATH + "/listener";
    
    private final ServerMemberManager memberManager;
    
    private final ConfigListenerInfo emptyConfigListenerInfo;
    
    public RemoteConfigListenerStateServiceImpl(ServerMemberManager memberManager) {
        this.memberManager = memberManager;
        this.emptyConfigListenerInfo = new ConfigListenerInfo();
        this.emptyConfigListenerInfo.setListenersStatus(Collections.emptyMap());
    }
    
    @Override
    public ConfigListenerInfo getListenerState(String dataId, String groupName,
        String namespaceId) {
        // 构造查询参数，aggregation=false 避免远程节点再次递归聚合
                .addParam("namespaceId", namespaceId).addParam("aggregation", false);
        Header header = buildHeader();
        ConfigListenerInfo result = new ConfigListenerInfo();
        result.setListenersStatus(new HashMap<>(16));
        result.setQueryType(ConfigListenerInfo.QUERY_TYPE_CONFIG);
        // 遍历集群成员（不含本节点）并发 HTTP 拉取监听快照
            String url = getUrl(each.getAddress(), CONFIG_LISTENER_STATE_URL);
            ConfigListenerInfo oneNodeResult = invokeUrl(url, query, header);
            result.getListenersStatus().putAll(oneNodeResult.getListenersStatus());
        }
        return result;
    }
    
    @Override
    public ConfigListenerInfo getListenerStateByIp(String ip) {
        Query query = Query.newInstance().addParam("ip", ip).addParam("aggregation", false);
        Header header = buildHeader();
        ConfigListenerInfo result = new ConfigListenerInfo();
        result.setListenersStatus(new HashMap<>(16));
        result.setQueryType(ConfigListenerInfo.QUERY_TYPE_IP);
        for (Member each : memberManager.allMembersWithoutSelf()) {
            String url = getUrl(each.getAddress(), Constants.LISTENER_CONTROLLER_V3_ADMIN_PATH);
            ConfigListenerInfo oneNodeResult = invokeUrl(url, query, header);
            result.getListenersStatus().putAll(oneNodeResult.getListenersStatus());
        }
        return result;
    }
    
    private String getUrl(String ip, String relativePath) {
        return HTTP_PREFIX + ip + EnvUtil.getContextPath() + relativePath;
    }
    
    private Header buildHeader() {
        Header header = Header.newInstance();
        header.addParam(HttpHeaderConsts.ACCEPT_CHARSET, Constants.ENCODE_UTF8);
        NacosAuthConfig authConfig = NacosAuthConfigHolder.getInstance()
            .getNacosAuthConfigByScope(NacosServerAuthConfig.NACOS_SERVER_AUTH_SCOPE);
        AuthHeaderUtil.addIdentityToHeader(header, authConfig);
        return header;
    }
    
    /**
     * 调用远程 URL 获取监听状态；失败时记录日志并返回空结果占位。
     *
     * @param url    完整 HTTP 地址
     * @param query  查询参数
     * @param header 含鉴权与编码的请求头
     * @return 解析后的监听信息或空对象
     */
    private ConfigListenerInfo invokeUrl(String url, Query query, Header header) {
        try {
            RestResult<String> restResult = HttpClientManager.getNacosRestTemplate()
                .get(url, header, query, String.class);
            if (!restResult.ok()) {
                LogUtil.DEFAULT_LOG.warn(
                    "Invoke remote server config listener state by url {} failed with code {}, msg {}",
                    url,
                    restResult.getCode(), restResult.getMessage());
                return emptyConfigListenerInfo;
            }
            Result<ConfigListenerInfo> result =
                JacksonUtils.toObj(restResult.getData(), new TypeReference<>() {
                });
            return result.getData();
        } catch (Exception e) {
            LogUtil.DEFAULT_LOG.error("Invoke remote server config listener by url {} failed :",
                url, e);
            return emptyConfigListenerInfo;
        }
    }
}
