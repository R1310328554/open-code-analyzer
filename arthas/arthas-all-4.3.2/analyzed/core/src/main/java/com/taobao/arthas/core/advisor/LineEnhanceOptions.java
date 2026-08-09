package com.taobao.arthas.core.advisor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.alibaba.bytekit.asm.location.LineDuplicatePolicy;
import com.alibaba.bytekit.asm.location.LineMode;

/**
 * line 命令的行号增强配置：目标行集合、方法描述符及 ByteKit 行匹配模式。
 */
public class LineEnhanceOptions {
    /** 需要插桩的源码行号集合 */
    private final Set<Integer> lines;
    /** 限定方法 ASM 描述符，空表示不限 */
    private final String methodDesc;
    /** 行号匹配模式（如 FRAME_AWARE） */
    private final LineMode mode;
    /** 同一行多次命中时的去重策略 */
    private final LineDuplicatePolicy duplicatePolicy;

    public LineEnhanceOptions(Set<Integer> lines, String methodDesc) {
        this(lines, methodDesc, LineMode.FRAME_AWARE, LineDuplicatePolicy.DEFAULT);
    }

    public LineEnhanceOptions(Set<Integer> lines, String methodDesc, LineMode mode,
            LineDuplicatePolicy duplicatePolicy) {
        this.lines = Collections.unmodifiableSet(new LinkedHashSet<Integer>(lines));
        this.methodDesc = methodDesc;
        this.mode = mode == null ? LineMode.FRAME_AWARE : mode;
        this.duplicatePolicy = duplicatePolicy == null ? LineDuplicatePolicy.DEFAULT : duplicatePolicy;
    }

    public Set<Integer> getLines() {
        return lines;
    }

    /** 返回不可变行号列表副本，供 LineLocationMatcher 使用 */
    public List<Integer> getLineList() {
        return new ArrayList<Integer>(lines);
    }

    public String getMethodDesc() {
        return methodDesc;
    }

    public LineMode getMode() {
        return mode;
    }

    public LineDuplicatePolicy getDuplicatePolicy() {
        return duplicatePolicy;
    }
}
