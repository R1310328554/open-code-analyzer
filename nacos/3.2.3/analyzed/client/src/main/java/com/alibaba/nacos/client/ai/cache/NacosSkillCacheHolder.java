/*
 * Copyright 1999-2026 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.client.ai.cache;

import com.alibaba.nacos.api.ai.constant.AiConstants;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.client.ai.event.SkillChangedEvent;
import com.alibaba.nacos.client.ai.remote.AiClientProxy;
import com.alibaba.nacos.client.ai.remote.SkillQueryResponse;
import com.alibaba.nacos.client.ai.utils.CacheKeyUtils;
import com.alibaba.nacos.client.env.NacosClientProperties;
import com.alibaba.nacos.client.utils.LogUtils;
import com.alibaba.nacos.common.executor.NameThreadFactory;
import com.alibaba.nacos.common.lifecycle.Closeable;
import com.alibaba.nacos.common.notify.NotifyCenter;
import com.alibaba.nacos.common.utils.StringUtils;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Nacos AI 模块 Skill 本地缓存持有者。
 *
 * <p>为每个订阅维护轮询循环，周期性调用
 * {@link AiClientProxy#querySkill(String, String, String, String)} 并携带本地 MD5
 * 进行条件下载。服务端返回 304（{@link NacosException#NOT_MODIFIED}）时保留本地缓存且不触发回调；
 * 内容变更（MD5 不同）时发布 {@link SkillChangedEvent}，由 {@code AiChangeNotifier}
 * 分发给已注册监听器。</p>
 *
 * @author nacos
 */
public class NacosSkillCacheHolder implements Closeable {
    
    private static final Logger LOGGER = LogUtils.logger(NacosSkillCacheHolder.class);
    
    private final AiClientProxy aiClientProxy;
    
    /**
     * cacheKey -> 本地缓存 Skill ZIP 上次发布的 MD5 指纹。
     */
    private final Map<String, String> skillMd5Cache;
    
    private final ScheduledExecutorService updaterExecutor;
    
    private final long updateIntervalMillis;
    
    private final Map<String, SkillUpdater> updateTaskMap;
    
    public NacosSkillCacheHolder(AiClientProxy aiClientProxy, NacosClientProperties properties) {
        this.aiClientProxy = aiClientProxy;
        this.skillMd5Cache = new ConcurrentHashMap<>(4);
        this.updateTaskMap = new ConcurrentHashMap<>(4);
        this.updaterExecutor = new ScheduledThreadPoolExecutor(1,
            new NameThreadFactory("com.alibaba.nacos.client.ai.skill.updater"));
        this.updateIntervalMillis = properties.getLong(AiConstants.AI_SKILL_CACHE_UPDATE_INTERVAL,
            AiConstants.DEFAULT_AI_CACHE_UPDATE_INTERVAL);
    }
    
    /**
     * 订阅 Skill 并启动变更轮询。
     *
     * <p>首次同步下载并填充 MD5 缓存；后续轮询携带 MD5 以跳过未变更内容。</p>
     *
     * @param skillName skill name
     * @param version   skill version, optional
     * @param label     skill label, optional
     * @return current skill ZIP bytes, never null when the server has the skill
     * @throws NacosException if error occurs
     */
    public byte[] subscribeSkill(String skillName, String version, String label)
        throws NacosException {
        if (StringUtils.isBlank(skillName)) {
            throw new NacosException(NacosException.INVALID_PARAM,
                "Required parameter `skillName` not present");
        }
        String cacheKey = CacheKeyUtils.buildSkillKey(skillName, version, label);
        
        byte[] zipBytes = null;
        try {
            SkillQueryResponse response = aiClientProxy.querySkill(skillName, version, label, null);
            zipBytes = response.getZipBytes();
            // 首次订阅仅更新缓存，不在此处发布事件；
            // 首次监听器通知由 NacosAiService 负责，避免与 NotifyCenter 异步分发重复回调。
            String newMd5 = response.getMd5();
            if (StringUtils.isNotBlank(newMd5)) {
                skillMd5Cache.put(cacheKey, newMd5);
            }
        } catch (NacosException e) {
            if (e.getErrCode() != NacosException.NOT_FOUND) {
                throw e;
            }
            skillMd5Cache.remove(cacheKey);
        }
        addSkillUpdateTask(skillName, version, label);
        LOGGER.info("Subscribed skill: {}, version: {}, label: {}", skillName, version, label);
        return zipBytes;
    }
    
    /**
     * 取消 Skill 订阅并移除轮询任务。
     *
     * @param skillName skill name
     * @param version   skill version, optional
     * @param label     skill label, optional
     */
    public void unsubscribeSkill(String skillName, String version, String label) {
        if (StringUtils.isBlank(skillName)) {
            return;
        }
        String cacheKey = CacheKeyUtils.buildSkillKey(skillName, version, label);
        
        removeSkillUpdateTask(skillName, version, label);
        skillMd5Cache.remove(cacheKey);
        LOGGER.info("Unsubscribed skill: {}, version: {}, label: {}", skillName, version, label);
    }
    
    @Override
    public void shutdown() throws NacosException {
        this.updaterExecutor.shutdownNow();
    }
    
    private void addSkillUpdateTask(String skillName, String version, String label) {
        String key = CacheKeyUtils.buildSkillKey(skillName, version, label);
        this.updateTaskMap.computeIfAbsent(key, s -> {
            SkillUpdater task = new SkillUpdater(skillName, version, label);
            updaterExecutor.schedule(task, updateIntervalMillis, TimeUnit.MILLISECONDS);
            return task;
        });
    }
    
    private void removeSkillUpdateTask(String skillName, String version, String label) {
        String key = CacheKeyUtils.buildSkillKey(skillName, version, label);
        SkillUpdater task = this.updateTaskMap.remove(key);
        if (task != null) {
            task.cancel();
        }
    }
    
    /** 处理 Skill 轮询响应，更新 MD5 并在变更时发布事件。 */
    private void processSkill(String skillName, String cacheKey, SkillQueryResponse response) {
        String oldMd5 = skillMd5Cache.get(cacheKey);
        String newMd5 = response == null ? null : response.getMd5();
        if (response == null) {
            skillMd5Cache.remove(cacheKey);
        } else if (StringUtils.isNotBlank(newMd5)) {
            skillMd5Cache.put(cacheKey, newMd5);
        }
        if (response != null && !StringUtils.equals(oldMd5, newMd5)) {
            NotifyCenter.publishEvent(new SkillChangedEvent(skillName, cacheKey,
                response.getZipBytes(), newMd5, response.getResolvedVersion()));
        }
    }
    
    /** 定时条件下载 Skill 的内部轮询任务。 */
    private class SkillUpdater implements Runnable {
        
        private final String skillName;
        
        private final String version;
        
        private final String label;
        
        private final String cacheKey;
        
        private final AtomicBoolean cancel = new AtomicBoolean(false);
        
        SkillUpdater(String skillName, String version, String label) {
            this.skillName = skillName;
            this.version = version;
            this.label = label;
            this.cacheKey = CacheKeyUtils.buildSkillKey(skillName, version, label);
        }
        
        void cancel() {
            cancel.set(true);
        }
        
        @Override
        public void run() {
            if (cancel.get()) {
                return;
            }
            try {
                String currentMd5 = skillMd5Cache.get(cacheKey);
                SkillQueryResponse response = aiClientProxy.querySkill(skillName, version, label,
                    currentMd5);
                processSkill(skillName, cacheKey, response);
            } catch (NacosException e) {
                if (e.getErrCode() == NacosException.NOT_FOUND) {
                    processSkill(skillName, cacheKey, null);
                } else if (e.getErrCode() == NacosException.NOT_MODIFIED) {
                    // 内容未变更，保留本地缓存并跳过回调。
                } else {
                    LOGGER.warn("Skill updater execute query failed: skillName={}, err={}",
                        skillName, e.getErrMsg());
                }
            } finally {
                if (!cancel.get()) {
                    updaterExecutor.schedule(this, updateIntervalMillis, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
