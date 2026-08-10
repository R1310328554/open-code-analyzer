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

package com.alibaba.nacos.core.cluster.lookup;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.core.cluster.MemberUtil;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.util.Collections;

/**
 * 单机模式寻址：仅将本机地址作为唯一成员，无需外部 serverlist。
 * Member node addressing mode in stand-alone mode.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class StandaloneMemberLookup extends AbstractMemberLookup {
    
    /** 启动时将本机 localAddress 作为唯一成员写入成员管理器。 */
    @Override
    public void doStart() {
        String url = EnvUtil.getLocalAddress();
        afterLookup(MemberUtil.readServerConf(Collections.singletonList(url)));
    }
    
    /** 单机模式无需销毁额外资源。 */
    @Override
    protected void doDestroy() throws NacosException {
        
    }
    
    /** 单机模式不使用 address-server。 */
    @Override
    public boolean useAddressServer() {
        return false;
    }
}
