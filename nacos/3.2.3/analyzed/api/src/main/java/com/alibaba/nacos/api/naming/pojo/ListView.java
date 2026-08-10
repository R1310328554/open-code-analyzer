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

package com.alibaba.nacos.api.naming.pojo;

import java.util.List;

/**
 * 分页列表视图，封装数据列表与总条数。
 *
 * <p>常用于命名服务查询接口的分页响应，{@code count} 表示符合条件的总记录数，
 * {@code data} 为当前页的数据集合。</p>
 *
 * @author nkorange
 */
public class ListView<T> {
    
    /** 当前页数据列表。 */
    private List<T> data;
    
    /** 符合条件的总记录数。 */
    private int count;
    
    /** 获取当前页数据列表。 */
    public List<T> getData() {
        return data;
    }
    
    /** 设置当前页数据列表。 */
    public void setData(List<T> data) {
        this.data = data;
    }
    
    /** 获取总记录数。 */
    public int getCount() {
        return count;
    }
    
    /** 设置总记录数。 */
    public void setCount(int count) {
        this.count = count;
    }
    
    @Override
    public String toString() {
        return "ListView{" + "data=" + data + ", count=" + count + '}';
    }
}
