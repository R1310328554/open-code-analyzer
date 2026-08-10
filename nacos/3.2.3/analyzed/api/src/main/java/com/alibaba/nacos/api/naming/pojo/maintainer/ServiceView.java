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

package com.alibaba.nacos.api.naming.pojo.maintainer;

/**
 * 服务列表视图项，用于运维控制台展示服务概要统计。
 *
 * @author nkorange
 */
public class ServiceView {
    
    /** 服务名。 */
    private String name;
    
    /** 分组名。 */
    private String groupName;
    
    /** 集群数量。 */
    private int clusterCount;
    
    /** 实例 IP 总数。 */
    private int ipCount;
    
    /** 健康实例数量。 */
    private int healthyInstanceCount;
    
    /** 触发标记（如同步/推送状态标识）。 */
    private String triggerFlag;
    
    /** 获取服务名。 */
    public String getName() {
        return name;
    }
    
    /** 设置服务名。 */
    public void setName(String name) {
        this.name = name;
    }
    
    /** 获取分组名。 */
    public String getGroupName() {
        return groupName;
    }
    
    /** 设置分组名。 */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
    
    /** 获取集群数。 */
    public int getClusterCount() {
        return clusterCount;
    }
    
    /** 设置集群数。 */
    public void setClusterCount(int clusterCount) {
        this.clusterCount = clusterCount;
    }
    
    /** 获取实例 IP 总数。 */
    public int getIpCount() {
        return ipCount;
    }
    
    /** 设置实例 IP 总数。 */
    public void setIpCount(int ipCount) {
        this.ipCount = ipCount;
    }
    
    /** 获取健康实例数。 */
    public int getHealthyInstanceCount() {
        return healthyInstanceCount;
    }
    
    /** 设置健康实例数。 */
    public void setHealthyInstanceCount(int healthyInstanceCount) {
        this.healthyInstanceCount = healthyInstanceCount;
    }
    
    /** 获取触发标记。 */
    public String getTriggerFlag() {
        return triggerFlag;
    }
    
    /** 设置触发标记。 */
    public void setTriggerFlag(String triggerFlag) {
        this.triggerFlag = triggerFlag;
    }
    
    /** 返回包含各统计字段的字符串表示。 */
    @Override
    public String toString() {
        return "ServiceView{" + "name='" + name + '\'' + ", groupName='" + groupName + '\''
            + ", clusterCount="
            + clusterCount + ", ipCount=" + ipCount + ", healthyInstanceCount="
            + healthyInstanceCount
            + ", triggerFlag='" + triggerFlag + '\'' + '}';
    }
}
