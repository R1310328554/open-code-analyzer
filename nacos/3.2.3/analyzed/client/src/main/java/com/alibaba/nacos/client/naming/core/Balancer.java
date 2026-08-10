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

package com.alibaba.nacos.client.naming.core;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.utils.Chooser;
import com.alibaba.nacos.client.naming.utils.Pair;
import com.alibaba.nacos.common.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 命名服务实例负载均衡工具。
 *
 * <p>提供按权重随机选取健康实例的能力，供 {@link com.alibaba.nacos.client.naming.NacosNamingService} 选择调用目标。</p>
 *
 * @author xuanyin
 */
public class Balancer {
    
    /** 按实例权重随机选择的负载均衡策略。 */
    public static class RandomByWeight {
        
        /**
         * 返回服务下全部实例列表。
         *
         * @param serviceInfo 服务信息
         * @return 实例列表，无主机时抛异常
         */
        public static List<Instance> selectAll(ServiceInfo serviceInfo) {
            List<Instance> hosts = serviceInfo.getHosts();
            if (CollectionUtils.isEmpty(hosts)) {
                throw new IllegalStateException(
                    "no host to srv for serviceInfo: " + serviceInfo.getName());
            }
            return hosts;
        }
        
        /**
         * 从服务实例中按权重随机选取一个。
         *
         * @param dom 服务信息
         * @return 随机选中的实例
         */
        public static Instance selectHost(ServiceInfo dom) {
            return getHostByRandomWeight(selectAll(dom));
        }
    }
    
    /**
     * 从主机列表中按权重随机返回一个健康实例。
     *
     * @param hosts 实例列表
     * @return 按权重随机选中的实例，无可用实例时返回 null
     */
    protected static Instance getHostByRandomWeight(List<Instance> hosts) {
        NAMING_LOGGER.debug("entry randomWithWeight");
        if (hosts == null || hosts.size() == 0) {
            NAMING_LOGGER.debug("hosts == null || hosts.size() == 0");
            return null;
        }
        NAMING_LOGGER.debug("new Chooser");
        List<Pair<Instance>> hostsWithWeight = new ArrayList<>();
        for (Instance host : hosts) {
            if (host.isHealthy()) {
                hostsWithWeight.add(new Pair<Instance>(host, host.getWeight()));
            }
        }
        NAMING_LOGGER.debug("for (Host host : hosts)");
        Chooser<String, Instance> vipChooser = new Chooser<>("www.taobao.com");
        vipChooser.refresh(hostsWithWeight);
        NAMING_LOGGER.debug("vipChooser.refresh");
        return vipChooser.randomWithWeight();
    }
}
