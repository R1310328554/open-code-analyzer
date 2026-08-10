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

package com.alibaba.nacos.client.naming.cache;

import com.alibaba.nacos.api.common.Constants;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import com.alibaba.nacos.client.utils.ConcurrentDiskUtil;
import com.alibaba.nacos.common.utils.CollectionUtils;
import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.common.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alibaba.nacos.client.utils.LogUtils.NAMING_LOGGER;

/**
 * 命名服务磁盘缓存读写工具。
 *
 * <p>将 {@link ServiceInfo} 序列化为 JSON 写入本地文件，支持启动时从缓存目录恢复服务实例列表。</p>
 *
 * @author xuanyin
 */
public class DiskCache {
    
    /**
     * 将服务信息写入指定缓存目录。
     *
     * @param dom 服务信息
     * @param dir 目标目录
     */
    public static void write(ServiceInfo dom, String dir) {
        writeWithResult(dom, dir);
    }
    
    static boolean writeWithResult(ServiceInfo dom, String dir) {
        
        try {
            makeSureCacheDirExists(dir);
            
            File file = new File(dir, dom.getKeyEncoded());
            createFileIfAbsent(file, false);
            
            StringBuilder keyContentBuffer = new StringBuilder();
            
            String json = dom.getJsonFromServer();
            
            if (StringUtils.isEmpty(json)) {
                json = JacksonUtils.toJson(dom);
            }
            
            keyContentBuffer.append(json);
            
            // 使用并发磁盘 API 保证读写一致性
            ConcurrentDiskUtil.writeFileContent(file, keyContentBuffer.toString(),
                Charset.defaultCharset().toString());
            return true;
        } catch (Throwable e) {
            NAMING_LOGGER.error("[NA] failed to write cache for dom:" + dom.getName(), e);
            return false;
        }
    }
    
    public static String getLineSeparator() {
        return System.getProperty("line.separator");
    }
    
    /**
     * 从磁盘缓存目录批量读取服务信息。
     *
     * @param cacheDir 缓存目录
     * @return groupKey 到 ServiceInfo 的映射
     */
    public static Map<String, ServiceInfo> read(String cacheDir) {
        Map<String, ServiceInfo> domMap = new HashMap<>(16);
        try {
            File[] files = makeSureCacheDirExists(cacheDir).listFiles();
            if (files == null || files.length == 0) {
                return domMap;
            }
            
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                domMap.putAll(parseServiceInfoFromCache(file));
            }
        } catch (Throwable e) {
            NAMING_LOGGER.error("[NA] failed to read cache file", e);
        }
        
        return domMap;
    }
    
    /**
     * 从单个缓存或容灾文件解析服务信息。
     *
     * @param file 缓存/容灾文件
     * @return 解析出的服务映射
     * @throws UnsupportedEncodingException 文件名非 UTF-8 编码时抛出
     */
    public static Map<String, ServiceInfo> parseServiceInfoFromCache(File file)
        throws UnsupportedEncodingException {
        Map<String, ServiceInfo> result = new HashMap<>(1);
        String fileName = URLDecoder.decode(file.getName(), "UTF-8");
        if (!(fileName.endsWith(Constants.SERVICE_INFO_SPLITER + "meta") || fileName
            .endsWith(Constants.SERVICE_INFO_SPLITER + "special-url"))) {
            ServiceInfo dom = new ServiceInfo(fileName);
            List<Instance> ips = new ArrayList<>();
            dom.setHosts(ips);
            ServiceInfo newFormat = null;
            try (BufferedReader reader = new BufferedReader(
                new StringReader(ConcurrentDiskUtil.getFileContent(file,
                    Charset.defaultCharset().toString())))) {
                
                String json;
                while ((json = reader.readLine()) != null) {
                    try {
                        if (!json.startsWith("{")) {
                            continue;
                        }
                        
                        newFormat = JacksonUtils.toObj(json, ServiceInfo.class);
                        
                        if (StringUtils.isEmpty(newFormat.getName())) {
                            ips.add(JacksonUtils.toObj(json, Instance.class));
                        }
                    } catch (Throwable e) {
                        NAMING_LOGGER.error("[NA] error while parsing cache file: " + json, e);
                    }
                }
            } catch (Exception e) {
                NAMING_LOGGER.error("[NA] failed to read cache for dom: " + file.getName(), e);
            }
            if (newFormat != null && !StringUtils.isEmpty(newFormat.getName()) && !CollectionUtils
                .isEmpty(newFormat.getHosts())) {
                result.put(dom.getKey(), newFormat);
            } else if (!CollectionUtils.isEmpty(dom.getHosts())) {
                result.put(dom.getKey(), dom);
            }
        }
        return result;
    }
    
    /**
     * 若不存在则创建文件或目录。
     *
     * @param file  目标文件
     * @param isDir 是否为目录
     * @throws IOException 创建失败时抛出
     */
    public static void createFileIfAbsent(File file, boolean isDir) throws IOException {
        if (file.exists()) {
            return;
        }
        boolean createResult = isDir ? file.mkdirs() : file.createNewFile();
        if (!createResult && !file.exists()) {
            throw new IllegalStateException(
                "failed to create cache : " + (isDir ? "dir" : file) + file.getPath());
        }
    }
    
    /** 确保缓存目录存在并返回 File 引用。 */
    private static File makeSureCacheDirExists(String dir) throws IOException {
        File cacheDir = new File(dir);
        createFileIfAbsent(cacheDir, true);
        return cacheDir;
    }
}
