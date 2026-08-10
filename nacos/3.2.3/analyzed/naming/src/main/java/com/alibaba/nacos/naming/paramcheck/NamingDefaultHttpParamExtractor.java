/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.paramcheck;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.common.paramcheck.ParamInfo;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.paramcheck.AbstractHttpParamExtractor;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Naming 默认 HTTP 请求参数提取器。
 *
 * <p>从 {@link HttpServletRequest} 解析 IP、端口、命名空间、集群、服务名、分组与元数据等字段，组装为 {@link ParamInfo} 供参数校验插件使用；支持多种参数别名。</p>
 *
 * @author zhuoguang
 */
public class NamingDefaultHttpParamExtractor extends AbstractHttpParamExtractor {
    
    /** 提取通用实例操作 HTTP 参数并解析 group@@service 组合格式。 */
    @Override
    public List<ParamInfo> extractParam(HttpServletRequest request) throws NacosException {
        ParamInfo paramInfo = new ParamInfo();
        paramInfo.setIp(getAliasIp(request));
        paramInfo.setPort(getAliasPort(request));
        paramInfo.setNamespaceId(getAliasNamespaceId(request));
        paramInfo.setCluster(getAliasClusterName(request));
        String serviceName = getAliasServiceName(request);
        String groupName = getAliasGroupName(request);
        String groupServiceName = serviceName;
        if (StringUtils.isNotBlank(groupServiceName)
            && groupServiceName.contains(Constants.SERVICE_INFO_SPLITER)) {
            String[] splits = groupServiceName.split(Constants.SERVICE_INFO_SPLITER, 2);
            groupName = splits[0];
            serviceName = splits[1];
        }
        paramInfo.setServiceName(serviceName);
        paramInfo.setGroup(groupName);
        paramInfo.setMetadata(UtilsAndCommons.parseMetadata(request.getParameter("metadata")));
        ArrayList<ParamInfo> paramInfos = new ArrayList<>();
        paramInfos.add(paramInfo);
        return paramInfos;
    }
    
    /** 读取 namespaceId 请求参数。 */
    private String getAliasNamespaceId(HttpServletRequest request) {
        String namespaceid = request.getParameter("namespaceId");
        return namespaceid;
    }
    
    /** 读取 ip 请求参数。 */
    private String getAliasIp(HttpServletRequest request) {
        String ip = request.getParameter("ip");
        return ip;
    }
    
    /** 读取 port 或 checkPort 请求参数。 */
    private String getAliasPort(HttpServletRequest request) {
        String port = request.getParameter("port");
        if (StringUtils.isBlank(port)) {
            port = request.getParameter("checkPort");
        }
        return port;
    }
    
    /** 读取 serviceName 或 serviceNameParam 请求参数。 */
    private String getAliasServiceName(HttpServletRequest request) {
        String serviceName = request.getParameter("serviceName");
        if (StringUtils.isBlank(serviceName)) {
            serviceName = request.getParameter("serviceNameParam");
        }
        return serviceName;
    }
    
    /** 读取 groupName 或 groupNameParam 请求参数。 */
    private String getAliasGroupName(HttpServletRequest request) {
        String groupName = request.getParameter("groupName");
        if (StringUtils.isBlank(groupName)) {
            groupName = request.getParameter("groupNameParam");
        }
        return groupName;
    }
    
    /** 读取 clusterName、cluster 或 clusters 请求参数。 */
    private String getAliasClusterName(HttpServletRequest request) {
        String clusterName = request.getParameter("clusterName");
        if (StringUtils.isBlank(clusterName)) {
            clusterName = request.getParameter("cluster");
        } else if (StringUtils.isBlank(clusterName)) {
            clusterName = request.getParameter("clusters");
        }
        return clusterName;
    }
}
