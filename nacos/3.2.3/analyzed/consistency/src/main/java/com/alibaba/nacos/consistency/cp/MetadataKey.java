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

package com.alibaba.nacos.consistency.cp;

/**
 * CP 协议元数据键名常量，与 {@link com.alibaba.nacos.consistency.ProtocolMetaData} 配合使用。
 *
 * Key value of metadata information of CP protocol.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class MetadataKey {
    
    /** 当前 Raft group 的 leader 节点标识。 */
    public static final String LEADER_META_DATA = "leader";
    
    /** 当前 Raft term（任期）编号。 */
    public static final String TERM_META_DATA = "term";
    
    /** Raft group 成员列表相关元数据键。 */
    public static final String RAFT_GROUP_MEMBER = "raftGroupMember";
    
    /** 协议异常或错误描述信息键。 */
    public static final String ERR_MSG = "errMsg";
    
}
