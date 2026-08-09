/*
 *  Copyright 1999-2021 Alibaba Group Holding Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alibaba.csp.sentinel.metric.exporter.jmx;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.metric.MetricNode;
import com.alibaba.csp.sentinel.util.StringUtil;

import javax.management.ObjectName;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 指标 MBean 写入器：通过 {@link MetricBeanWriter#write} 注册或更新 {@link MBeanRegistry} 中的 {@link MetricBean}。
 *
 * @author chenglu
 * @date 2021-07-01 20:02
 * @since 1.8.3
 */
public class MetricBeanWriter {
    
    private final MBeanRegistry mBeanRegistry = MBeanRegistry.getInstance();
    
    private static final String DEFAULT_APP_NAME = "sentinel-application";

    private static final Pattern SPECIAL_CHARACTER_PATTERN = Pattern.compile("[*?=:\"\n]");
    
    /**
     * 将按资源分组的 {@link MetricNode} 写入对应 {@link MetricBean}。
     * 未注册则创建并注册；已存在则更新数值。
     * 若 map 为空则重置全部已注册 MBean。
     * @param map metricNode value group by resource
     * @throws Exception write failed exception
     */
    public synchronized void write(Map<String, MetricNode> map) throws Exception {
        if (map == null || map.isEmpty()) {
            List<MetricBean> metricNodes = mBeanRegistry.listAllMBeans();
            if (metricNodes == null || metricNodes.isEmpty()) {
                return;
            }
            for (MetricBean metricNode : metricNodes) {
                metricNode.reset();
            }
            return;
        }
        String appName = SentinelConfig.getAppName();
        if (appName == null) {
            appName = DEFAULT_APP_NAME;
        }
        long version = System.currentTimeMillis();
        // 注册或更新本轮指标值
        for (MetricNode metricNode : map.values()) {
            // 转义资源名中的 JMX 特殊字符，见 issue #2989
            // 未转义会导致 ObjectName 非法
            String resourceName = escapeSpecialCharacter(metricNode.getResource());
            final String mBeanName = "Sentinel:type=Metric,resource=" + resourceName
                    +",classification=" + metricNode.getClassification()
                    +",appName=" + appName;
            MetricBean metricBean = mBeanRegistry.findMBean(mBeanName);
            if (metricBean != null) {
                metricBean.setValueFromNode(metricNode);
                metricBean.setVersion(version);
            } else {
                metricBean = new MetricBean();
                metricBean.setValueFromNode(metricNode);
                metricBean.setVersion(version);
                mBeanRegistry.register(metricBean, mBeanName);
                RecordLog.info("[MetricBeanWriter] Registering with JMX as Metric MBean [{}]", mBeanName);
            }
        }
        // 重置/注销本轮未更新的旧 MBean
        List<MetricBean> metricBeans = mBeanRegistry.listAllMBeans();
        if (metricBeans == null || metricBeans.isEmpty()) {
            return;
        }
        for (MetricBean metricBean : metricBeans) {
            if (!Objects.equals(metricBean.getVersion(), version)) {
                metricBean.reset();
                mBeanRegistry.unRegister(metricBean);
            }
        }
    }

    /**
     * 仅当资源名含 JMX 特殊字符（*、?、=、:、"、换行等）时进行转义 eg.(*,?,\n,\")
     *
     * @param resourceName need escape resource name
     * @return escaped characters
     */
    public static String escapeSpecialCharacter(String resourceName) {
        if (StringUtil.isBlank(resourceName) || !SPECIAL_CHARACTER_PATTERN.matcher(resourceName).find()) {
            return resourceName;
        }
        return ObjectName.quote(resourceName);
    }
}
