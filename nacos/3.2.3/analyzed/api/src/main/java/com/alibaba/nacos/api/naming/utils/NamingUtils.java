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

package com.alibaba.nacos.api.naming.utils;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.exception.api.NacosApiException;
import com.alibaba.nacos.api.model.v2.ErrorCode;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.utils.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static com.alibaba.nacos.api.common.Constants.CLUSTER_NAME_PATTERN_STRING;
import static com.alibaba.nacos.api.common.Constants.DEFAULT_NAMESPACE_ID;
import static com.alibaba.nacos.api.common.Constants.NUMBER_PATTERN_STRING;

/**
 * 命名服务工具类。
 *
 * <p>提供服务名/分组拼接与解析、实例合法性校验等静态方法，贯穿客户端与服务端命名 API。</p>
 *
 * @author nkorange
 * @since 1.0.0
 */
public class NamingUtils {
    
    /** 集群名称合法字符正则。 */
    private static final Pattern CLUSTER_NAME_PATTERN =
        Pattern.compile(CLUSTER_NAME_PATTERN_STRING);
    
    /** 纯数字字符串正则。 */
    private static final Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_PATTERN_STRING);
    
    /**
     * 将服务名与分组名拼接为 {@code groupName@@serviceName} 格式。
     *
     * <p>多数场景下 serviceName 不可为空；若需宽松拼接，参见 {@link
     * com.alibaba.nacos.api.naming.utils.NamingUtils#getGroupedNameOptional(String, String)}。</p>
     *
     * <p>示例：</p>
     * <p>serviceName | groupName | result</p>
     * <p>serviceA    | groupA    | groupA@@serviceA</p>
     * <p>nil         | groupA    | 抛出 IllegalArgumentException</p>
     *
     * @return {@code groupName@@serviceName}
     */
    public static String getGroupedName(final String serviceName, final String groupName) {
        if (StringUtils.isBlank(serviceName)) {
            throw new IllegalArgumentException(
                "Param 'serviceName' is illegal, serviceName is blank");
        }
        if (StringUtils.isBlank(groupName)) {
            throw new IllegalArgumentException("Param 'groupName' is illegal, groupName is blank");
        }
        final String resultGroupedName = groupName + Constants.SERVICE_INFO_SPLITER + serviceName;
        // 复用 intern 减少重复字符串占用
        return resultGroupedName.intern();
    }
    
    /** 拼接命名空间、分组与服务名，构成全局唯一服务键。 */
    public static String getServiceKey(String namespace, String group, String serviceName) {
        if (StringUtils.isBlank(namespace)) {
            namespace = DEFAULT_NAMESPACE_ID;
        }
        return namespace + Constants.SERVICE_INFO_SPLITER + group + Constants.SERVICE_INFO_SPLITER
            + serviceName;
    }
    
    /**
     * 解析服务键为 [namespace, group, serviceName] 三段。
     *
     * @param serviceKey 服务键
     * @return 解析后的字符串数组
     */
    public static String[] parseServiceKey(String serviceKey) {
        return serviceKey.split(Constants.SERVICE_INFO_SPLITER);
    }
    
    /** 从 {@code group@@service} 格式字符串中提取服务名。 */
    public static String getServiceName(final String serviceNameWithGroup) {
        if (StringUtils.isBlank(serviceNameWithGroup)) {
            return StringUtils.EMPTY;
        }
        if (!serviceNameWithGroup.contains(Constants.SERVICE_INFO_SPLITER)) {
            return serviceNameWithGroup;
        }
        return serviceNameWithGroup.split(Constants.SERVICE_INFO_SPLITER)[1];
    }
    
    /** 从 {@code group@@service} 格式字符串中提取分组名；无分隔符时返回默认分组。 */
    public static String getGroupName(final String serviceNameWithGroup) {
        if (StringUtils.isBlank(serviceNameWithGroup)) {
            return StringUtils.EMPTY;
        }
        if (!serviceNameWithGroup.contains(Constants.SERVICE_INFO_SPLITER)) {
            return Constants.DEFAULT_GROUP;
        }
        return serviceNameWithGroup.split(Constants.SERVICE_INFO_SPLITER)[0];
    }
    
    /**
     * 判断服务名是否为兼容模式（含 {@code @@} 分隔符）。
     *
     * @param serviceName 服务名
     * @return 若为兼容模式格式则返回 true
     */
    public static boolean isServiceNameCompatibilityMode(final String serviceName) {
        return !StringUtils.isBlank(serviceName)
            && serviceName.contains(Constants.SERVICE_INFO_SPLITER);
    }
    
    /**
     * 校验组合服务名格式，serviceName 不可为空。
     * <pre>
     * serviceName = "@@";                 长度 = 0；非法
     * serviceName = "group@@";            长度 = 1；非法
     * serviceName = "@@serviceName";      长度 = 2；非法
     * serviceName = "group@@serviceName"; 长度 = 2；合法
     * </pre>
     *
     * @param combineServiceName 组合服务名，如 groupName@@serviceName
     */
    public static void checkServiceNameFormat(String combineServiceName) {
        String[] split = combineServiceName.split(Constants.SERVICE_INFO_SPLITER);
        if (split.length <= 1) {
            throw new IllegalArgumentException(
                "Param 'serviceName' is illegal, it should be format as 'groupName@@serviceName'");
        }
        if (split[0].isEmpty()) {
            throw new IllegalArgumentException(
                "Param 'serviceName' is illegal, groupName can't be empty");
        }
    }
    
    /**
     * 将服务名与分组名拼接，不做参数校验。
     *
     * <p>行为类似 {@link com.alibaba.nacos.api.naming.utils.NamingUtils#getGroupedName}，但不验证参数合法性。</p>
     *
     * <p>示例：</p>
     * <p>serviceName | groupName | result</p>
     * <p>serviceA    | groupA    | groupA@@serviceA</p>
     * <p>nil         | groupA    | groupA@@</p>
     * <p>nil         | nil       | @@</p>
     *
     * @return {@code groupName@@serviceName}
     */
    public static String getGroupedNameOptional(final String serviceName, final String groupName) {
        return groupName + Constants.SERVICE_INFO_SPLITER + serviceName;
    }
    
    /**
     * 校验实例保活相关参数是否合法。
     *
     * <pre>
     * 心跳超时必须 &gt; 心跳间隔
     * IP 删除超时必须 &gt; 心跳间隔
     * </pre>
     *
     * @param instance 待校验的实例
     * @throws NacosException 校验失败时抛出
     */
    public static void checkInstanceIsLegal(Instance instance) throws NacosException {
        if (null == instance) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.INSTANCE_ERROR,
                "Instance can not be null.");
        }
        instance.validate();
        if (instance.getInstanceHeartBeatTimeOut() < instance.getInstanceHeartBeatInterval()
            || instance.getIpDeleteTimeout() < instance.getInstanceHeartBeatInterval()) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.INSTANCE_ERROR,
                "Instance 'heart beat interval' must less than 'heart beat timeout' and 'ip delete timeout'.");
        }
        if (!StringUtils.isEmpty(instance.getClusterName())
            && !CLUSTER_NAME_PATTERN.matcher(instance.getClusterName())
                .matches()) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.INSTANCE_ERROR,
                String.format(
                    "Instance 'clusterName' should be characters with only 0-9a-zA-Z-. (current: %s)",
                    instance.getClusterName()));
        }
    }
    
    /**
     * 校验批量注册场景下实例必须为临时实例。
     *
     * @param instance 实例对象
     * @throws NacosException 非临时实例时抛出
     */
    public static void checkInstanceIsEphemeral(Instance instance) throws NacosException {
        if (!instance.isEphemeral()) {
            throw new NacosApiException(NacosException.INVALID_PARAM, ErrorCode.INSTANCE_ERROR,
                String.format(
                    "Batch registration does not allow persistent instance registration , Instance：%s",
                    instance));
        }
    }
    
    /**
     * 批量校验实例列表的合法性（去重后逐条校验）。
     *
     * @param instances 待注册实例列表
     * @throws NacosException 任一实例校验失败时抛出
     */
    public static void batchCheckInstanceIsLegal(List<Instance> instances) throws NacosException {
        Set<Instance> newInstanceSet = new HashSet<>(instances);
        for (Instance instance : newInstanceSet) {
            checkInstanceIsEphemeral(instance);
            checkInstanceIsLegal(instance);
        }
    }
    
    /**
     * 判断字符串是否为纯数字。
     *
     * @param str 待检测字符串
     * @return 若为数字字符串则返回 true
     */
    public static boolean isNumber(String str) {
        return !StringUtils.isEmpty(str) && NUMBER_PATTERN.matcher(str).matches();
    }
}
