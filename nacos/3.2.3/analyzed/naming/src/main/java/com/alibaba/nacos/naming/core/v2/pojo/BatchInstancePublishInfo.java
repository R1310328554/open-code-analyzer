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

package com.alibaba.nacos.naming.core.v2.pojo;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * 单服务多实例批量注册时的发布信息容器，继承 {@link InstancePublishInfo}。
 *
 * <p>客户端一次请求可向同一服务注册多个实例。</p>
 *
 * @author : ChenHao26
 * @ClassName: BatchInstancePublishInfo
 * @Date: 2022/4/21 16:19
 */
public class BatchInstancePublishInfo extends InstancePublishInfo {
    
    /** 客户端上报的全部实例发布信息列表。 */
    /**
     * save all the service instance data transmitted from the client.
      * <p>Nacos 命名 V2 元数据、POJO、客户端操作与健康检查；详见上方类/接口说明。</p>
     */
    private List<InstancePublishInfo> instancePublishInfos;
    
    public List<InstancePublishInfo> getInstancePublishInfos() {
        return instancePublishInfos;
    }
    
    public void setInstancePublishInfos(List<InstancePublishInfo> instancePublishInfos) {
        this.instancePublishInfos = instancePublishInfos;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BatchInstancePublishInfo)) {
            return false;
        }
        BatchInstancePublishInfo that = (BatchInstancePublishInfo) o;
        return CollectionUtils.isEqualCollection(this.getInstancePublishInfos(),
            that.getInstancePublishInfos());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(CollectionUtils.getCardinalityMap(instancePublishInfos));
    }
}
