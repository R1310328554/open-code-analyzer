package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.MethodNode;
import com.taobao.arthas.core.command.model.ThreadNode;
import com.taobao.arthas.core.command.model.ThrowNode;
import com.taobao.arthas.core.command.model.TraceModel;
import com.taobao.arthas.core.command.model.TraceNode;
import com.taobao.arthas.core.shell.command.CommandProcess;
import com.taobao.arthas.core.util.DateUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.view.Ansi;

import java.util.List;

import static java.lang.String.format;

/**
 * {@code trace} 命令的终端渲染视图。
 * <p>
 * 将 {@link TraceModel} 中的调用树以 ASCII 树形结构输出，展示方法耗时、行号、
 * 线程上下文及异常节点；耗时最高的方法节点以红色高亮。
 *
 * @author gongdewei 2020/4/29
 */
public class TraceView extends ResultView<TraceModel> {
    /** 树形末节点前缀字符 */
    private static final String STEP_FIRST_CHAR = "`---";
    /** 树形中间节点前缀字符 */
    private static final String STEP_NORMAL_CHAR = "+---";
    private static final String STEP_HAS_BOARD = "|   ";
    private static final String STEP_EMPTY_BOARD = "    ";
    private static final String TIME_UNIT = "ms";
    private static final char PERCENTAGE = '%';

    // 是否输出耗时
    private boolean isPrintCost = true;
    private MethodNode maxCostNode;

    @Override
    public void draw(CommandProcess process, TraceModel result) {
        process.write(drawTree(result.getRoot())).write("\n");
    }

    public String drawTree(TraceNode root) {

        // 每次绘制前重置，重新查找最大耗时节点
        maxCostNode = null;
        findMaxCostNode(root);

        final StringBuilder treeSB = new StringBuilder(2048);

        final Ansi highlighted = Ansi.ansi().fg(Ansi.Color.RED);

        recursive(0, true, "", root, new Callback() {

            @Override
            public void callback(int deep, boolean isLast, String prefix, TraceNode node) {
                treeSB.append(prefix).append(isLast ? STEP_FIRST_CHAR : STEP_NORMAL_CHAR);
                renderNode(treeSB, node, highlighted);
                if (!StringUtils.isBlank(node.getMark())) {
                    treeSB.append(" [").append(node.getMark()).append(node.marks() > 1 ? "," + node.marks() : "").append("]");
                }
                treeSB.append("\n");
            }

        });

        return treeSB.toString();
    }

    private void renderNode(StringBuilder sb, TraceNode node, Ansi highlighted) {
        // 渲染耗时标签，格式如 [12.34% 0.37ms ] 或 [0.37ms]
        if (isPrintCost && node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;

            String costStr = renderCost(methodNode);
            if (node == maxCostNode) {
                // 全局最大耗时节点用 ANSI 红色高亮
                sb.append(highlighted.a(costStr).reset().toString());
            } else {
                sb.append(costStr);
            }
        }

        // 按节点类型分别渲染：方法、线程上下文或异常
        if (node instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) node;
            //clazz.getName() + ":" + method.getName() + "()"
            sb.append(methodNode.getClassName()).append(":").append(methodNode.getMethodName()).append("()");
            // #lineNumber
            if (methodNode.getLineNumber()!= -1) {
                sb.append(" #").append(methodNode.getLineNumber());
            }
        } else if (node instanceof ThreadNode) {
            // 线程根节点：输出 ts/name/id/daemon/priority/TCCL 等上下文
            ThreadNode threadNode = (ThreadNode) node;
            //ts=2020-04-29 10:34:00;thread_name=main;id=1;is_daemon=false;priority=5;TCCL=sun.misc.Launcher$AppClassLoader@18b4aac2
            sb.append(format("ts=%s;thread_name=%s;id=%d;is_daemon=%s;priority=%d;TCCL=%s",
                    DateUtils.formatDateTime(threadNode.getTimestamp()),
                    threadNode.getThreadName(),
                    threadNode.getThreadId(),
                    threadNode.isDaemon(),
                    threadNode.getPriority(),
                    threadNode.getClassloader()));

            // 可选分布式 trace_id / rpc_id 扩展字段
            if (threadNode.getTraceId() != null) {
                sb.append(";trace_id=").append(threadNode.getTraceId());
            }
            if (threadNode.getRpcId() != null) {
                sb.append(";rpc_id=").append(threadNode.getRpcId());
            }
        } else if (node instanceof ThrowNode) {
            ThrowNode throwNode = (ThrowNode) node;
            sb.append("throw:").append(throwNode.getException())
                    .append(" #").append(throwNode.getLineNumber())
                    .append(" [").append(throwNode.getMessage()).append("]");

        } else {
            throw new UnsupportedOperationException("unknown trace node: " + node.getClass());
        }
    }

    private String renderCost(MethodNode node) {
        StringBuilder sb = new StringBuilder();
        if (node.getTimes() <= 1) {
            if(node.parent() instanceof ThreadNode) {
                sb.append('[').append(nanoToMillis(node.getCost())).append(TIME_UNIT).append("] ");
            }else {
                MethodNode parentNode = (MethodNode) node.parent();
                String percentage = String.format("%.2f", node.getCost()*100.0/parentNode.getTotalCost());
                sb.append('[').append(percentage).append(PERCENTAGE).append(" ").append(nanoToMillis(node.getCost())).append(TIME_UNIT).append(" ").append("] ");

            }
        } else {
            if(node.parent() instanceof ThreadNode) {
                sb.append("[min=").append(nanoToMillis(node.getMinCost())).append(TIME_UNIT).append(",max=")
                        .append(nanoToMillis(node.getMaxCost())).append(TIME_UNIT).append(",total=")
                        .append(nanoToMillis(node.getTotalCost())).append(TIME_UNIT).append(",count=")
                        .append(node.getTimes()).append("] ");
            }else {
                MethodNode parentNode = (MethodNode) node.parent();
                String percentage = String.format("%.2f",node.getTotalCost()*100.0/parentNode.getTotalCost());
                sb.append('[').append(percentage).append(PERCENTAGE).append(" min=").append(nanoToMillis(node.getMinCost())).append(TIME_UNIT).append(",max=")
                        .append(nanoToMillis(node.getMaxCost())).append(TIME_UNIT).append(",total=")
                        .append(nanoToMillis(node.getTotalCost())).append(TIME_UNIT).append(",count=")
                        .append(node.getTimes()).append("] ");
            }

        }
        return sb.toString();
    }

    /**
     * 递归遍历
     */
    private void recursive(int deep, boolean isLast, String prefix, TraceNode node, Callback callback) {
        callback.callback(deep, isLast, prefix, node);
        if (!isLeaf(node)) {
            List<TraceNode> children = node.getChildren();
            if (children == null) {
                return;
            }
            final int size = children.size();
            for (int index = 0; index < size; index++) {
                final boolean isLastFlag = index == size - 1;
                final String currentPrefix = isLast ? prefix + STEP_EMPTY_BOARD : prefix + STEP_HAS_BOARD;
                recursive(
                        deep + 1,
                        isLastFlag,
                        currentPrefix,
                        children.get(index),
                        callback
                );
            }
        }
    }

    /**
     * 查找耗时最大的节点，便于后续高亮展示
     * @param node
     */
    private void findMaxCostNode(TraceNode node) {
        if (node instanceof MethodNode && !isRoot(node) && !isRoot(node.parent())) {
            MethodNode aNode = (MethodNode) node;
            if (maxCostNode == null || maxCostNode.getTotalCost() < aNode.getTotalCost()) {
                maxCostNode = aNode;
            }
        }
        if (!isLeaf(node)) {
            List<TraceNode> children = node.getChildren();
            if (children != null) {
                for (TraceNode n: children) {
                    findMaxCostNode(n);
                }
            }
        }
    }

    private boolean isRoot(TraceNode node) {
        return node.parent() == null;
    }

    private boolean isLeaf(TraceNode node) {
        List<TraceNode> children = node.getChildren();
        return children == null || children.isEmpty();
    }


    /** 纳秒转毫秒，保留小数精度供终端展示 */

    double nanoToMillis(long nanoSeconds) {
        return nanoSeconds / 1000000.0;
    }

    /**
     * 遍历回调接口
     */
    private interface Callback {

        void callback(int deep, boolean isLast, String prefix, TraceNode node);

    }
}
