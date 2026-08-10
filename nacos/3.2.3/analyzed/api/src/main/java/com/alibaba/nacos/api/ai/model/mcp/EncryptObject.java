/*
 * Copyright 1999-2025 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.api.ai.model.mcp;

import java.io.Serializable;
import java.util.Map;

/**
 * MCP 工具规范加密载荷包装类，承载密文及加密元数据（算法、IV、密钥 ID、版本等）。
 *
 * <p>当工具或资源规范需加密存储时，服务端将明文序列化后加密，
 * 并以本模型持久化 {@link #data} 与 {@link #encryptInfo}。</p>
 *
 * @author luoxiner
 */
public class EncryptObject implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 密文或编码后的载荷内容。 */
    private String data;
    
    /** 加密元数据，如 alg、iv、keyId、version 等键值对。 */
    private Map<String, String> encryptInfo;
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    public Map<String, String> getEncryptInfo() {
        return encryptInfo;
    }
    
    public void setEncryptInfo(Map<String, String> encryptInfo) {
        this.encryptInfo = encryptInfo;
    }
}
