/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.naming.utils;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.common.utils.MD5Utils;
import com.alibaba.nacos.naming.core.v2.client.Client;
import com.alibaba.nacos.naming.core.v2.client.impl.IpPortBasedClient;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.alibaba.nacos.naming.constants.Constants.DEFAULT_INSTANCE_WEIGHT;
import static com.alibaba.nacos.naming.constants.Constants.PUBLISH_INSTANCE_ENABLE;
import static com.alibaba.nacos.naming.constants.Constants.PUBLISH_INSTANCE_WEIGHT;
import static com.alibaba.nacos.naming.misc.UtilsAndCommons.DEFAULT_CLUSTER_NAME;

/**
 * Distro 客户端校验与摘要工具类。
 *
 * <p>为 {@link IpPortBasedClient} 构建唯一字符串、hashCode 与 MD5 校验和，用于集群 Distro 数据一致性比对。</p>
 *
 * @author Pixy Yuan
 * on 2021/10/9
 */
public class DistroUtils {
    
    /** 构建服务唯一键：namespace##groupedName##ephemeral。 */
    /** 生成 Distro 服务键字符串。 */
    public static String serviceKey(Service service) {
        return service.getNamespace()
            + "##"
            + service.getGroupedServiceName()
            + "##"
            + service.isEphemeral();
    }
    
    /** 基于客户端唯一字符串计算 hashCode。 */
    /** 对 buildUniqueString 结果取 hashCode。 */
    public static int stringHash(Client client) {
        String s = buildUniqueString(client);
        if (s == null) {
            return 0;
        }
        return s.hashCode();
    }
    
    /** 直接对客户端发布信息计算 hash，避免构建长字符串。 */
    /** 聚合 clientId 与各服务发布信息 hash 值。 */
    public static int hash(Client client) {
        if (!(client instanceof IpPortBasedClient)) {
            return 0;
        }
        return Objects.hash(client.getClientId(),
            client.getAllPublishedService().stream()
                .map(s -> {
                    InstancePublishInfo ip = client.getInstancePublishInfo(s);
                    double weight = getWeight(ip);
                    Boolean enabled = getEnabled(ip);
                    String cluster =
                        StringUtils.defaultIfBlank(ip.getCluster(), DEFAULT_CLUSTER_NAME);
                    return Objects.hash(
                        s.getNamespace(),
                        s.getGroup(),
                        s.getName(),
                        s.isEphemeral(),
                        ip.getIp(),
                        ip.getPort(),
                        weight,
                        ip.isHealthy(),
                        enabled,
                        cluster,
                        ip.getExtendDatum());
                })
                .collect(Collectors.toSet()));
    }
    
    /** 计算客户端 MD5 校验和。 */
    /** 对唯一字符串做 MD5 十六进制摘要。 */
    public static String checksum(Client client) {
        String s = buildUniqueString(client);
        if (s == null) {
            return "0";
        }
        return MD5Utils.md5Hex(s, Constants.ENCODE);
    }
    
    /** 按排序后的服务键拼接实例发布详情，生成唯一字符串。 */
    /** 构建用于校验的客户端唯一描述串。 */
    public static String buildUniqueString(Client client) {
        if (!(client instanceof IpPortBasedClient)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(client.getClientId()).append('|');
        client.getAllPublishedService().stream()
            .sorted(Comparator.comparing(DistroUtils::serviceKey))
            .forEach(s -> {
                InstancePublishInfo ip = client.getInstancePublishInfo(s);
                double weight = getWeight(ip);
                Boolean enabled = getEnabled(ip);
                String cluster = StringUtils.defaultIfBlank(ip.getCluster(), DEFAULT_CLUSTER_NAME);
                sb.append(serviceKey(s)).append('_')
                    .append(ip.getIp()).append(':').append(ip.getPort()).append('_')
                    .append(weight).append('_')
                    .append(ip.isHealthy()).append('_')
                    .append(enabled).append('_')
                    .append(cluster).append('_')
                    .append(convertMap2String(ip.getExtendDatum()))
                    .append(',');
            });
        return sb.toString();
    }
    
    /** 从 extendDatum 读取实例 enabled，缺省 true。 */
    private static boolean getEnabled(InstancePublishInfo ip) {
        Object enabled0 = ip.getExtendDatum().get(PUBLISH_INSTANCE_ENABLE);
        if (!(enabled0 instanceof Boolean)) {
            return true;
        } else {
            return (Boolean) enabled0;
        }
    }
    
    /** 从 extendDatum 读取实例 weight，缺省默认权重。 */
    private static double getWeight(InstancePublishInfo ip) {
        Object weight0 = ip.getExtendDatum().get(PUBLISH_INSTANCE_WEIGHT);
        if (!(weight0 instanceof Number)) {
            return DEFAULT_INSTANCE_WEIGHT;
        } else {
            return ((Number) weight0).doubleValue();
        }
    }
    
    /**
     * 将 Map 按 key 排序后转为 key:value 逗号分隔串。
     *
     * @param map 待转换的扩展元数据
     * @return KV 字符串
     */
    private static String convertMap2String(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return StringUtils.EMPTY;
        }
        StringBuilder sb = new StringBuilder();
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            sb.append(key);
            sb.append(':');
            sb.append(map.get(key));
            sb.append(',');
        }
        return sb.toString();
    }
    
}
