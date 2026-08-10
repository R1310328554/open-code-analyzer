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

package com.alibaba.nacos.persistence.model.event;

import com.alibaba.nacos.common.notify.SlowEvent;

/**
 * Derby 数据库加载完成事件。
 *
 * <p>单例 {@link SlowEvent}，在嵌入式 Derby 初始化或 schema 加载完成后发布， 通知依赖方可以开始访问本地库。</p>
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class DerbyLoadEvent extends SlowEvent {
    
    /** 全局单例事件实例。 */
    public static final DerbyLoadEvent INSTANCE = new DerbyLoadEvent();
    
    private static final long serialVersionUID = 875401667921565121L;
    
}
