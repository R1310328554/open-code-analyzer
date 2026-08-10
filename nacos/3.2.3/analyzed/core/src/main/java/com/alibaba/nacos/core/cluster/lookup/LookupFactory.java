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
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.cluster.MemberLookup;
import com.alibaba.nacos.core.cluster.ServerMemberManager;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * 成员寻址工厂：根据配置或环境选择 file / address-server 等 {@link MemberLookup} 实现。
 * An addressing pattern factory, responsible for the creation of all addressing patterns.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public final class LookupFactory {
    
    private static final String LOOKUP_MODE_TYPE = "nacos.core.member.lookup.type";
    
    @SuppressWarnings("checkstyle:StaticVariableName")
    private static MemberLookup LOOK_UP = null;
    
    private static LookupType currentLookupType = null;
    
    /**
     * 根据配置创建目标寻址实现（不含成员管理器注入）。
     *
     * @return {@link MemberLookup}
     * @throws NacosException 创建失败时抛出
     */
    public static MemberLookup createLookUp() throws NacosException {
        String lookupType = EnvUtil.getProperty(LOOKUP_MODE_TYPE);
        LookupType type = chooseLookup(lookupType);
        currentLookupType = type;
        return find(type);
    }
    
    /**
     * 创建寻址实例并注入 {@link ServerMemberManager}；单机模式使用 {@link StandaloneMemberLookup}。
     *
     * @param memberManager {@link ServerMemberManager}
     * @return {@link MemberLookup}
     * @throws NacosException 创建失败时抛出
     */
    public static MemberLookup createLookUp(ServerMemberManager memberManager)
        throws NacosException {
        if (!EnvUtil.getStandaloneMode()) {
            LOOK_UP = createLookUp();
        } else {
            LOOK_UP = new StandaloneMemberLookup();
        }
        LOOK_UP.injectMemberManager(memberManager);
        Loggers.CLUSTER.info("Current addressing mode selection : {}",
            LOOK_UP.getClass().getSimpleName());
        return LOOK_UP;
    }
    
    /**
     * 运行时切换到指定寻址模式，必要时销毁旧实例。
     *
     * @param name          寻址模式名称
     * @param memberManager {@link ServerMemberManager}
     * @return 新的 {@link MemberLookup}
     * @throws NacosException 切换失败时抛出
     */
    public static MemberLookup switchLookup(String name, ServerMemberManager memberManager)
        throws NacosException {
        LookupType lookupType = LookupType.sourceOf(name);
        
        if (Objects.isNull(lookupType)) {
            throw new IllegalArgumentException(
                "The addressing mode exists : " + name + ", just support : ["
                    + Arrays.toString(LookupType.values())
                    + "]");
        }
        
        if (Objects.equals(currentLookupType, lookupType)) {
            return LOOK_UP;
        }
        MemberLookup newLookup = find(lookupType);
        currentLookupType = lookupType;
        if (Objects.nonNull(LOOK_UP)) {
            LOOK_UP.destroy();
        }
        LOOK_UP = newLookup;
        LOOK_UP.injectMemberManager(memberManager);
        Loggers.CLUSTER.info("Current addressing mode selection : {}",
            LOOK_UP.getClass().getSimpleName());
        return LOOK_UP;
    }
    
    private static MemberLookup find(LookupType type) {
        if (LookupType.FILE_CONFIG.equals(type)) {
            LOOK_UP = new FileConfigMemberLookup();
            return LOOK_UP;
        }
        if (LookupType.ADDRESS_SERVER.equals(type)) {
            LOOK_UP = new AddressServerMemberLookup();
            return LOOK_UP;
        }
        // 不应执行到此分支
        throw new IllegalArgumentException();
    }
    
    private static LookupType chooseLookup(String lookupType) {
        if (StringUtils.isNotBlank(lookupType)) {
            LookupType type = LookupType.sourceOf(lookupType);
            if (Objects.nonNull(type)) {
                return type;
            }
        }
        File file = new File(EnvUtil.getClusterConfFilePath());
        if (file.exists() || StringUtils.isNotBlank(EnvUtil.getMemberList())) {
            return LookupType.FILE_CONFIG;
        }
        return LookupType.ADDRESS_SERVER;
    }
    
    public static MemberLookup getLookUp() {
        return LOOK_UP;
    }
    
    public static void destroy() throws NacosException {
        Objects.requireNonNull(LOOK_UP).destroy();
    }
    
    public enum LookupType {
        
        /**
         * 基于 cluster.conf 文件的寻址模式。
         */
        FILE_CONFIG(1, "file"),
        
        /**
         * 基于 address-server 的寻址模式。
         */
        ADDRESS_SERVER(2, "address-server");
        
        private final int code;
        
        private final String name;
        
        LookupType(int code, String name) {
            this.code = code;
            this.name = name;
        }
        
        /**
         * 按名称解析 {@link LookupType}，未匹配则返回 null。
         *
         * @param name 模式名称
         * @return {@link LookupType}
         */
        public static LookupType sourceOf(String name) {
            for (LookupType type : values()) {
                if (Objects.equals(type.name, name)) {
                    return type;
                }
            }
            return null;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
}
