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

package com.alibaba.nacos.client.ai.remote;

/**
 * 监听式 Skill 查询响应包装类。
 *
 * <p>携带新下载的 ZIP 字节及响应头 {@code X-Nacos-Skill-Md5}、{@code X-Nacos-Skill-Resolved-Version}，使客户端缓存可在下次轮询时通过 MD5 条件查询短路。</p>
 *
 * @author nacos
 * @since 3.2.0
 */
public class SkillQueryResponse {
    
    /** 下载的 Skill ZIP 字节内容。 */
    private final byte[] zipBytes;
    
    /** 服务端发布内容的 MD5 指纹。 */
    private final String md5;
    
    /** 服务端解析后的实际版本号。 */
    private final String resolvedVersion;
    
    /**
     * 构造 Skill 查询响应。
     *
     * @param zipBytes        ZIP 字节内容
     * @param md5             内容 MD5
     * @param resolvedVersion 解析后的版本号
     */
        this.zipBytes = zipBytes;
        this.md5 = md5;
        this.resolvedVersion = resolvedVersion;
    }
    
    /** 返回 ZIP 字节内容。 */
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
