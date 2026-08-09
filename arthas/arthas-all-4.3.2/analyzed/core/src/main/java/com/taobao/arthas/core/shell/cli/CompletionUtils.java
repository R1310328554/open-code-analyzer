package com.taobao.arthas.core.shell.cli;

import com.taobao.arthas.core.shell.session.Session;
import com.taobao.arthas.core.shell.term.Tty;
import com.taobao.arthas.core.util.SearchUtils;
import com.taobao.arthas.core.util.StringUtils;
import com.taobao.arthas.core.util.matcher.Matcher;
import com.taobao.arthas.core.util.usage.StyledUsageFormatter;
import com.taobao.middleware.cli.CLI;
import com.taobao.middleware.cli.Option;
import com.taobao.middleware.cli.annotations.CLIConfigurator;
import io.termd.core.util.Helper;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Shell Tab 补全工具集：选项、类名、方法名、文件路径及前缀匹配。
 * <p>
 * 与 {@link Completion}、{@link OptionCompleteHandler} 配合，为各诊断命令提供上下文感知补全。
 *
 * @author beiwei30 on 09/11/2016.
 */
public class CompletionUtils {

    /**
     * 计算候选字符串集合的最长公共前缀（按 Unicode 码点）。
     *
     * @param values 候选项集合
     * @return 最长公共前缀
     */
    public static String findLongestCommonPrefix(Collection<String> values) {
        List<int[]> entries = new LinkedList<int[]>();
        for (String value : values) {
            int[] entry = Helper.toCodePoints(value);
            entries.add(entry);
        }
        return Helper.fromCodePoints(io.termd.core.readline.Completion.findLongestCommonPrefix(entries));
    }

    /**
     * 根据命令类上的 CLI 注解补全长/短选项或使用说明。
     *
     * @param completion 补全上下文
     * @param clazz 带 {@link com.taobao.middleware.cli.annotations} 的命令类
     */
    public static void complete(Completion completion, Class<?> clazz) {
        List<CliToken> tokens = completion.lineTokens();
        CliToken lastToken = tokens.get(tokens.size() - 1);
        CLI cli = CLIConfigurator.define(clazz);
        List<com.taobao.middleware.cli.Option> options = cli.getOptions();
        if (lastToken == null || lastToken.isBlank()) {
            completeUsage(completion, cli);
        } else if (lastToken.value().startsWith("--")) {
            completeLongOption(completion, lastToken, options);
        } else if (lastToken.value().startsWith("-")) {
            completeShortOption(completion, lastToken, options);
        } else {
            completion.complete(Collections.<String>emptyList());
        }
    }

    /**
     * 在 searchScope 中按前缀匹配并补全。
     *
     * @param completion 补全上下文
     * @param searchScope 候选名称集合
     * @return 始终 true（表示已尝试补全）
     */
    public static boolean complete(Completion completion, Collection<String> searchScope) {
        List<CliToken> tokens = completion.lineTokens();
        String lastToken = tokens.get(tokens.size() - 1).value();
        List<String> candidates = new ArrayList<String>();

        if (StringUtils.isBlank(lastToken)) {
            lastToken = "";
        }

        for (String name: searchScope) {
            if (name.startsWith(lastToken)) {
                candidates.add(name);
            }
        }
        if (candidates.size() == 1) {
            completion.complete(candidates.get(0).substring(lastToken.length()), true);
            return true;
        } else {
            completion.complete(candidates);
            return true;
        }
    }

    /** 判断 token 是否以目录分隔符结尾（表示用户已进入某目录） */
    private static boolean isEndOfDirectory(String token) {
        return !StringUtils.isBlank(token) && (token.endsWith(File.separator) || token.endsWith("/"));
    }

    /**
     * 文件路径 Tab 补全。
     *
     * @param completion 补全上下文
     * @return true 表示已完成补全；false 表示未处理，调用方需另行补全
     */
    public static boolean completeFilePath(Completion completion) {
        List<CliToken> tokens = completion.lineTokens();
        String token = tokens.get(tokens.size() - 1).value();

        if (token.startsWith("-") || StringUtils.isBlank(token)) {
            return false;
        }

        File dir = null;
        String partName = "";
        if (StringUtils.isBlank(token)) {
            dir = new File("").getAbsoluteFile();
            token = "";
        } else if (isEndOfDirectory(token)) {
            dir = new File(token);
        } else {
            File parent = new File(token).getAbsoluteFile().getParentFile();
            if (parent != null && parent.exists()) {
                dir = parent;
                partName = new File(token).getName();
            }
        }

        File tokenFile = new File(token);

        String tokenFileName = null;
        if (isEndOfDirectory(token)) {
            tokenFileName = "";
        } else {
            tokenFileName = tokenFile.getName();
        }

        if (dir == null) {
            return false;
        }

        File[] listFiles = dir.listFiles();

        ArrayList<String> names = new ArrayList<>();
        if (listFiles != null) {
            for (File child : listFiles) {
                if (child.getName().startsWith(partName)) {
                    if (child.isDirectory()) {
                        names.add(child.getName() + "/");
                    } else {
                        names.add(child.getName());
                    }
                }
            }
        }

        if (names.size() == 1 && isEndOfDirectory(names.get(0))) {
            String name = names.get(0);
            // 单目录候选：只插入相对片段，补全后不带尾随空格
            completion.complete(name.substring(tokenFileName.length()), false);
            return true;
        }

        String prefix = null;
        if (isEndOfDirectory(token)) {
            prefix = token;
        } else {
            prefix = token.substring(0, token.length() - new File(token).getName().length());
        }

        ArrayList<String> namesWithPrefix = new ArrayList<>();
        for (String name : names) {
            namesWithPrefix.add(prefix + name);
        }
        // 多候选时保留路径前缀再交给通用前缀补全
        CompletionUtils.complete(completion, namesWithPrefix);
        return true;
    }

    /**
     * 基于 JVM 已加载类列表补全类名（包名逐级展开）。
     *
     * @param completion 补全上下文
     * @return true 表示已处理
     */
    public static boolean completeClassName(Completion completion) {
        List<CliToken> tokens = completion.lineTokens();
        String lastToken = tokens.get(tokens.size() - 1).value();

        if (StringUtils.isBlank(lastToken)) {
            lastToken = "";
        }

        if (lastToken.startsWith("-")) {
            return false;
        }

        Instrumentation instrumentation = completion.session().getInstrumentation();

        Class<?>[] allLoadedClasses = instrumentation.getAllLoadedClasses();

        Set<String> result = new HashSet<String>();
        for(Class<?> clazz : allLoadedClasses) {
            String name = clazz.getName();
            if (name.startsWith("[")) {
                continue;
            }
            if(name.startsWith(lastToken)) {
                int index = name.indexOf('.', lastToken.length());

                if(index > 0) {
                    result.add(name.substring(0, index + 1));
                }else {
                    result.add(name);
                }

            }
        }

        if(result.size() == 1 && result.iterator().next().endsWith(".")) {
            completion.complete(result.iterator().next().substring(lastToken.length()), false);
        }else {
            CompletionUtils.complete(completion, result);
        }
        return true;
    }

    /**
     * 在已解析类名前提下补全方法名（含 {@code <init>}）。
     *
     * @param completion 补全上下文
     * @return true 表示已处理
     */
    public static boolean completeMethodName(Completion completion) {
        List<CliToken> tokens = completion.lineTokens();
        String lastToken = completion.lineTokens().get(tokens.size() - 1).value();

        if (StringUtils.isBlank(lastToken)) {
            lastToken = "";
        }

        // 从 token 序列推断类名位置
        String className;
        if (StringUtils.isBlank(lastToken)) {
            // tokens = { " ", "CLASS_NAME", " "}
            className = tokens.get(tokens.size() - 2).value();
        } else {
            // tokens = { " ", "CLASS_NAME", " ", "PARTIAL_METHOD_NAME"}
            className = tokens.get(tokens.size() - 3).value();
        }

        Matcher<String> classNameMatcher = SearchUtils.classNameMatcher(className, false);
        Set<Class<?>> results = new LinkedHashSet<Class<?>>();
        String matchedClassName = null;
        for (Class<?> clazz : completion.session().getInstrumentation().getAllLoadedClasses()) {
            if (clazz == null || !classNameMatcher.matching(clazz.getName())) {
                continue;
            }
            if (matchedClassName == null) {
                matchedClassName = clazz.getName();
            } else if (!matchedClassName.equals(clazz.getName())) {
                completion.complete(Collections.<String>emptyList());
                return true;
            }
            results.add(clazz);
        }
        if (results.isEmpty()) {
            completion.complete(Collections.<String>emptyList());
            return true;
        }

        Set<String> res = new LinkedHashSet<String>();
        for (Class<?> clazz : results) {
            Method[] methods;
            try {
                methods = clazz.getDeclaredMethods();
            } catch (LinkageError e) {
                // 方法签名可能引用当前 ClassLoader 不可见的类型，跳过该类
                continue;
            }
            for (Method method : methods) {
                if (StringUtils.isBlank(lastToken)) {
                    res.add(method.getName());
                } else if (method.getName().startsWith(lastToken)) {
                    res.add(method.getName());
                }
            }
        }
        if (StringUtils.isBlank(lastToken) || "<init>".startsWith(lastToken)) {
            res.add("<init>");
        }

        if (res.size() == 1) {
            completion.complete(res.iterator().next().substring(lastToken.length()), true);
            return true;
        } else {
            CompletionUtils.complete(completion, res);
            return true;
        }
    }

    /**
     * 推断当前光标位于第几个 positional 参数（1-based）。
     *
     * @param completion 补全上下文
     * @return 参数序号；光标在选项上时返回 -1
     */
    public static int detectArgumentIndex(Completion completion) {
        List<CliToken> tokens = completion.lineTokens();
        CliToken lastToken = tokens.get(tokens.size() - 1);

        if (lastToken.value().startsWith("-") || lastToken.value().startsWith("--")) {
            return -1;
        }

        if (StringUtils.isBlank((lastToken.value())) && tokens.size() == 1) {
            return 1;
        }

        int tokenCount = 0;

        for (CliToken token : tokens) {
            if (StringUtils.isBlank(token.value()) || token.value().startsWith("-") || token.value().startsWith("--")) {
                // 跳过空白与选项 token
                continue;
            }
            tokenCount++;
        }

        if (StringUtils.isBlank((lastToken.value())) && tokens.size() != 1) {
            tokenCount++;
        }
        return tokenCount;
    }

    /** 补全短选项名（{@code -x} 形式） */
    public static void completeShortOption(Completion completion, CliToken lastToken, List<Option> options) {
        String prefix = lastToken.value().substring(1);
        List<String> candidates = new ArrayList<String>();
        for (Option option : options) {
            if (option.getShortName().startsWith(prefix)) {
                candidates.add(option.getShortName());
            }
        }
        complete(completion, prefix, candidates);
    }

    /** 补全长选项名（{@code --name} 形式） */
    public static void completeLongOption(Completion completion, CliToken lastToken, List<Option> options) {
        String prefix = lastToken.value().substring(2);
        List<String> candidates = new ArrayList<String>();
        for (Option option : options) {
            if (option.getLongName().startsWith(prefix)) {
                candidates.add(option.getLongName());
            }
        }
        complete(completion, prefix, candidates);
    }

    /** 在光标位于命令名后时展示格式化 usage 帮助 */
    public static void completeUsage(Completion completion, CLI cli) {
        Tty tty = completion.session().get(Session.TTY);
        String usage = StyledUsageFormatter.styledUsage(cli, tty.width());
        completion.complete(Collections.singletonList(usage));
    }

    /** 单/多候选前缀补全：唯一匹配直接插入，否则展示 LCP 或完整列表 */
    private static void complete(Completion completion, String prefix, List<String> candidates) {
        if (candidates.size() == 1) {
            completion.complete(candidates.get(0).substring(prefix.length()), true);
        } else {
            String commonPrefix = CompletionUtils.findLongestCommonPrefix(candidates);
            if (commonPrefix.length() > 0) {
                if (commonPrefix.length() == prefix.length()) {
                    completion.complete(candidates);
                } else {
                    completion.complete(commonPrefix.substring(prefix.length()), false);
                }

            } else {
                completion.complete(candidates);
            }
        }
    }

    /**
     * <pre>
     * 检查是否应补全某个 option 的值（如 --classPattern 后的类名）。
     * 例如 option 为 {@code --classPattern}，tokens 可能是：
     *  2 个：'--classPattern' ' '
     *  3 个：'--classPattern' ' ' 'demo.'
     * </pre>
     *
     * @param completion 补全上下文
     * @param option 选项完整 token（如 {@code --classPattern}）
     * @return 若已执行类名补全则 true
     */
    public static boolean shouldCompleteOption(Completion completion, String option) {
        List<CliToken> tokens = completion.lineTokens();
        // 两个 token：倒数第二个等于 option，倒数第一个非选项
        if (tokens.size() >= 2) {
            CliToken cliToken_1 = tokens.get(tokens.size() - 1);
            CliToken cliToken_2 = tokens.get(tokens.size() - 2);
            String token_2 = cliToken_2.value();
            if (!cliToken_1.value().startsWith("-") && token_2.equals(option)) {
                return CompletionUtils.completeClassName(completion);
            }
        }
        // 三个 token：option + 空白 + 部分类名
        if (tokens.size() >= 3) {
            CliToken cliToken_1 = tokens.get(tokens.size() - 1);
            CliToken cliToken_2 = tokens.get(tokens.size() - 2);
            CliToken cliToken_3 = tokens.get(tokens.size() - 3);
            if (!cliToken_1.value().startsWith("-") && cliToken_2.isBlank()
                    && cliToken_3.value().equals(option)) {
                return CompletionUtils.completeClassName(completion);
            }
        }
        return false;
    }

    /**
     * 按 {@link OptionCompleteHandler} 列表匹配当前 option 并委派值补全。
     *
     * @param completion 补全上下文
     * @param handlers 各选项的自定义补全处理器
     * @return 若某 handler 已处理则 true
     */
    public static boolean completeOptions(Completion completion, List<OptionCompleteHandler> handlers) {
        List<CliToken> tokens = completion.lineTokens();
        /**
         * <pre>
         * 例如 {@code --name a}：option + 空白 + 部分值
         * </pre>
         */
        if (tokens.size() >= 3) {
            CliToken cliToken_2 = tokens.get(tokens.size() - 2);
            CliToken cliToken_3 = tokens.get(tokens.size() - 3);

            if (cliToken_2.isBlank()) {
                String token_3 = cliToken_3.value();

                for (OptionCompleteHandler handler : handlers) {
                    if (handler.matchName(token_3)) {
                        return handler.complete(completion);
                    }
                }
            }
        }

        /**
         * <pre>
         * 例如 {@code --name }：option 后光标位于空白 token
         * </pre>
         */
        if (tokens.size() >= 2) {
            CliToken cliToken_1 = tokens.get(tokens.size() - 1);
            CliToken cliToken_2 = tokens.get(tokens.size() - 2);
            if (cliToken_1.isBlank()) {
                String token_2 = cliToken_2.value();
                for (OptionCompleteHandler handler : handlers) {
                    if (handler.matchName(token_2)) {
                        return handler.complete(completion);
                    }
                }
            }
        }

        return false;
    }
}
