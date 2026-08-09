package com.taobao.arthas.core.command.model;

/**
 * JFR（Java Flight Recorder）命令的结构化结果：承载 jfr 子命令输出的文本片段。
 * <p>
 * {@link #setJfrOutput} 采用追加模式，便于流式或分块写入大段 JFR dump 摘要；
 * 初始值为空串，避免前端收到 null。
 *
 * @author xulong 2022/7/25
 */
public class JFRModel extends ResultModel {

    /** JFR 命令输出的累积文本（dump / start / stop 等子命令的 stdout） */
    private String jfrOutput = "";

    @Override
    public String getType() {
        return "jfr";
    }

    public String getJfrOutput() {
        return jfrOutput;
    }

    /** 追加一段输出；多次调用可拼接分块结果，而非覆盖 */
    public void setJfrOutput(String jfrOutput) {
        this.jfrOutput += jfrOutput;
    }
}
