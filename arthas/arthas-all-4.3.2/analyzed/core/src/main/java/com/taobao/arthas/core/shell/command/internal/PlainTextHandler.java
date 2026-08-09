package com.taobao.arthas.core.shell.command.internal;

import com.taobao.arthas.core.shell.cli.CliToken;
import com.taobao.text.util.RenderUtil;

import java.util.List;

/**
 * 管道 plaintext 处理器：将 ANSI 彩色终端输出转为纯文本。
 * <p>
 * 委托 {@link RenderUtil#ansiToPlainText} 剥离转义序列，便于重定向或 tee 到文件。
 *
 * @author beiwei30 on 20/12/2016.
 */
public class PlainTextHandler extends StdoutHandler {
    /** 管道子命令名，与 {@link StdoutHandler#inject} 路由表对应 */
    public static String NAME = "plaintext";

    /** 无额外参数，直接构造 PlainTextHandler */
    public static StdoutHandler inject(List<CliToken> tokens) {
        return new PlainTextHandler();
    }

    @Override
    /** 将输入中的 ANSI 控制码转为可读纯文本 */
    public String apply(String s) {
        return RenderUtil.ansiToPlainText(s);
    }
}
