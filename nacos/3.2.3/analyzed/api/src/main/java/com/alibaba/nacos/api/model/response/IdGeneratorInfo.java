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

package com.alibaba.nacos.api.model.response;

/**
 * ID 生成器状态信息。
 *
 * <p>描述某资源对应的 Snowflake 式 ID 生成器当前序号与 workerId。</p>
 *
 * @author wuzhiguo
 */
public class IdGeneratorInfo {
    
    /** 资源名称（如配置、服务等）。 */
    private String resource;
    
    /** ID 生成器运行时详情。 */
    private IdInfo info;
    
    /** 获取资源名称。 */
    public String getResource() {
        return resource;
    }
    
    /** 设置资源名称。 */
    public void setResource(String resource) {
        this.resource = resource;
    }
    
    /** 获取 ID 生成器详情。 */
    public IdInfo getInfo() {
        return info;
    }
    
    /** 设置 ID 生成器详情。 */
    public void setInfo(IdInfo info) {
        this.info = info;
    }
    
    /** ID 生成器运行时序号与 worker 信息。 */
    public static class IdInfo {
        
        /** 当前已分配的最大 ID。 */
        private Long currentId;
        
        /** Snowflake worker 节点 ID。 */
        private Long workerId;
        
        /** 获取当前 ID 序号。 */
        public Long getCurrentId() {
            return currentId;
        }
        
        /** 设置当前 ID 序号。 */
        public void setCurrentId(Long currentId) {
            this.currentId = currentId;
        }
        
        /** 获取 worker ID。 */
        public Long getWorkerId() {
            return workerId;
        }
        
        /** 设置 worker ID。 */
        public void setWorkerId(Long workerId) {
            this.workerId = workerId;
        }
        
        @Override
        public String toString() {
            return "IdInfo{" + "currentId=" + currentId + ", workerId=" + workerId + '}';
        }
    }
    
    @Override
    public String toString() {
        return "IdGeneratorVO{" + "resource='" + resource + '\'' + ", info=" + info + '}';
    }
}
