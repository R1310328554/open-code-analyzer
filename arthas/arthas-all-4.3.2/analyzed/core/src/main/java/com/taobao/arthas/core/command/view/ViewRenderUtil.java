package com.taobao.arthas.core.command.view;

import com.taobao.arthas.core.command.model.ChangeResultVO;
import com.taobao.arthas.core.command.model.EnhancerAffectVO;
import com.taobao.arthas.core.command.model.ThreadVO;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.text.Color;
import com.taobao.text.Decoration;
import com.taobao.text.Style;
import com.taobao.text.ui.LabelElement;
import com.taobao.text.ui.Overflow;
import com.taobao.text.ui.RowElement;
import com.taobao.text.ui.TableElement;
import com.taobao.text.util.RenderUtil;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.taobao.text.ui.Element.label;
import static java.lang.String.format;

/**
 * 终端（TTY）视图渲染工具类。
 * <p>
 * 提供 KV 表格、配置变更对比、增强器影响摘要、线程列表等通用渲染方法，
 * 供各 {@link ResultView} 子类复用。
 *
 * @author gongdewei 2020/6/22
 */
public class ViewRenderUtil {

    /** 线程状态到终端颜色的映射，用于 thread 命令表格着色 */
    public static final EnumMap<Thread.State, Color> colorMapping = new EnumMap<Thread.State, Color>(Thread.State.class);
    static {
        colorMapping.put(Thread.State.NEW, Color.cyan);
        colorMapping.put(Thread.State.RUNNABLE, Color.green);
        colorMapping.put(Thread.State.BLOCKED, Color.red);
        colorMapping.put(Thread.State.WAITING, Color.yellow);
        colorMapping.put(Thread.State.TIMED_WAITING, Color.magenta);
        colorMapping.put(Thread.State.TERMINATED, Color.blue);
    }

    /**
     * 渲染 KEY/VALUE 两列表格。
     * @param map 键值对
     * @param width 终端宽度
     * @return 渲染后的字符串
     */
    public static String renderKeyValueTable(Map<String, String> map, int width) {
        TableElement table = new TableElement(1, 4).leftCellPadding(1).rightCellPadding(1);
        table.row(true, label("KEY").style(Decoration.bold.bold()), label("VALUE").style(Decoration.bold.bold()));

        for (Map.Entry<String, String> entry : map.entrySet()) {
            table.row(entry.getKey(), entry.getValue());
        }

        return RenderUtil.render(table, width);
    }

    /**
     * 渲染配置项修改前后对比表（NAME/BEFORE-VALUE/AFTER-VALUE）。
     * @param result 变更结果 VO
     * @return 表格元素
     */
    public static TableElement renderChangeResult(ChangeResultVO result) {
        TableElement table = new TableElement().leftCellPadding(1).rightCellPadding(1);
        table.row(true, label("NAME").style(Decoration.bold.bold()),
                label("BEFORE-VALUE").style(Decoration.bold.bold()),
                label("AFTER-VALUE").style(Decoration.bold.bold()));
        table.row(result.getName(), StringUtils.objectToString(result.getBeforeValue()),
                StringUtils.objectToString(result.getAfterValue()));
        return table;
    }

    /**
     * 渲染字节码增强影响摘要：dump 文件、受影响方法、类/方法计数与 listenerId。
     * @param affectVO 增强器影响 VO
     * @return 多行文本
     */
    public static String renderEnhancerAffect(EnhancerAffectVO affectVO) {
        final StringBuilder infoSB = new StringBuilder();
        List<String> classDumpFiles = affectVO.getClassDumpFiles();
        if (classDumpFiles != null) {
            for (String classDumpFile : classDumpFiles) {
                infoSB.append("[dump: ").append(classDumpFile).append("]\n");
            }
        }

        List<String> methods = affectVO.getMethods();
        if (methods != null) {
            for (String method : methods) {
                infoSB.append("[Affect method: ").append(method).append("]\n");
            }
        }

        infoSB.append(format("Affect(class count: %d , method count: %d) cost in %s ms, listenerId: %d",
                affectVO.getClassCount(),
                affectVO.getMethodCount(),
                affectVO.getCost(),
                affectVO.getListenerId()));
        if (!StringUtils.isEmpty(affectVO.getOverLimitMsg())) {
            infoSB.append("\n" + affectVO.getOverLimitMsg());
        }
        if (affectVO.getThrowable() != null) {
            infoSB.append("\nEnhance error! exception: ").append(affectVO.getThrowable());
        }
        infoSB.append("\n");

        return infoSB.toString();
    }

    /**
     * 渲染线程列表表格，含 ID/NAME/STATE/%CPU 等列；
     * 非守护线程以洋红色标注，状态列按 colorMapping 着色。
     */
    public static String drawThreadInfo(List<ThreadVO> threads, int width, int height) {
        TableElement table = new TableElement(1, 6, 3, 2, 2, 2, 2, 2, 2, 2).overflow(Overflow.HIDDEN).rightCellPadding(1);

        // 表头行：加粗白底黑字
        table.add(
                new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add(
                        "ID",
                        "NAME",
                        "GROUP",
                        "PRIORITY",
                        "STATE",
                        "%CPU",
                        "DELTA_TIME",
                        "TIME",
                        "INTERRUPTED",
                        "DAEMON"
                )
        );

        int count = 0;
        for (ThreadVO thread : threads) {
            Color color = colorMapping.get(thread.getState());
            String time = formatTimeMills(thread.getTime());
            String deltaTime = formatTimeMillsToSeconds(thread.getDeltaTime());
            double cpu = thread.getCpu();

            LabelElement daemonLabel = new LabelElement(thread.isDaemon());
            if (!thread.isDaemon()) {
                daemonLabel.setStyle(Style.style(Color.magenta));
            }
            LabelElement stateElement;
            if (thread.getState() != null) {
                stateElement = new LabelElement(thread.getState()).style(color.fg());
            } else {
                stateElement = new LabelElement("-");
            }
            table.row(
                    new LabelElement(thread.getId()),
                    new LabelElement(thread.getName()),
                    new LabelElement(thread.getGroup() != null ? thread.getGroup() : "-"),
                    new LabelElement(thread.getPriority()),
                    stateElement,
                    new LabelElement(cpu),
                    new LabelElement(deltaTime),
                    new LabelElement(time),
                    new LabelElement(thread.isInterrupted()),
                    daemonLabel
            );
            // 超出可见行数时截断，避免刷屏
            if (++count >= height) {
                break;
            }
        }
        return RenderUtil.render(table, width, height);
    }

    /** 将毫秒格式化为 min:sec.mmm 字符串（避免 String.format 开销） */
    private static String formatTimeMills(long timeMills) {
        long seconds = timeMills / 1000;
        long mills = timeMills % 1000;
        long min = seconds / 60;
        seconds = seconds % 60;

        //return String.format("%d:%d.%03d", min, seconds, mills);
        String str;
        if (mills >= 100) {
            str = min + ":" + seconds + "." + mills;
        } else if (mills >= 10) {
            str = min + ":" + seconds + ".0" + mills;
        } else {
            str = min + ":" + seconds + ".00" + mills;
        }
        return str;
    }

    /** 将毫秒格式化为 sec.mmm，用于 DELTA_TIME 列 */
    private static String formatTimeMillsToSeconds(long timeMills) {
        long seconds = timeMills / 1000;
        long mills = timeMills % 1000;

        //return String.format("%d.%03d", seconds, mills);
        String str;
        if (mills >= 100) {
            str = seconds + "." + mills;
        } else if (mills >= 10) {
            str = seconds + ".0" + mills;
        } else {
            str = seconds + ".00" + mills;
        }
        return str;
    }
}
