/*
 * Copyright 1999-2023 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.lock.model;

import java.io.Serializable;
import java.util.Map;

/**
 * 锁操作信息 DTO，服务端内部传递加锁/解锁参数。
 *
 * <p>包含 {@link LockKey}、过期时间戳及扩展参数。</p>
 *
 * @author 985492783@qq.com
 * @date 2023/9/17 14:20
 */
public class LockInfo implements Serializable {
    
    private static final long serialVersionUID = -3460985546826875524L;
    
    /** 锁键（类型 + 资源键）。 */
    private LockKey key;
    
    /** 锁过期时间戳（毫秒）。 */
    private Long endTime;
    
    /** 扩展参数字典。 */
    private Map<String, ? extends Serializable> params;
    
    /** 无参构造，供序列化使用。 */
    public LockInfo() {
    }
    
    /** 获取锁键。 */
    public LockKey getKey() {
        return key;
    }
    
    /** 设置锁键。 */
    public void setKey(LockKey key) {
        this.key = key;
    }
    
    /** 获取过期时间戳。 */
    public Long getEndTime() {
        return endTime;
    }
    
    /** 设置过期时间戳。 */
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    
    /** 获取扩展参数。 */
    public Map<String, ? extends Serializable> getParams() {
        return params;
    }
    
    /** 设置扩展参数。 */
    public void setParams(Map<String, ? extends Serializable> params) {
        this.params = params;
    }
}
