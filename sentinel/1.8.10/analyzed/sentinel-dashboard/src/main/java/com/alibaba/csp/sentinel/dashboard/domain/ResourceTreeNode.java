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
package com.alibaba.csp.sentinel.dashboard.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.command.vo.NodeVo;

/**
 * 资源调用树节点，用于 Dashboard 展示实时 QPS、RT 等指标。
 * <p>由客户端 {@link NodeVo} 列表构建父子树，并支持关键字过滤可见性。
 *
 * @author leyou
 */
public class ResourceTreeNode {
    /** 节点唯一 id。 */
    private String id;
    /** 父节点 id，根节点为空。 */
    private String parentId;
    /** 资源名称。 */
    private String resource;

    private Integer threadNum;
    private Long passQps;
    private Long blockQps;
    private Long totalQps;
    private Long averageRt;
    private Long successQps;
    private Long exceptionQps;
    private Long oneMinutePass;
    private Long oneMinuteBlock;
    private Long oneMinuteException;
    private Long oneMinuteTotal;

    /** 搜索过滤后是否在 UI 中可见。 */
    private boolean visible = true;

    private List<ResourceTreeNode> children = new ArrayList<>();

    /** 将扁平 {@link NodeVo} 列表组装为单棵调用树根节点。 */
    public static ResourceTreeNode fromNodeVoList(List<NodeVo> nodeVos) {
        if (nodeVos == null || nodeVos.isEmpty()) {
            return null;
        }
        ResourceTreeNode root = null;
        Map<String, ResourceTreeNode> map = new HashMap<>();
        for (NodeVo vo : nodeVos) {
            ResourceTreeNode node = fromNodeVo(vo);
            map.put(node.id, node);
            // 无 parentId 的节点为调用树根。
            if (node.parentId == null || node.parentId.isEmpty()) {
                root = node;
            } else if (map.containsKey(node.parentId)) {
                map.get(node.parentId).children.add(node);
            } else {
                // 父节点尚未出现，正常不应发生。
            }
        }
        return root;
    }

    public static ResourceTreeNode fromNodeVo(NodeVo vo) {
        ResourceTreeNode node = new ResourceTreeNode();
        node.id = vo.getId();
        node.parentId = vo.getParentId();
        node.resource = vo.getResource();
        node.threadNum = vo.getThreadNum();
        node.passQps = vo.getPassQps();
        node.blockQps = vo.getBlockQps();
        node.totalQps = vo.getTotalQps();
        node.averageRt = vo.getAverageRt();
        node.successQps = vo.getSuccessQps();
        node.exceptionQps = vo.getExceptionQps();
        node.oneMinutePass = vo.getOneMinutePass();
        node.oneMinuteBlock = vo.getOneMinuteBlock();
        node.oneMinuteException = vo.getOneMinuteException();
        node.oneMinuteTotal = vo.getOneMinuteTotal();
        return node;
    }

    /** 对整棵树执行忽略大小写的资源名关键字过滤。 */
    public void searchIgnoreCase(String searchKey) {
        search(this, searchKey);
    }

    /**
     * 递归标记可见性：资源名匹配或任一子节点可见则本节点可见。
     */
    private boolean search(ResourceTreeNode node, String searchKey) {
        // 空关键字视为匹配全部节点。
        if (searchKey == null || searchKey.isEmpty() ||
            node.resource.toLowerCase().contains(searchKey.toLowerCase())) {
            node.visible = true;
        } else {
            node.visible = false;
        }

        boolean found = false;
        for (ResourceTreeNode c : node.children) {
            found |= search(c, searchKey);
        }
        node.visible |= found;
        return node.visible;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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

    public Long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(Long successQps) {
        this.successQps = successQps;
    }

    public Long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(Long exceptionQps) {
        this.exceptionQps = exceptionQps;
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

    public Long getOneMinuteException() {
        return oneMinuteException;
    }

    public void setOneMinuteException(Long oneMinuteException) {
        this.oneMinuteException = oneMinuteException;
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

    public List<ResourceTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<ResourceTreeNode> children) {
        this.children = children;
    }
}

