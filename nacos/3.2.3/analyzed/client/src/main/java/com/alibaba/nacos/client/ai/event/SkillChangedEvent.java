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

package com.alibaba.nacos.client.ai.event;

import com.alibaba.nacos.common.notify.Event;

/**
 * Skill 变更内部通知事件。
 *
 * <p>由 {@code NacosSkillCacheHolder} 在轮询检测到服务端 Skill 内容 MD5 与本地缓存不一致时发布。{@code AiChangeNotifier} 消费该事件并将变更分派给相同缓存键下所有已注册的 {@code AbstractNacosSkillListener}。</p>
 *
 * @author nacos
 */
public class SkillChangedEvent extends Event {
    
    /** 序列化版本号。 */
    private static final long serialVersionUID = 1L;
    
    /** Skill 名称。 */
    private final String skillName;
    
    /** 本地缓存键。 */
    private final String cacheKey;
    
    /** 最新下载的 Skill ZIP 字节内容。 */
    private final byte[] zipBytes;
    
    /** 服务端发布的 Skill 内容 MD5。 */
    private final String md5;
    
    /** 服务端解析后的实际版本号。 */
    private final String resolvedVersion;
    
    /**
     * 构造 Skill 变更事件。
     *
     * @param skillName       Skill 名称
     * @param cacheKey        本地缓存键
     * @param zipBytes        ZIP 字节内容
     * @param md5             内容 MD5
     * @param resolvedVersion 解析后的版本号
     */
        this.skillName = skillName;
        this.cacheKey = cacheKey;
        this.zipBytes = zipBytes;
        this.md5 = md5;
        this.resolvedVersion = resolvedVersion;
    }
    
    /** 返回 Skill 名称。 */
    public String getSkillName() {
        return skillName;
    }
    
    /** 返回本地缓存键。 */
    public String getCacheKey() {
        return cacheKey;
    }
    
    /** 返回 Skill ZIP 字节内容。 */
    public byte[] getZipBytes() {
        return zipBytes;
    }
    
    /** 返回内容 MD5。 */
    public String getMd5() {
        return md5;
    }
    
    /** 返回解析后的版本号。 */
    public String getResolvedVersion() {
        return resolvedVersion;
    }
}
