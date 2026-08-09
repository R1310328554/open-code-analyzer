package com.taobao.arthas.core.shell.command.internal;

import com.taobao.arthas.core.shell.cli.CliToken;
import io.termd.core.function.Function;

import java.util.LinkedList;
import java.util.List;

/**
 * Shell 管道 stdout 处理器抽象基类，实现 termd {@link Function}。
 * <p>
 * {@link #inject} 根据管道首 Token 分发到 grep、plaintext、wc、tee 等具体 Handler；
 * 默认 {@link #apply} 为恒等传递。
 *
 * @author beiwei30 on 20/12/2016.
 */
public abstract class StdoutHandler implements Function<String, String> {

    /**
     * 解析管道 Token 链，返回第一个子命令对应的 Handler。
     * @return 匹配的 Handler，无法识别时返回 null
     */
    public static StdoutHandler inject(List<CliToken> tokens) {
        CliToken firstTextToken = null;
        for (CliToken token : tokens) {
            if (token.isText()) {
                firstTextToken = token;
                break;
            }
        }

        if (firstTextToken == null) {
            return null;
        }

        // 按子命令名路由到各 Handler 的 inject 工厂
        if (firstTextToken.value().equals(GrepHandler.NAME)) {
            return GrepHandler.inject(tokens);
        } else if (firstTextToken.value().equals(PlainTextHandler.NAME)) {
            return PlainTextHandler.inject(tokens);
        } else if (firstTextToken.value().equals(WordCountHandler.NAME)) {
            return WordCountHandler.inject(tokens);
        } else if (firstTextToken.value().equals(TeeHandler.NAME)){
            return TeeHandler.inject(tokens);
        } else{
            return null;
        }
    }

    /** 提取 command 名称之后所有文本 Token 作为 CLI 参数列表 */
    public static List<String> parseArgs(List<CliToken> tokens, String command) {
        List<String> args = new LinkedList<String>();
        boolean found = false;
        for (CliToken token : tokens) {
            if (token.isText() && token.value().equals(command)) {
                found = true;
            } else if (token.isText() && found) {
                args.add(token.value());
            }
        }
        return args;
    }

    @Override
    /** 默认透传输入，子类覆盖以实现过滤或重定向 */
    public String apply(String s) {
        return s;
    }
}
