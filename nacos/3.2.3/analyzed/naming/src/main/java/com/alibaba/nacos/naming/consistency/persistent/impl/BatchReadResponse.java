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

package com.alibaba.nacos.naming.consistency.persistent.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 持久化存储批量读操作的响应体。
 *
 * <p>以并行列表形式返回键与值，供 Raft 批量读协议反序列化。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class BatchReadResponse implements Serializable {
    
    private static final long serialVersionUID = 5639813122389207205L;
    
    /** 批量读取的键列表。 */
    private List<byte[]> keys = new ArrayList<>(16);
    
    /** 与键一一对应的值列表。 */
    private List<byte[]> values = new ArrayList<>(16);
    
    public List<byte[]> getKeys() {
        return keys;
    }
    
    public void setKeys(List<byte[]> keys) {
        this.keys = keys;
    }
    
    public List<byte[]> getValues() {
        return values;
    }
    
    public void setValues(List<byte[]> values) {
        this.values = values;
    }
    
    /** 追加一对键值到响应列表末尾。 */
    public void append(byte[] key, byte[] value) {
        keys.add(key);
        values.add(value);
    }
}
