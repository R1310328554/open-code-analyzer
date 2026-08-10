package org.keycloak.client.admin.cli.v2;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import picocli.AutoComplete;
import picocli.CommandLine;

/**
 * 处理 {@code __complete} 请求的动态 Shell 补全。
 * <p>
 * 委托 PicoCLI {@link AutoComplete#complete} 解析候选；对单独的 {@code --} 前缀做特殊处理。
 */
public class KcAdmV2Completer {

    private static final String LONG_OPTION_PREFIX = "--";

    /** 使用默认缓存目录构建 CLI 并补全。 */
    public static void complete(String[] args, PrintWriter out) {
        completeWith(buildCli(new KcAdmV2Cmd(args)), args, out);
    }

    /** 使用指定描述符缓存目录构建 CLI 并补全。 */
    public static void complete(String[] args, PrintWriter out, Path cacheDir) {
        completeWith(buildCli(new KcAdmV2Cmd(cacheDir, args)), args, out);
    }

    private static CommandLine buildCli(KcAdmV2Cmd rootCmd) {
        CommandLine cli = new CommandLine(rootCmd);
        rootCmd.configureCommandLine(cli);
        return cli;
    }

    private static void completeWith(CommandLine cli, String[] args, PrintWriter out) {
        String partial = args.length > 0 ? args[args.length - 1] : "";

        if (LONG_OPTION_PREFIX.equals(partial)) {
            completeLongOptions(cli, args, out);
        } else {
            completePicocli(cli, args, partial, out);
        }

        out.flush();
    }

    private static void completePicocli(CommandLine cli, String[] args, String partial, PrintWriter out) {
        int cursor = 0;
        for (String arg : args) {
            cursor += arg.length() + 1;
        }
        if (cursor > 0) {
            cursor--;
        }

        int argIndex = Math.max(0, args.length - 1);
        int posInArg = partial.length();

        List<CharSequence> candidates = new ArrayList<>();
        AutoComplete.complete(cli.getCommandSpec(), args, argIndex, posInArg, cursor, candidates);

        for (CharSequence candidate : candidates) {
            out.println(partial + candidate);
        }
    }

    // PicoCLI 的 AutoComplete 将 "--" 视为歧义（选项结束 vs 部分选项名），
    // 此处自行列出当前命令的全部长选项
    private static void completeLongOptions(CommandLine cli, String[] args, PrintWriter out) {
        CommandLine current = cli;
        for (int i = 0; i < args.length - 1; i++) {
            CommandLine sub = current.getSubcommands().get(args[i]);
            if (sub == null) {
                break;
            }
            current = sub;
        }

        for (var opt : current.getCommandSpec().options()) {
            if (opt.hidden() && !opt.usageHelp()) {
                continue;
            }
            for (String name : opt.names()) {
                if (name.startsWith(LONG_OPTION_PREFIX)) {
                    out.println(name);
                }
            }
        }
    }
}
