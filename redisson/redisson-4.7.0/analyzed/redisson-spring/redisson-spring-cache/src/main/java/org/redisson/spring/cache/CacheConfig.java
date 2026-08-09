/**
 * Copyright (c) 2013-2026 Nikita Koksharov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.redisson.spring.cache;

import org.redisson.api.EvictionMode;
import org.redisson.api.map.event.MapEntryListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Cache 集成用的 Redisson 缓存配置对象。
 * <p>支持 TTL、max-idle、容量上限与 {@link org.redisson.api.map.event.MapEntryListener}。
 * 可通过静态 {@link #fromYAML} 方法从 YAML 批量加载。
 *
 * @author Nikita Koksharov
 */
public class CacheConfig {

    static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    private long ttl;

    private long maxIdleTime;
    
    private int maxSize;

    private EvictionMode evictionMode = EvictionMode.LRU;

    private List<MapEntryListener> listeners = new ArrayList<>();

    /** 默认构造：{@code ttl=0}、{@code maxIdleTime=0}（条目永不过期）。 */
    /**
     * Creates config object with
     * <code>ttl = 0</code> and <code>maxIdleTime = 0</code>.
     *
     */
    public CacheConfig() {
    }

    /**
     * 指定 TTL 与 max-idle 构造配置。
     * @param ttl 条目存活时间（毫秒）；{@code 0} 表示不按 TTL 过期
     * @param maxIdleTime 最大空闲时间（毫秒）；与 ttl 均为 {@code 0} 时条目永久保留
     */
    public CacheConfig(long ttl, long maxIdleTime) {
        super();
        this.ttl = ttl;
        this.maxIdleTime = maxIdleTime;
    }

    public long getTTL() {
        return ttl;
    }

    /**
     * 设置条目 TTL（毫秒）。
     * @param ttl 存活时间；{@code 0} 表示 TTL 不参与过期
     */
    public void setTTL(long ttl) {
        this.ttl = ttl;
    }
    
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * 设置 Map 最大容量；超出时按 LRU 淘汰。
     * @param maxSize 上限；{@code 0} 表示无界（默认）
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public EvictionMode getEvictionMode() {
        return evictionMode;
    }

    /**
     * 设置淘汰算法（{@link org.redisson.api.EvictionMode#LRU} 或 LFU）。
     * @param evictionMode 淘汰模式
     * @return 当前实例（链式调用）
     */
    public CacheConfig setEvictionMode(EvictionMode evictionMode) {
        this.evictionMode = evictionMode;
        return this;
    }

    public long getMaxIdleTime() {
        return maxIdleTime;
    }

    /**
     * Set max idle time for key\value entry in milliseconds.
     *
     * @param maxIdleTime - max idle time for key\value entry in milliseconds.
     *              If <code>0</code> then max idle time doesn't affect entry expiration.
     */
    public void setMaxIdleTime(long maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    /**
     * 注册 Map 事件监听器（ttl/maxIdleTime/maxSize 任一非零时生效）。
     * <p>listener 可为 EntryCreated/Expired/Removed/Updated 等实现。
     * @param listener 监听器实例
     */
    public void addListener(MapEntryListener listener) {
        listeners.add(listener);
    }

    public void setListeners(List<MapEntryListener> listeners) {
        this.listeners = listeners;
    }

    public List<MapEntryListener> getListeners() {
        return listeners;
    }
    
    /**
     * 从 YAML 字符串解析缓存名 → {@link CacheConfig} 映射。
     * @param content YAML 文本
     * @return 配置映射
     * @throws IOException 解析失败
     */
    public static Map<String, ? extends CacheConfig> fromYAML(String content) throws IOException {
        return new CacheConfigSupport().fromYAML(content);
    }

    /**
     * Read config objects stored in YAML format from <code>InputStream</code>
     *
     * @param inputStream of config
     * @return config
     * @throws IOException  error
     */
    public static Map<String, ? extends CacheConfig> fromYAML(InputStream inputStream) throws IOException {
        return new CacheConfigSupport().fromYAML(inputStream);
    }

    /**
     * Read config objects stored in YAML format from <code>File</code>
     *
     * @param file of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends CacheConfig> fromYAML(File file) throws IOException {
        return new CacheConfigSupport().fromYAML(file);
    }

    /**
     * Read config objects stored in YAML format from <code>URL</code>
     *
     * @param url of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends CacheConfig> fromYAML(URL url) throws IOException {
        return new CacheConfigSupport().fromYAML(url);
    }

    /**
     * Read config objects stored in YAML format from <code>Reader</code>
     *
     * @param reader of config
     * @return config
     * @throws IOException error
     */
    public static Map<String, ? extends CacheConfig> fromYAML(Reader reader) throws IOException {
        return new CacheConfigSupport().fromYAML(reader);
    }

    /**
     * 将配置映射序列化为 YAML 字符串。
     * @param config 缓存名 → 配置
     * @return YAML 文本
     * @throws IOException 序列化失败
     */
    public static String toYAML(Map<String, ? extends CacheConfig> config) throws IOException {
        return new CacheConfigSupport().toYAML(config);
    }

    @Override
    public String toString() {
        return "CacheConfig{" +
                "ttl=" + ttl +
                ", maxIdleTime=" + maxIdleTime +
                ", maxSize=" + maxSize +
                ", evictionMode=" + evictionMode +
                ", listeners=" + listeners +
                '}';
    }
}
