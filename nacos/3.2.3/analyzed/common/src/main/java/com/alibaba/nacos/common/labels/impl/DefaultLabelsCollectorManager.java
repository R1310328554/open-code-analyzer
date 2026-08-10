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

package com.alibaba.nacos.common.labels.impl;

import com.alibaba.nacos.common.labels.LabelsCollector;
import com.alibaba.nacos.common.labels.LabelsCollectorManager;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

/**
 * DefaultLabelsCollectorManager.
 * <p>默认标签收集器管理器：通过 {@link ServiceLoader} 加载所有 {@link LabelsCollector} 实现，按 order 降序依次收集并校验标签键值。</p>
 *
 * @author rong
 */
public class DefaultLabelsCollectorManager implements LabelsCollectorManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger("com.alibaba.nacos.common.labels");
    
    /** 已加载并按 order 排序的标签收集器列表 */
    private ArrayList<LabelsCollector> labelsCollectorsList = new ArrayList<>();
    
    /** 构造时自动 SPI 加载并排序所有 {@link LabelsCollector} */
    public DefaultLabelsCollectorManager() {
        labelsCollectorsList = loadLabelsCollectors();
    }
    
    @Override
    public Map<String, String> getLabels(Properties properties) {
        LOGGER.info("DefaultLabelsCollectorManager get labels.....");
        Map<String, String> labels = getLabels(labelsCollectorsList, properties);
        LOGGER.info("DefaultLabelsCollectorManager get labels finished,labels :{}", labels);
        return labels;
    }
    
    Map<String, String> getLabels(ArrayList<LabelsCollector> labelsCollectorsList,
        Properties properties) {
        
        // 允许调用方传入 null，内部使用空 Properties
        if (properties == null) {
            properties = new Properties();
        }
        Map<String, String> labels = new HashMap<>(8);
        for (LabelsCollector labelsCollector : labelsCollectorsList) {
            
            LOGGER.info("Process LabelsCollector with [name:{}]", labelsCollector.getName());
            for (Map.Entry<String, String> entry : labelsCollector.collectLabels(properties)
                .entrySet()) {
                // 键值须非空、长度≤128 且仅含字母数字及 _-. 
                if (!checkValidLabel(entry.getKey(), entry.getValue())) {
                    LOGGER.info(
                        " ignore invalid label with [key:{}, value:{}] of collector [name:{}]",
                        entry.getKey(),
                        entry.getValue(), labelsCollector.getName());
                    continue;
                }
                // putIfAbsent：先注册的收集器同名标签优先保留
                if (innerAddLabel(labels, entry.getKey(), entry.getValue())) {
                    LOGGER.info("pick label with [key:{}, value:{}] of collector [name:{}]",
                        entry.getKey(),
                        entry.getValue(), labelsCollector.getName());
                } else {
                    LOGGER.info(" ignore label with [key:{}, value:{}] of collector [name:{}],"
                        + "already existed in LabelsCollectorManager with previous [value:{}]，",
                        entry.getKey(),
                        entry.getValue(), labelsCollector.getName(), labels.get(entry.getKey()));
                }
            }
        }
        return labels;
    }
    
    private boolean checkValidLabel(String key, String value) {
        return isValid(key) && isValid(value);
    }
    
    private static boolean isValid(String param) {
        if (StringUtils.isBlank(param)) {
            return false;
        }
        int length = param.length();
        if (length > maxLength) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            char ch = param.charAt(i);
            if (!Character.isLetterOrDigit(ch) && !isValidChar(ch)) {
                return false;
            }
        }
        return true;
    }
    
    /** 标签键值允许的额外特殊字符 */
    private static char[] validChars = new char[] {'_', '-', '.'};
    
    /** 单个标签键或值的最大长度 */
    private static int maxLength = 128;
    
    private static boolean isValidChar(char ch) {
        for (char c : validChars) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }
    
    private ArrayList<LabelsCollector> loadLabelsCollectors() {
        ServiceLoader<LabelsCollector> labelsCollectors = ServiceLoader.load(LabelsCollector.class);
        ArrayList<LabelsCollector> labelsCollectorsList = new ArrayList<>();
        for (LabelsCollector labelsCollector : labelsCollectors) {
            labelsCollectorsList.add(labelsCollector);
        }
        // order 越大越先执行，高优先级收集器的标签更易被保留
        labelsCollectorsList.sort((o1, o2) -> o2.getOrder() - o1.getOrder());
        return labelsCollectorsList;
    }
    
    private boolean innerAddLabel(Map<String, String> labels, String key, String value) {
        // 返回 true 表示首次插入该 key
        return null == labels.putIfAbsent(key, value);
    }
}
