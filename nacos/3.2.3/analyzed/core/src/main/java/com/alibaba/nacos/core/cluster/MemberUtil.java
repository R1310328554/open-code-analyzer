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

package com.alibaba.nacos.core.cluster;

import com.alibaba.nacos.api.common.NodeState;
import com.alibaba.nacos.common.utils.ExceptionUtil;
import com.alibaba.nacos.common.utils.InternetAddressUtil;
import com.alibaba.nacos.common.utils.StringUtils;
import com.alibaba.nacos.core.utils.Loggers;
import com.alibaba.nacos.sys.env.EnvUtil;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 集群成员工具类：解析地址、同步文件、健康状态迁移及元数据变更比较等通用逻辑。
 * Member node tool class.
 *
 * @author <a href="mailto:liaochuntao@live.com">liaochuntao</a>
 */
public class MemberUtil {
    
    protected static final String TARGET_MEMBER_CONNECT_REFUSE_ERRMSG = "Connection refused";
    
    private static final String SERVER_PORT_PROPERTY = "nacos.server.main.port";
    
    private static final int DEFAULT_SERVER_PORT = 8848;
    
    private static final int DEFAULT_RAFT_OFFSET_PORT = 1000;
    
    private static final String MEMBER_FAIL_ACCESS_CNT_PROPERTY =
        "nacos.core.member.fail-access-cnt";
    
    private static final int DEFAULT_MEMBER_FAIL_ACCESS_CNT = 3;
    
    /**
     * 将新成员的基础字段与扩展信息复制到已有成员对象。
     *
     * @param newMember {@link Member}
     * @param oldMember {@link Member}
     */
    public static void copy(Member newMember, Member oldMember) {
        oldMember.setIp(newMember.getIp());
        oldMember.setPort(newMember.getPort());
        oldMember.setState(newMember.getState());
        oldMember.setExtendInfo(newMember.getExtendInfo());
        oldMember.setAddress(newMember.getAddress());
        oldMember.setAbilities(newMember.getAbilities());
        oldMember.setGrpcReportEnabled(newMember.isGrpcReportEnabled());
    }
    
    /**
     * 将 ip 或 ip:port 字符串解析为 {@link Member}，并填充默认 Raft 端口等元数据。
     *
     * @param member ip:port
     * @return {@link Member}
     */
    public static Member singleParse(String member) {
        // Nacos 默认服务端口 8848
        int defaultPort =
            EnvUtil.getProperty(SERVER_PORT_PROPERTY, Integer.class, DEFAULT_SERVER_PORT);
        // 预置 Raft 端口等安全相关元数据
        
        String address = member;
        int port = defaultPort;
        String[] info = InternetAddressUtil.splitIpPortStr(address);
        if (info.length > 1) {
            address = info[0];
            port = Integer.parseInt(info[1]);
        }
        
        Member target = Member.builder().ip(address).port(port).state(NodeState.UP).build();
        Map<String, Object> extendInfo = new HashMap<>(4);
        // 默认写入 Raft 端口偏移计算结果
        extendInfo.put(MemberMetaDataConstants.RAFT_PORT,
            String.valueOf(calculateRaftPort(target)));
        extendInfo.put(MemberMetaDataConstants.READY_TO_UPGRADE, true);
        target.setExtendInfo(extendInfo);
        // 默认启用 gRPC 上报
        target.setGrpcReportEnabled(true);
        return target;
    }
    
    /**
     * 判断成员是否支持远程长连接（gRPC 上报或能力位声明）。
     *
     * @param member member instance of server.
     * @return support long connection or not.
     */
    public static boolean isSupportedLongCon(Member member) {
        if (member.getAbilities() == null || member.getAbilities().getRemoteAbility() == null) {
            return false;
        }
        
        boolean oldVerJudge = member.getAbilities().getRemoteAbility().isSupportRemoteConnection();
        
        return member.isGrpcReportEnabled() || oldVerJudge;
    }
    
    public static int calculateRaftPort(Member member) {
        return member.getPort() - DEFAULT_RAFT_OFFSET_PORT;
    }
    
    /**
     * 批量解析地址列表为成员集合。
     *
     * @param addresses ip list, example [127.0.0.1:8847,127.0.0.1:8848,127.0.0.1:8849]
     * @return member list
     */
    public static Collection<Member> multiParse(Collection<String> addresses) {
        List<Member> members = new ArrayList<>(addresses.size());
        for (String address : addresses) {
            Member member = singleParse(address);
            members.add(member);
        }
        return members;
    }
    
    /**
     * 节点访问成功：重置失败计数、标记 UP 并在状态变化时通知监听器。
     *
     * @param member {@link Member}
     */
    public static void onSuccess(final ServerMemberManager manager, final Member member) {
        final NodeState old = member.getState();
        manager.getMemberAddressInfos().add(member.getAddress());
        member.setState(NodeState.UP);
        member.setFailAccessCnt(0);
        if (!Objects.equals(old, member.getState())) {
            manager.notifyMemberChange(member);
        }
    }
    
    /**
     * 节点访问成功且对端返回元数据：若能力或扩展信息变更则合并后通知。
     *
     * @param member {@link Member}
     * @since 2.1.2
     */
    public static void onSuccess(final ServerMemberManager manager, final Member member,
        final Member receivedMember) {
        if (isMetadataChanged(member, receivedMember)) {
            manager.getMemberAddressInfos().add(member.getAddress());
            member.setState(NodeState.UP);
            member.setFailAccessCnt(0);
            member.setExtendInfo(receivedMember.getExtendInfo());
            member.setAbilities(receivedMember.getAbilities());
            manager.notifyMemberChange(member);
        } else {
            onSuccess(manager, member);
        }
    }
    
    private static boolean isMetadataChanged(Member expected, Member actual) {
        return !Objects.equals(expected.getAbilities(), actual.getAbilities())
            || isBasicInfoChangedInExtendInfo(
                expected, actual);
    }
    
    public static void onFail(final ServerMemberManager manager, final Member member) {
        // 避免空指针，传入 NONE_EXCEPTION 占位
        onFail(manager, member, ExceptionUtil.NONE_EXCEPTION);
    }
    
    /**
     * 节点访问失败：递增失败次数，超阈值或连接被拒时标记 DOWN 并通知。
     *
     * @param member {@link Member}
     * @param ex     {@link Throwable}
     */
    public static void onFail(final ServerMemberManager manager, final Member member,
        Throwable ex) {
        manager.getMemberAddressInfos().remove(member.getAddress());
        final NodeState old = member.getState();
        member.setState(NodeState.SUSPICIOUS);
        member.setFailAccessCnt(member.getFailAccessCnt() + 1);
        int maxFailAccessCnt = EnvUtil
            .getProperty(MEMBER_FAIL_ACCESS_CNT_PROPERTY, Integer.class,
                DEFAULT_MEMBER_FAIL_ACCESS_CNT);
        
        // 连续失败超过阈值或连接被拒绝时，直接将节点置为 DOWN
        if (member.getFailAccessCnt() > maxFailAccessCnt || StringUtils
            .containsIgnoreCase(ex.getMessage(), TARGET_MEMBER_CONNECT_REFUSE_ERRMSG)) {
            member.setState(NodeState.DOWN);
        }
        if (!Objects.equals(old, member.getState())) {
            manager.notifyMemberChange(member);
        }
    }
    
    /**
     * 将当前成员地址列表持久化到集群配置文件。
     *
     * @param members member list
     */
    public static void syncToFile(Collection<Member> members) {
        try {
            StringBuilder builder = new StringBuilder();
            builder.append('#').append(LocalDateTime.now()).append(StringUtils.LF);
            for (String member : simpleMembers(members)) {
                builder.append(member).append(StringUtils.LF);
            }
            EnvUtil.writeClusterConf(builder.toString());
        } catch (Throwable ex) {
            Loggers.CLUSTER.error("cluster member node persistence failed : {}",
                ExceptionUtil.getAllExceptionMsg(ex));
        }
    }
    
    /**
     * 解析集群配置文件中的成员行（IP、IP:port 或 hostname:port）。
     */
    public static Collection<Member> readServerConf(Collection<String> members) {
        Set<Member> nodes = new HashSet<>();
        
        for (String member : members) {
            Member target = singleParse(member);
            nodes.add(target);
        }
        
        return nodes;
    }
    
    /**
     * 按谓词筛选目标成员子集。
     *
     * @param members original members
     * @param filter  filter
     * @return target members
     */
    public static Set<Member> selectTargetMembers(Collection<Member> members,
        Predicate<Member> filter) {
        return members.stream().filter(filter).collect(Collectors.toSet());
    }
    
    /**
     * 提取成员 address 字段并排序返回。
     *
     * @param members members
     * @return address list
     */
    public static List<String> simpleMembers(Collection<Member> members) {
        return members.stream().map(Member::getAddress).sorted()
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
    
    /**
     * 比较 IP、端口、状态、gRPC 开关及基础扩展元数据是否发生变化。
     *
     * @param actual   actual member
     * @param expected expected member
     * @return true if one content is different, otherwise false
     */
    public static boolean isBasicInfoChanged(Member actual, Member expected) {
        if (null == expected) {
            return null != actual;
        }
        if (!expected.getIp().equals(actual.getIp())) {
            return true;
        }
        if (expected.getPort() != actual.getPort()) {
            return true;
        }
        if (!expected.getAddress().equals(actual.getAddress())) {
            return true;
        }
        if (!expected.getState().equals(actual.getState())) {
            return true;
        }
        
        // gRPC 上报开关变更亦视为基础信息变化
        if (expected.isGrpcReportEnabled() != actual.isGrpcReportEnabled()) {
            return true;
        }
        
        return isBasicInfoChangedInExtendInfo(expected, actual);
    }
    
    private static boolean isBasicInfoChangedInExtendInfo(Member expected, Member actual) {
        for (String each : MemberMetaDataConstants.BASIC_META_KEYS) {
            if (expected.getExtendInfo().containsKey(each) != actual.getExtendInfo()
                .containsKey(each)) {
                return true;
            }
            if (!Objects.equals(expected.getExtendVal(each), actual.getExtendVal(each))) {
                return true;
            }
        }
        return false;
    }
}
