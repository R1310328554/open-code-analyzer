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

package com.alibaba.nacos.api.config;

import com.alibaba.nacos.api.annotation.Since;
import com.alibaba.nacos.api.config.filter.IConfigFilter;
import com.alibaba.nacos.api.config.listener.FuzzyWatchEventWatcher;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * Nacos 配置服务客户端接口。
 *
 * <p>提供配置拉取、发布、监听、模糊订阅及过滤器扩展等能力。</p>
 *
 * @author Nacos
 */
public interface ConfigService {
    
    /**
     * 获取指定 dataId 与 group 的配置内容。
     *
     * @param dataId    配置 Data ID
     * @param group     配置分组
     * @param timeoutMs 读取超时（毫秒）
     * @return 配置内容字符串
     * @throws NacosException 拉取失败时抛出
     */
    @Since("0.2.0")
    String getConfig(String dataId, String group, long timeoutMs) throws NacosException;
    
    /**
     * 获取配置内容及 MD5 等完整查询结果。
     *
     * <p>返回 {@link ConfigQueryResult}，可用于 CAS 发布等需内容摘要的场景。</p>
     *
     * @param dataId    配置 Data ID
     * @param group     配置分组
     * @param timeoutMs 读取超时（毫秒）
     * @return 含内容与 MD5 的查询结果
     * @throws NacosException 拉取失败时抛出
     * @since 3.2.0
     */
    @Since("3.2.0")
    default ConfigQueryResult getConfigWithResult(String dataId, String group, long timeoutMs)
        throws NacosException {
        // 默认实现仅返回内容，不含 MD5
        String content = getConfig(dataId, group, timeoutMs);
        return new ConfigQueryResult(content, null);
    }
    
    /**
     * 拉取配置并注册变更监听器。
     *
     * <p>首次启动时主动拉取配置，后续变更由 {@link Listener} 回调；
     * 推荐直接使用本接口，而非依赖 enableRemoteSyncConfig 参数。</p>
     *
     * @param dataId    配置 Data ID
     * @param group     配置分组
     * @param timeoutMs 读取超时（毫秒）
     * @param listener  配置变更监听器
     * @return 当前配置内容
     * @throws NacosException 拉取或注册失败时抛出
     */
    @Since("1.1.0")
    String getConfigAndSignListener(String dataId, String group, long timeoutMs, Listener listener)
        throws NacosException;
    
    /**
     * 为指定配置添加变更监听器。
     *
     * <p>服务端修改配置后客户端回调监听器；建议在监听器中提供独立线程池异步处理，
     * 避免阻塞其他配置的回调。</p>
     *
     * @param dataId   配置 Data ID
     * @param group    配置分组
     * @param listener 监听器实例
     * @throws NacosException 注册失败时抛出
     */
    @Since("0.2.0")
    void addListener(String dataId, String group, Listener listener) throws NacosException;
    
    /**
     * 发布配置到 Nacos 服务端。
     *
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param content 配置内容
     * @return 发布成功返回 {@code true}
     * @throws NacosException 发布失败时抛出
     */
    @Since("0.2.0")
    boolean publishConfig(String dataId, String group, String content) throws NacosException;
    
    /**
     * 发布指定类型的配置到 Nacos 服务端。
     *
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param content 配置内容
     * @param type    配置类型，参见 {@link ConfigType}
     * @return 发布成功返回 {@code true}
     * @throws NacosException 发布失败时抛出
     */
    @Since("1.4.1")
    boolean publishConfig(String dataId, String group, String content, String type)
        throws NacosException;
    
    /**
     * 基于 MD5 的 CAS 条件发布配置。
     *
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param content 新配置内容
     * @param casMd5  期望的当前内容 MD5，不匹配则发布失败
     * @return 发布成功返回 {@code true}
     * @throws NacosException 发布失败时抛出
     */
    @Since("2.0.0")
    boolean publishConfigCas(String dataId, String group, String content, String casMd5)
        throws NacosException;
    
    /**
     * 基于 MD5 的 CAS 条件发布指定类型配置。
     *
     * @param dataId  配置 Data ID
     * @param group   配置分组
     * @param content 新配置内容
     * @param casMd5  期望的当前内容 MD5
     * @param type    配置类型，参见 {@link ConfigType}
     * @return 发布成功返回 {@code true}
     * @throws NacosException 发布失败时抛出
     */
    @Since("2.0.0")
    boolean publishConfigCas(String dataId, String group, String content, String casMd5,
        String type)
        throws NacosException;
    
    /**
     * 从 Nacos 服务端删除指定配置。
     *
     * @param dataId 配置 Data ID
     * @param group  配置分组
     * @return 删除成功返回 {@code true}
     * @throws NacosException 删除失败时抛出
     */
    @Since("0.2.0")
    boolean removeConfig(String dataId, String group) throws NacosException;
    
    /**
     * 移除已注册的配置变更监听器。
     *
     * @param dataId   配置 Data ID
     * @param group    配置分组
     * @param listener 待移除的监听器
     */
    @Since("0.2.0")
    void removeListener(String dataId, String group, Listener listener);
    
    /**
     * 获取 Nacos 配置服务端健康状态。
     *
     * @return 健康状态描述字符串
     */
    @Since("0.2.0")
    String getServerStatus();
    
    /**
     * 添加配置过滤器，在拉取/发布链路中介入处理。
     *
     * <p>建议继承 {@link com.alibaba.nacos.api.config.filter.AbstractConfigFilter} 实现自定义过滤器。</p>
     *
     * @param configFilter 过滤器实例
     * @since 2.3.0
     */
    @Since("2.3.0")
    void addConfigFilter(IConfigFilter configFilter);
    
    /**
     * 关闭配置服务并释放相关资源。
     *
     * @throws NacosException 关闭过程中发生异常
     */
    @Since("1.3.1")
    void shutDown() throws NacosException;
    
    /**
     * 按分组名模式添加模糊配置监听。
     *
     * <p>匹配指定 group 模式下 dataId 的配置变更时回调 {@link FuzzyWatchEventWatcher}。</p>
     *
     * @param groupNamePattern 分组名匹配模式
     * @param watcher          模糊监听回调
     * @throws NacosException 注册失败时抛出
     * @since 3.0
     */
    @Since("3.0.0")
    void fuzzyWatch(String groupNamePattern, FuzzyWatchEventWatcher watcher) throws NacosException;
    
    /**
     * 按 dataId 与 group 模式添加模糊配置监听。
     *
     * @param dataIdPattern    dataId 匹配模式
     * @param groupNamePattern 分组名匹配模式
     * @param watcher          模糊监听回调
     * @throws NacosException 注册失败时抛出
     * @since 3.0
     */
    @Since("3.0.0")
    void fuzzyWatch(String dataIdPattern, String groupNamePattern, FuzzyWatchEventWatcher watcher)
        throws NacosException;
    
    /**
     * 添加模糊监听并返回匹配指定 group 模式的所有配置键。
     *
     * @param groupNamePattern 分组名匹配模式
     * @param watcher          模糊监听回调
     * @return 异步返回匹配的配置键集合
     * @throws NacosException 注册失败时抛出
     * @since 3.0
     */
    @Since("3.0.0")
    Future<Set<String>> fuzzyWatchWithGroupKeys(String groupNamePattern,
        FuzzyWatchEventWatcher watcher) throws NacosException;
    
    /**
     * 添加模糊监听并返回匹配 dataId 与 group 模式的所有配置键。
     *
     * @param dataIdPattern    dataId 匹配模式
     * @param groupNamePattern 分组名匹配模式
     * @param watcher          模糊监听回调
     * @return 异步返回匹配的配置键集合
     * @throws NacosException 注册失败时抛出
     * @since 3.0
     */
    @Since("3.0.0")
    Future<Set<String>> fuzzyWatchWithGroupKeys(String dataIdPattern, String groupNamePattern,
        FuzzyWatchEventWatcher watcher) throws NacosException;
    
    /**
     * 取消指定 group 模式的模糊监听并移除监听器。
     *
     * @param groupNamePattern 分组名匹配模式
     * @param watcher          待移除的模糊监听回调
     * @throws NacosException 取消过程中发生异常
     * @since 3.0
     */
    @Since("3.0.0")
    void cancelFuzzyWatch(String groupNamePattern, FuzzyWatchEventWatcher watcher)
        throws NacosException;
    
    /**
     * 取消指定 dataId 与 group 模式的模糊监听并移除监听器。
     *
     * @param dataIdPattern    dataId 匹配模式
     * @param groupNamePattern 分组名匹配模式
     * @param watcher          待移除的模糊监听回调
     * @throws NacosException 取消过程中发生异常
     * @since 3.0
     */
    @Since("3.0.0")
    void cancelFuzzyWatch(String dataIdPattern, String groupNamePattern,
        FuzzyWatchEventWatcher watcher)
        throws NacosException;
    
}
