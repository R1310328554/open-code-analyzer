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

package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.common.utils.IoUtils;
import com.alibaba.nacos.config.server.utils.LogUtil;

import com.alibaba.nacos.common.utils.StringUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import static com.alibaba.nacos.config.server.utils.LogUtil.FATAL_LOG;

/**
 * 运行时开关服务：从 meta 配置 {@link #SWITCH_META_DATA_ID} 解析 key=value 行，
 * 维护内存 switches 供长轮询延迟等模块热读取。
 * SwitchService.
 *
 * @author Nacos
 */
@Service
public class SwitchService {
    
    /** 开关元数据 dataId，由 DumpConfigHandler 加载触发 refresh。 */
    public static final String SWITCH_META_DATA_ID = "com.alibaba.nacos.meta.switch";
    
    /** 长轮询固定延迟毫秒数开关键名。 */
    public static final String FIXED_DELAY_TIME = "fixedDelayTime";
    
    private static volatile Map<String, String> switches = new HashMap<>();
    
    /**
     * 读取整型开关，解析失败或缺失时返回 defaultValue。
     *
     * @param key          开关键
     * @param defaultValue 默认值
     * @return 开关整数值
     */
    public static int getSwitchInteger(String key, int defaultValue) {
        int rtn;
        try {
            String status = switches.get(key);
            rtn = status != null ? Integer.parseInt(status) : defaultValue;
        } catch (Exception e) {
            rtn = defaultValue;
            LogUtil.FATAL_LOG.error("corrupt switch value {}={}", key, switches.get(key));
        }
        return rtn;
    }
    
    /**
     * Load config.
     *
     * @param config config content string value.
      * <p>运行时开关；详见类级说明。</p>
     */
    public static void load(String config) {
        if (StringUtils.isBlank(config)) {
            FATAL_LOG.warn("switch config is blank.");
            return;
        }
        FATAL_LOG.warn("[switch-config] {}", config);
        
        Map<String, String> map = new HashMap<>(30);
        try (StringReader reader = new StringReader(config)) {
            for (String line : IoUtils.readLines(reader)) {
                if (!StringUtils.isBlank(line) && !line.startsWith("#")) {
                    String[] array = line.split("=");
                    
                    if (array.length != 2) {
                        LogUtil.FATAL_LOG.error("corrupt switch record {}", line);
                        continue;
                    }
                    
                    String key = array[0].trim();
                    String value = array[1].trim();
                    
                    map.put(key, value);
                }
            }
            switches = map;
            FATAL_LOG.warn("[reload-switches] {}", getSwitches());
        } catch (IOException e) {
            LogUtil.FATAL_LOG.warn("[reload-switches] error! {}", config);
        }
    }
    
    /** 将全部开关序列化为 "key=value; ..." 便于日志输出。 */
    public static String getSwitches() {
        StringBuilder sb = new StringBuilder();
        
        String split = "";
        for (Map.Entry<String, String> entry : switches.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(split);
            sb.append(key);
            sb.append('=');
            sb.append(value);
            split = "; ";
        }
        
        return sb.toString();
    }
}
