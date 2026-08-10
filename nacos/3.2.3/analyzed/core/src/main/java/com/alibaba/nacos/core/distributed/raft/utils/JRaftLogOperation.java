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

package com.alibaba.nacos.core.distributed.raft.utils;

/**
 * JRaft 日志附加操作类型常量：写入 Raft 日志扩展字段，区分读/写 apply 路径。
 * JRaft for additional information on logging operations.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class JRaftLogOperation {
    
    /** 写操作（状态变更）日志标记。 */
    public static final String MODIFY_OPERATION = "modify";
    
    /** 只读操作日志标记。 */
    public static final String READ_OPERATION = "read";
    
}
