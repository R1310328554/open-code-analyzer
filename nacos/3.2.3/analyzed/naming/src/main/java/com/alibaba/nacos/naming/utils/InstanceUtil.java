/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.builder.InstanceBuilder;
import com.alibaba.nacos.api.naming.utils.NamingUtils;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.naming.constants.Constants;
import com.alibaba.nacos.naming.core.v2.metadata.InstanceMetadata;
import com.alibaba.nacos.naming.core.v2.pojo.InstancePublishInfo;
import com.alibaba.nacos.naming.core.v2.pojo.Service;
import com.alibaba.nacos.naming.misc.UtilsAndCommons;
import com.alibaba.nacos.naming.model.form.InstanceForm;
import com.alibaba.nacos.naming.pojo.instance.InstanceIdGeneratorManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 实例对象转换与辅助工具类。
 *
 * <p>负责 {@link InstancePublishInfo} 与 API {@link Instance} 互转、元数据合并、深拷贝及实例 ID 生成等常用操作。</p>
 *
 * @author xiweng.yy
 */
public final class InstanceUtil {
    
    /**
     * 将内部 {@link InstancePublishInfo} 转为 API {@link Instance}。
     *
     * @param service 所属服务
     * @param instanceInfo 内部发布信息
     * @return API 实例对象
     */
    /** 映射 IP、端口、集群、健康状态及 extendDatum 元数据。 */
    public static Instance parseToApiInstance(Service service, InstancePublishInfo instanceInfo) {
        Instance result = new Instance();
        result.setIp(instanceInfo.getIp());
        result.setPort(instanceInfo.getPort());
        result.setServiceName(NamingUtils.getGroupedName(service.getName(), service.getGroup()));
        result.setClusterName(instanceInfo.getCluster());
        Map<String, String> instanceMetadata = new HashMap<>(instanceInfo.getExtendDatum().size());
        for (Map.Entry<String, Object> entry : instanceInfo.getExtendDatum().entrySet()) {
            switch (entry.getKey()) {
                case Constants.CUSTOM_INSTANCE_ID:
                    result.setInstanceId(entry.getValue().toString());
                    break;
                case Constants.PUBLISH_INSTANCE_ENABLE:
                    result.setEnabled((boolean) entry.getValue());
                    break;
                case Constants.PUBLISH_INSTANCE_WEIGHT:
                    result.setWeight((Double) entry.getValue());
                    break;
                default:
                    instanceMetadata.put(entry.getKey(),
                        null != entry.getValue() ? entry.getValue().toString() : null);
            }
        }
        result.setMetadata(instanceMetadata);
        result.setEphemeral(service.isEphemeral());
        result.setHealthy(instanceInfo.isHealthy());
        return result;
    }
    
    /**
     * 用 {@link InstanceMetadata} 更新实例 enabled、weight 与扩展元数据。
     *
     * @param instance 待更新实例
     * @param metadata 实例元数据
     */
    /** 合并元数据到 API 实例对象。 */
    public static void updateInstanceMetadata(Instance instance, InstanceMetadata metadata) {
        instance.setEnabled(metadata.isEnabled());
        instance.setWeight(metadata.getWeight());
        for (Map.Entry<String, Object> entry : metadata.getExtendData().entrySet()) {
            instance.getMetadata().put(entry.getKey(), entry.getValue().toString());
        }
    }
    
    /**
     * 深拷贝实例，metadata 使用新 Map。
     *
     * @param source 源实例
     */
    /** 复制实例全部字段与 metadata。 */
    public static Instance deepCopy(Instance source) {
        Instance target = new Instance();
        target.setInstanceId(source.getInstanceId());
        target.setIp(source.getIp());
        target.setPort(source.getPort());
        target.setWeight(source.getWeight());
        target.setHealthy(source.isHealthy());
        target.setEnabled(source.isEnabled());
        target.setEphemeral(source.isEphemeral());
        target.setClusterName(source.getClusterName());
        target.setServiceName(source.getServiceName());
        target.setMetadata(new HashMap<>(source.getMetadata()));
        return target;
    }
    
    /**
     * 实例 ID 为空时用默认生成器补全，并回填 serviceName。
     *
     * @param instance 请求中的实例
     * @param groupedServiceName 服务的 grouped 名称
     */
    /** 调用 InstanceIdGeneratorManager 生成缺失的 instanceId。 */
    public static void setInstanceIdIfEmpty(Instance instance, String groupedServiceName) {
        if (null != instance && StringUtils.isEmpty(instance.getInstanceId())) {
            if (StringUtils.isBlank(instance.getServiceName())) {
                instance.setServiceName(groupedServiceName);
            }
            instance.setInstanceId(InstanceIdGeneratorManager.generateInstanceId(instance));
        }
    }
    
    /**
     * 批量为实例列表补全缺失的 instanceId。
     *
     * @param instances 实例列表
     * @param groupedServiceName 服务的 grouped 名称
     */
    /** 遍历列表调用 setInstanceIdIfEmpty。 */
    public static void batchSetInstanceIdIfEmpty(List<Instance> instances,
        String groupedServiceName) {
        if (null != instances) {
            for (Instance instance : instances) {
                setInstanceIdIfEmpty(instance, groupedServiceName);
            }
        }
    }
    
    /**
     * 从 HTTP {@link InstanceForm} 构建 {@link Instance}。
     *
     * @param instanceForm 请求表单
     * @param defaultEphemeral ephemeral 缺省值
     * @return 新实例
     * @throws NacosException 解析 metadata 失败时抛出
     */
    /** 解析表单字段并设置 ephemeral 默认值。 */
    public static Instance buildInstance(InstanceForm instanceForm, boolean defaultEphemeral)
        throws NacosException {
        String groupedServiceName =
            NamingUtils.getGroupedName(instanceForm.getServiceName(), instanceForm.getGroupName());
        Instance instance = InstanceBuilder.newBuilder().setServiceName(groupedServiceName)
            .setIp(instanceForm.getIp()).setClusterName(instanceForm.getClusterName())
            .setPort(instanceForm.getPort()).setHealthy(instanceForm.getHealthy())
            .setWeight(instanceForm.getWeight()).setEnabled(instanceForm.getEnabled())
            .setMetadata(UtilsAndCommons.parseMetadata(instanceForm.getMetadata()))
            .setEphemeral(instanceForm.getEphemeral()).build();
        if (instanceForm.getEphemeral() == null) {
            instance.setEphemeral(defaultEphemeral);
        }
        return instance;
    }
}
