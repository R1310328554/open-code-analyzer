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

package com.alibaba.nacos.client.naming.backups.datasource;

import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.naming.backups.FailoverData;
import com.alibaba.nacos.client.naming.backups.FailoverDataSource;
import com.alibaba.nacos.client.naming.backups.FailoverSwitch;
import com.alibaba.nacos.client.naming.backups.NamingFailoverData;
import com.alibaba.nacos.client.naming.cache.DiskCache;
import com.alibaba.nacos.client.naming.utils.CacheDirUtil;
import com.alibaba.nacos.client.naming.utils.UtilAndComs;
import com.alibaba.nacos.client.utils.ConcurrentDiskUtil;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 基于本地磁盘的命名容灾数据源。
 *
 * <p>从容灾目录读取开关文件与缓存服务文件，解析为 {@link NamingFailoverData} 映射。开关文件变更时触发重新加载。</p>
 *
 * @author zongkang.guo
 */
public class DiskFailoverDataSource implements FailoverDataSource {
    
    /** 容灾数据相对缓存根目录的子路径。 */
    private static final String FAILOVER_DIR = "/failover";
    
    /** 开关文件中表示开启容灾的行内容。 */
    private static final String IS_FAILOVER_MODE = "1";
    
    /** 开关文件中表示关闭容灾的行内容。 */
    private static final String NO_FAILOVER_MODE = "0";
    
    /** 内存中缓存的开关参数键名。 */
    private static final String FAILOVER_MODE_PARAM = "failover-mode";
    
    private static final FailoverSwitch FAILOVER_SWITCH_FALSE = new FailoverSwitch(Boolean.FALSE);
    
    private static final FailoverSwitch FAILOVER_SWITCH_TRUE = new FailoverSwitch(Boolean.TRUE);
    
    /** 容灾开关参数的内存缓存。 */
    private final Map<String, String> switchParams = new ConcurrentHashMap<>();
    
    /** 从磁盘加载的容灾服务数据映射。 */
    private Map<String, FailoverData> serviceMap = new ConcurrentHashMap<>();
    
    /** 容灾文件根目录绝对路径。 */
    private String failoverDir;
    
    /** 开关文件上次读取时的修改时间戳。 */
    private long lastModifiedMillis = 0L;
    
    public DiskFailoverDataSource() {
        failoverDir = CacheDirUtil.getCacheDir() + FAILOVER_DIR;
        switchParams.put(FAILOVER_MODE_PARAM, Boolean.FALSE.toString());
    }
    
    /** 扫描容灾目录并解析各服务缓存文件为容灾数据。 */
    class FailoverFileReader implements Runnable {
        
        @Override
        public void run() {
            Map<String, FailoverData> domMap = new HashMap<>(200);
            
            try {
                File cacheDir = new File(failoverDir);
                DiskCache.createFileIfAbsent(cacheDir, true);
                
                File[] files = cacheDir.listFiles();
                if (files == null) {
                    return;
                }
                
                for (File file : files) {
                    if (!file.isFile()) {
                        continue;
                    }
                    
                    if (file.getName().equals(UtilAndComs.FAILOVER_SWITCH)) {
                        continue;
                    }
                    
                    for (Map.Entry<String, ServiceInfo> entry : DiskCache
                        .parseServiceInfoFromCache(file).entrySet()) {
                        domMap.put(entry.getKey(),
                            NamingFailoverData.newNamingFailoverData(entry.getValue()));
                    }
                }
            } catch (Exception e) {
                NAMING_LOGGER.error("[NA] failed to read cache file", e);
            }
            
            if (domMap.size() > 0) {
                serviceMap = domMap;
            }
        }
    }
    
    @Override
    public FailoverSwitch getSwitch() {
        try {
            File switchFile = Paths.get(failoverDir, UtilAndComs.FAILOVER_SWITCH).toFile();
            if (!switchFile.exists()) {
                NAMING_LOGGER.debug("failover switch is not found, {}", switchFile.getName());
                switchParams.put(FAILOVER_MODE_PARAM, Boolean.FALSE.toString());
                return FAILOVER_SWITCH_FALSE;
            }
            
            long modified = switchFile.lastModified();
            
            if (lastModifiedMillis < modified) {
                lastModifiedMillis = modified;
                String failover = ConcurrentDiskUtil.getFileContent(switchFile.getPath(),
                    Charset.defaultCharset().toString());
                if (!StringUtils.isEmpty(failover)) {
                    String[] lines = failover.split(DiskCache.getLineSeparator());
                    
                    for (String line : lines) {
                        String line1 = line.trim();
                        if (IS_FAILOVER_MODE.equals(line1)) {
                            switchParams.put(FAILOVER_MODE_PARAM, Boolean.TRUE.toString());
                            NAMING_LOGGER.info("failover-mode is on");
                            new FailoverFileReader().run();
                            return FAILOVER_SWITCH_TRUE;
                        } else if (NO_FAILOVER_MODE.equals(line1)) {
                            switchParams.put(FAILOVER_MODE_PARAM, Boolean.FALSE.toString());
                            NAMING_LOGGER.info("failover-mode is off");
                            return FAILOVER_SWITCH_FALSE;
                        }
                    }
                }
            }
            return switchParams.get(FAILOVER_MODE_PARAM).equals(Boolean.TRUE.toString())
                ? FAILOVER_SWITCH_TRUE : FAILOVER_SWITCH_FALSE;
            
        } catch (Throwable e) {
            NAMING_LOGGER.error("[NA] failed to read failover switch.", e);
            switchParams.put(FAILOVER_MODE_PARAM, Boolean.FALSE.toString());
            return FAILOVER_SWITCH_FALSE;
        }
    }
    
    @Override
    public Map<String, FailoverData> getFailoverData() {
        if (Boolean.parseBoolean(switchParams.get(FAILOVER_MODE_PARAM))) {
            return serviceMap;
        }
        return new ConcurrentHashMap<>(0);
    }
}
