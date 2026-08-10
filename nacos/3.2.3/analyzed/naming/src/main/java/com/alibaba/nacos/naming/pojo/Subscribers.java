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

package com.alibaba.nacos.naming.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * 订阅者列表包装 POJO。
 *
 * <p>作为 OpenAPI 批量查询订阅关系的响应体，内含 {@link Subscriber} 列表。</p>
 *
 * @author nicholas
 * @version $Id: Subscribers.java, v 0.1 2019-05-28 下午10:47 nicholas Exp $$
 */
public class Subscribers implements Serializable {
    
    private static final long serialVersionUID = -3075690233070417052L;
    
    private List<Subscriber> subscribers;
    
    /** 获取订阅者列表。 */
    public List<Subscriber> getSubscribers() {
        return subscribers;
    }
    
    /** 设置订阅者列表。 */
    public void setSubscribers(List<Subscriber> subscribers) {
        this.subscribers = subscribers;
    }
}
