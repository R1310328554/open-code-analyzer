/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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

package com.alibaba.nacos.plugin.auth.impl.users;

import java.io.Serializable;

/**
 * 鉴权模块中的用户信息模型。
 *
 * <p>轻量级用户标识，仅包含 {@code userName} 字段， 用于鉴权上下文传递与序列化，区别于持久化层 {@code persistence.User}。</p>
 *
 * @author nkorange
 * @author mai.jh
 * @since 1.2.0
 */
public class User implements Serializable {
    
    private static final long serialVersionUID = -8002966873087151367L;
    
    /** 唯一标识用户的登录名。 */
    private String userName;
    
    /** 获取用户名。 */
    public String getUserName() {
        return userName;
    }
    
    /** 设置用户名。 */
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
