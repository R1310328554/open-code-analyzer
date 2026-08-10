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

package com.alibaba.nacos.api.ai.listener;

/**
 * Nacos AI 模块 Skill 变更事件。
 *
 * <p>当订阅的 Skill 在服务端发生变更时触发。{@link #zipBytes} 携带最新下载的
 * Skill ZIP 包（含 SKILL.md 与资源文件）；{@link #md5} 为服务端发布的内容指纹，
 * 可用于与后续版本对比或持久化本地缓存。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public class NacosSkillEvent implements NacosAiEvent {
    
    private final String skillName;
    
    private final byte[] zipBytes;
    
    private final String md5;
    
    private final String resolvedVersion;
    
    /**
     * 构造 Skill 变更事件。
     *
     * @param skillName Skill 名称
     * @param zipBytes Skill ZIP 字节，删除时为 null
     * @param md5 内容 MD5 指纹
     * @param resolvedVersion 解析后的版本号（按标签订阅时）
     */
        this.skillName = skillName;
        this.zipBytes = zipBytes;
        this.md5 = md5;
        this.resolvedVersion = resolvedVersion;
    }
    
    /**
     * 获取 Skill 名称。
     *
     * @return skill name
     */
    public String getSkillName() {
        return skillName;
    }
    
    /**
     * 获取 Skill ZIP 载荷；服务端已删除该 Skill 时为 {@code null}。
     *
     * @return skill ZIP byte array, or {@code null} if the skill no longer exists
     */
    public byte[] getZipBytes() {
        return zipBytes;
    }
    
    /**
     * 获取本 Skill 版本的内容 MD5；删除事件或未携带指纹响应头时为 {@code null}。
     *
     * @return content MD5
     */
    public String getMd5() {
        return md5;
    }
    
    /**
     * 获取按标签订阅时解析出的版本字符串；显式指定版本或未携带解析版本响应头时为 {@code null}。
     *
     * @return resolved version, optional
     */
    public String getResolvedVersion() {
        return resolvedVersion;
    }
}
