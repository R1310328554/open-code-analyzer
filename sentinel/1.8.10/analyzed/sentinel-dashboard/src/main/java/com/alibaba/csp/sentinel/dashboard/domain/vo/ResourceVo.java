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
package com.alibaba.csp.sentinel.dashboard.domain.vo;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.command.vo.NodeVo;

import com.alibaba.csp.sentinel.dashboard.domain.ResourceTreeNode;

/**
 * 资源调用树节点视图，用于 Dashboard 实时展示 QPS、RT、线程数等指标。
 * <p>可由 {@link NodeVo} 列表或 {@link ResourceTreeNode} 递归遍历生成。
 *
 * @author leyou
 */
public class ResourceVo {
    /** 父节点 id。 */
    private String parentTtId;
    /** 本节点 id。 */
    private String ttId;
    /** 资源名称。 */
    private String resource;

    /** 当前占用线程数。 */
    private Integer threadNum;
    /** 通过 QPS。 */
    private Long passQps;
    /** 被限流 QPS。 */
    private Long blockQps;
    /** 总 QPS（通过 + 限流）。 */
    private Long totalQps;
    /** 平均响应时间（毫秒）。 */
    private Long averageRt;
    /** 通过请求 QPS。 */
    private Long passRequestQps;
    /** 异常 QPS。 */
    private Long exceptionQps;
    /** 近一分钟通过数。 */
    private Long oneMinutePass;
    /** 近一分钟被限流数。 */
    private Long oneMinuteBlock;
    /** 近一分钟异常数。 */
    private Long oneMinuteException;
    /** 近一分钟总请求数。 */
    private Long oneMinuteTotal;

    /** 搜索过滤后是否在 UI 中可见。 */
    private boolean visible = true;

    /** 无参构造。 */
    public ResourceVo() {
    }

    /** 将客户端 {@link NodeVo} 列表转换为扁平 {@link ResourceVo} 列表。 */
    public static List<ResourceVo> fromNodeVoList(List<NodeVo> nodeVos) {
        if (nodeVos == null) {
            return null;
        }
        List<ResourceVo> list = new ArrayList<>();
        for (NodeVo nodeVo : nodeVos) {
            ResourceVo vo = new ResourceVo();
            vo.parentTtId = nodeVo.getParentId();
            vo.ttId = nodeVo.getId();
            vo.resource = nodeVo.getResource();
            vo.threadNum = nodeVo.getThreadNum();
            vo.passQps = nodeVo.getPassQps();
            vo.blockQps = nodeVo.getBlockQps();
            vo.totalQps = nodeVo.getTotalQps();
            vo.averageRt = nodeVo.getAverageRt();
            vo.exceptionQps = nodeVo.getExceptionQps();
            vo.oneMinutePass = nodeVo.getOneMinutePass();
            vo.oneMinuteBlock = nodeVo.getOneMinuteBlock();
            vo.oneMinuteException = nodeVo.getOneMinuteException();
            vo.oneMinuteTotal = nodeVo.getOneMinuteTotal();
            list.add(vo);
        }
        return list;
    }

    /** 从 {@link ResourceTreeNode} 根节点递归收集可见节点为 VO 列表。 */
    public static List<ResourceVo> fromResourceTreeNode(ResourceTreeNode root) {
        if (root == null) {
            return null;
        }
        List<ResourceVo> list = new ArrayList<>();
        visit(root, list, false, true);
        // 可选：移除根节点占位项（当前保留全部可见节点）。
        return list;
    }

    /**
     * 递归遍历资源树：根节点始终不可见；
     * 子节点在本节点 visible 为 true 或父链已可见时加入结果列表。
     */
    private static void visit(ResourceTreeNode node, List<ResourceVo> list, boolean parentVisible, boolean isRoot) {
        boolean visible = !isRoot && (node.isVisible() || parentVisible);
        // 备选：仅依据节点自身 visible 标记。
        if (visible) {
            ResourceVo vo = new ResourceVo();
            vo.parentTtId = node.getParentId();
            vo.ttId = node.getId();
            vo.resource = node.getResource();
            vo.threadNum = node.getThreadNum();
            vo.passQps = node.getPassQps();
            vo.blockQps = node.getBlockQps();
            vo.totalQps = node.getTotalQps();
            vo.averageRt = node.getAverageRt();
            vo.exceptionQps = node.getExceptionQps();
            vo.oneMinutePass = node.getOneMinutePass();
            vo.oneMinuteBlock = node.getOneMinuteBlock();
            vo.oneMinuteException = node.getOneMinuteException();
            vo.oneMinuteTotal = node.getOneMinuteTotal();
            vo.visible = node.isVisible();
            list.add(vo);
        }
        for (ResourceTreeNode c : node.getChildren()) {
            visit(c, list, visible, false);
        }
    }

    public String getParentTtId() {
        return parentTtId;
    }

    public void setParentTtId(String parentTtId) {
        this.parentTtId = parentTtId;
    }

    public String getTtId() {
        return ttId;
    }

    public void setTtId(String ttId) {
        this.ttId = ttId;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public Long getPassQps() {
        return passQps;
    }

    public void setPassQps(Long passQps) {
        this.passQps = passQps;
    }

    public Long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(Long blockQps) {
        this.blockQps = blockQps;
    }

    public Long getTotalQps() {
        return totalQps;
    }

    public void setTotalQps(Long totalQps) {
        this.totalQps = totalQps;
    }

    public Long getAverageRt() {
        return averageRt;
    }

    public void setAverageRt(Long averageRt) {
        this.averageRt = averageRt;
    }

    public Long getPassRequestQps() {
        return passRequestQps;
    }

    public void setPassRequestQps(Long passRequestQps) {
        this.passRequestQps = passRequestQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public Long getOneMinuteException() {
        return oneMinuteException;
    }

    public void setOneMinuteException(Long oneMinuteException) {
        this.oneMinuteException = oneMinuteException;
    }

    public Long getOneMinutePass() {
        return oneMinutePass;
    }

    public void setOneMinutePass(Long oneMinutePass) {
        this.oneMinutePass = oneMinutePass;
    }

    public Long getOneMinuteBlock() {
        return oneMinuteBlock;
    }

    public void setOneMinuteBlock(Long oneMinuteBlock) {
        this.oneMinuteBlock = oneMinuteBlock;
    }

    public Long getOneMinuteTotal() {
        return oneMinuteTotal;
    }

    public void setOneMinuteTotal(Long oneMinuteTotal) {
        this.oneMinuteTotal = oneMinuteTotal;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
