package com.taobao.arthas.core.view;

import com.taobao.arthas.core.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 树形命令行视图控件。
 * <p>
 * 通过 {@link #begin(String)} / {@link #end()} 构建调用树，
 * 支持按节点统计耗时并以 ASCII 字符绘制层级结构；
 * 可选高亮全局耗时最大的分支节点。
 * Created by vlinux on 15/5/26.
 */
public class TreeView implements View {

    /** 最后一个子节点的连接符前缀。 */
    private static final String STEP_FIRST_CHAR = "`---";
    private static final String STEP_NORMAL_CHAR = "+---";
    private static final String STEP_HAS_BOARD = "|   ";
    private static final String STEP_EMPTY_BOARD = "    ";
    private static final String TIME_UNIT = "ms";

    // 是否输出耗时
    private final boolean isPrintCost;

    // 根节点
    private final Node root;

    // 当前节点
    private Node current;

    // 最耗时的节点
    private Node maxCost;


    /**
     * 创建带标题的根节点。
     *
     * @param isPrintCost 是否在节点旁输出耗时统计
     * @param title 根节点显示文本
     */
    public TreeView(boolean isPrintCost, String title) {
        this.root = new Node(title).markBegin().markEnd();
        this.current = root;
        this.isPrintCost = isPrintCost;
    }

    /** 递归渲染整棵树并返回多行文本。 */
    @Override
    public String draw() {

        findMaxCostNode(root);

        final StringBuilder treeSB = new StringBuilder();

        final Ansi highlighted = Ansi.ansi().fg(Ansi.Color.RED);

        recursive(0, true, "", root, new Callback() {

            @Override
            public void callback(int deep, boolean isLast, String prefix, Node node) {
                treeSB.append(prefix).append(isLast ? STEP_FIRST_CHAR : STEP_NORMAL_CHAR);
                if (isPrintCost && !node.isRoot()) {
                    if (node == maxCost) {
                        // 全局最大耗时节点以红色高亮
                        treeSB.append(highlighted.a(node.toString()).reset().toString());
                    } else {
                        treeSB.append(node.toString());
                    }
                }
                treeSB.append(node.data);
                if (!StringUtils.isBlank(node.mark)) {
                    treeSB.append(" [").append(node.mark).append(node.marks > 1 ? "," + node.marks : "").append("]");
                }
                treeSB.append("\n");
            }

        });

        return treeSB.toString();
    }

    /**
     * 深度优先递归遍历子树，通过回调逐行输出前缀与节点内容。
     *
     * @param deep 当前深度
     * @param isLast 当前节点是否为同级最后一个
     * @param prefix 已累积的 ASCII 前缀
     * @param node 待遍历节点
     * @param callback 每访问一个节点时触发的回调
     */
    private void recursive(int deep, boolean isLast, String prefix, Node node, Callback callback) {
        callback.callback(deep, isLast, prefix, node);
        if (!node.isLeaf()) {
            final int size = node.children.size();
            for (int index = 0; index < size; index++) {
                final boolean isLastFlag = index == size - 1;
                final String currentPrefix = isLast ? prefix + STEP_EMPTY_BOARD : prefix + STEP_HAS_BOARD;
                recursive(
                        deep + 1,
                        isLastFlag,
                        currentPrefix,
                        node.children.get(index),
                        callback
                );
            }
        }
    }

    /**
     * 后序遍历查找 totalCost 最大的非根、非根子节点，供 draw 高亮。
     *
     * @param node 起始节点（通常为 root）
     */
    private void findMaxCostNode(Node node) {
        if (!node.isRoot() && !node.parent.isRoot()) {
            if (maxCost == null) {
                maxCost = node;
            } else if (maxCost.totalCost < node.totalCost) {
                maxCost = node;
            }
        }
        if (!node.isLeaf()) {
            for (Node n: node.children) {
                findMaxCostNode(n);
            }
        }
    }


    /**
     * 创建一个分支节点
     *
     * @param data 分支节点标签；若同名子节点已存在则复用
     * @return this，支持链式调用
     */
    public TreeView begin(String data) {
        Node n = current.find(data);
        if (n != null) {
            current = n;
        } else {
            current = new Node(current, data);
        }
        current.markBegin();
        return this;
    }

    /**
     * 结束一个分支节点
     *
     * @return this，当前指针回退到父节点
     * @throws IllegalStateException 当前已在根节点时调用
     */
    public TreeView end() {
        if (current.isRoot()) {
            throw new IllegalStateException("current node is root.");
        }
        current.markEnd();
        current = current.parent;
        return this;
    }

    /**
     * 结束当前分支并附加备注标记（如异常信息）。
     *
     * @param mark 备注文本，多次标记会累加计数
     * @return this
     * @throws IllegalStateException 当前已在根节点时调用
     */
    public TreeView end(String mark) {
        if (current.isRoot()) {
            throw new IllegalStateException("current node is root.");
        }
        current.markEnd().mark(mark);
        current = current.parent;
        return this;
    }


    /** 内部树节点，维护父子关系、耗时统计与备注。 */

    private static class Node {

        /**
         * 父节点
         */
        final Node parent;

        /**
         * 节点数据
         */
        final String data;

        /**
         * 子节点
         */
        final List<Node> children = new ArrayList<Node>();

        final Map<String, Node> map = new HashMap<String, Node>();

        /**
         * 开始时间戳
         */
        private long beginTimestamp;

        /**
         * 结束时间戳
         */
        private long endTimestamp;

        /**
         * 备注
         */
        private String mark;

        /**
         * 构造树节点(根节点)
         */
        private Node(String data) {
            this.parent = null;
            this.data = data;
        }

        /**
         * 构造树节点
         *
         * @param parent 父节点
         * @param data   节点数据
         */
        private Node(Node parent, String data) {
            this.parent = parent;
            this.data = data;
            parent.children.add(this);
            parent.map.put(data, this);
        }

        /**
         * 查找已经存在的节点
         */
        Node find(String data) {
            return map.get(data);
        }

        /**
         * 是否根节点
         *
         * @return true / false
         */
        boolean isRoot() {
            return null == parent;
        }

        /**
         * 是否叶子节点
         *
         * @return true / false
         */
        boolean isLeaf() {
            return children.isEmpty();
        }

        /** 记录进入节点时的纳秒时间戳。 */
        Node markBegin() {
            beginTimestamp = System.nanoTime();
            return this;
        }

        /** 记录离开时间并累加 min/max/total 耗时与调用次数。 */
        Node markEnd() {
            endTimestamp = System.nanoTime();

            long cost = getCost();
            if (cost < minCost) {
                minCost = cost;
            }
            if (cost > maxCost) {
                maxCost = cost;
            }
            times++;
            totalCost += cost;

            return this;
        }

        /** 为节点追加备注并递增标记计数。 */
        Node mark(String mark) {
            this.mark = mark;
            marks++;
            return this;
        }

        /** 本次 begin/end 区间的纳秒耗时。 */
        long getCost() {
            return endTimestamp - beginTimestamp;
        }

        /** 将纳秒转换为毫秒，供 toString 展示。 */

        double getCostInMillis(long nanoSeconds) {
            return nanoSeconds / 1000000.0;
        }

        /** 格式化耗时：单次调用显示区间耗时，多次调用显示 min/max/total/count。 */
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (times <= 1) {
                sb.append("[").append(getCostInMillis(getCost())).append(TIME_UNIT).append("] ");
            } else {
                sb.append("[min=").append(getCostInMillis(minCost)).append(TIME_UNIT).append(",max=")
                        .append(getCostInMillis(maxCost)).append(TIME_UNIT).append(",total=")
                        .append(getCostInMillis(totalCost)).append(TIME_UNIT).append(",count=")
                        .append(times).append("] ");
            }
            return sb.toString();
        }

        /**
         * 合并统计相同调用,并计算最小\最大\总耗时
         */
        private long minCost = Long.MAX_VALUE;
        private long maxCost = Long.MIN_VALUE;
        private long totalCost = 0;
        private long times = 0;
        private long marks = 0;
    }


    /** draw 过程中每访问一个节点时触发的回调接口。 */

    private interface Callback {

        void callback(int deep, boolean isLast, String prefix, Node node);

    }

}
