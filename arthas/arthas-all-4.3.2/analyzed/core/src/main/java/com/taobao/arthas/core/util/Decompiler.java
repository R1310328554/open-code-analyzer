package com.taobao.arthas.core.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.benf.cfr.reader.api.CfrDriver;
import org.benf.cfr.reader.api.OutputSinkFactory;
import org.benf.cfr.reader.api.SinkReturns.LineNumberMapping;

import com.taobao.arthas.common.Pair;

/**
 * 基于 CFR 的 class 文件反编译器封装。
 * <p>
 * 支持按方法名局部反编译、隐藏 Unicode 转义、以及字节码行号到源码行号的映射，
 * 供 {@code jad} 等命令将 .class 还原为可读 Java 源码。
 *
 * @author hengyunabc 2018-11-16
 */
public class Decompiler {

    /** 反编译整个 class 文件（可选限定单个方法名） */
    public static String decompile(String classFilePath, String methodName) {
        return decompile(classFilePath, methodName, false);
    }

    /** 反编译并可选择将 Unicode 字面量隐藏为转义形式 */
    public static String decompile(String classFilePath, String methodName, boolean hideUnicode) {
        return decompile(classFilePath, methodName, hideUnicode, true);
    }

    /**
     * 反编译并返回源码与行号映射。
     * <p>
     * 通过自定义 {@link OutputSinkFactory} 收集 CFR 输出，同时构建反编译行号到
     * class 文件行号的 {@link NavigableMap}，便于与 trace/watch 等命令对齐。
     *
     * @param classFilePath .class 文件路径
     * @param methodName 仅反编译指定方法名，空则反编译整个类
     * @param hideUnicode 是否隐藏 UTF 字符
     * @param printLineNumber 是否在输出左侧打印源码行号注释
     * @return Pair：反编译文本与行号映射
     */
    public static Pair<String, NavigableMap<Integer, Integer>> decompileWithMappings(String classFilePath,
            String methodName, boolean hideUnicode, boolean printLineNumber) {
        final StringBuilder sb = new StringBuilder(8192);

        final NavigableMap<Integer, Integer> lineMapping = new TreeMap<Integer, Integer>();

        OutputSinkFactory mySink = new OutputSinkFactory() {
            @Override
            public List<SinkClass> getSupportedSinks(SinkType sinkType, Collection<SinkClass> collection) {
                return Arrays.asList(SinkClass.STRING, SinkClass.DECOMPILED, SinkClass.DECOMPILED_MULTIVER,
                        SinkClass.EXCEPTION_MESSAGE, SinkClass.LINE_NUMBER_MAPPING);
            }

            @Override
            public <T> Sink<T> getSink(final SinkType sinkType, final SinkClass sinkClass) {
                return new Sink<T>() {
                    @Override
                    public void write(T sinkable) {
                        // 跳过进度类消息，如 "Analysing type demo.MathGame"
                        if (sinkType == SinkType.PROGRESS) {
                            return;
                        }
                        if (sinkType == SinkType.LINENUMBER) {
                            LineNumberMapping mapping = (LineNumberMapping) sinkable;
                            NavigableMap<Integer, Integer> classFileMappings = mapping.getClassFileMappings();
                            NavigableMap<Integer, Integer> mappings = mapping.getMappings();
                            if (classFileMappings != null && mappings != null) {
                                for (Entry<Integer, Integer> entry : mappings.entrySet()) {
                                    Integer srcLineNumber = classFileMappings.get(entry.getKey());
                                    lineMapping.put(entry.getValue(), srcLineNumber);
                                }
                            }
                            return;
                        }
                        sb.append(sinkable);
                    }
                };
            }
        };

        HashMap<String, String> options = new HashMap<String, String>();
        /**
         * @see org.benf.cfr.reader.util.MiscConstants.Version#getVersion() Currently,
         *      the cfr version is wrong. so disable show cfr version.
         */
        options.put("showversion", "false");
        options.put("hideutf", String.valueOf(hideUnicode));
        options.put("trackbytecodeloc", "true");
        if (!StringUtils.isBlank(methodName)) {
            options.put("methodname", methodName);
        }

        CfrDriver driver = new CfrDriver.Builder().withOptions(options).withOutputSink(mySink).build();
        List<String> toAnalyse = new ArrayList<String>();
        toAnalyse.add(classFilePath);
        driver.analyse(toAnalyse);

        String resultCode = sb.toString();
        if (printLineNumber && !lineMapping.isEmpty()) {
            resultCode = addLineNumber(resultCode, lineMapping);
        }

        return Pair.make(resultCode, lineMapping);
    }

    /** 反编译并返回源码字符串（内部调用 {@link #decompileWithMappings}） */
    public static String decompile(String classFilePath, String methodName, boolean hideUnicode,
            boolean printLineNumber) {
        return decompileWithMappings(classFilePath, methodName, hideUnicode, printLineNumber).getFirst();
    }

    /**
     * 在反编译结果每行前插入 {@code /* N *\/} 形式的源码行号注释。
     *
     * @param src 反编译原文
     * @param lineMapping 反编译行索引（1-based）到 class 源码行号的映射
     * @return 带行号前缀的多行字符串
     */
    private static String addLineNumber(String src, Map<Integer, Integer> lineMapping) {
        int maxLineNumber = 0;
        for (Integer value : lineMapping.values()) {
            if (value != null && value > maxLineNumber) {
                maxLineNumber = value;
            }
        }

        String formatStr = "/*%2d*/ ";
        String emptyStr = "       ";

        StringBuilder sb = new StringBuilder();

        List<String> lines = StringUtils.toLines(src);

        // 按最大行号位数动态调整注释宽度
        if (maxLineNumber >= 1000) {
            formatStr = "/*%4d*/ ";
            emptyStr = "         ";
        } else if (maxLineNumber >= 100) {
            formatStr = "/*%3d*/ ";
            emptyStr = "        ";
        }

        int index = 0;
        for (String line : lines) {
            Integer srcLineNumber = lineMapping.get(index + 1);
            if (srcLineNumber != null) {
                sb.append(String.format(formatStr, srcLineNumber));
            } else {
                sb.append(emptyStr);
            }
            sb.append(line).append("\n");
            index++;
        }

        return sb.toString();
    }

}
