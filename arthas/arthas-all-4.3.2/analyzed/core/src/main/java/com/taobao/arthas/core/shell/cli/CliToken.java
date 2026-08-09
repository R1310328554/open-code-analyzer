package com.taobao.arthas.core.shell.cli;

/**
 * 命令行解析后的单个词法 Token（文本、空白或引号段）。
 * <p>
 * {@link #value()} 为规范化值；{@link #raw()} 保留原始转义形式供回放或高亮。
 */
public interface CliToken {
    /** @return 规范化后的 Token 文本（已处理引号与转义） */

    String value();

    /** @return 原始 Token 字符串，可能仍含未展开转义字符 */

    String raw();

    /** @return 是否为有效文本 Token（非纯空白） */

    boolean isText();

    /** @return 是否为空白分隔 Token */

    boolean isBlank();
}
