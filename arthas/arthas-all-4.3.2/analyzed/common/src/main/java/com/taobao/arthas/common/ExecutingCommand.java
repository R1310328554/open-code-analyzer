package com.taobao.arthas.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 本地 shell 命令执行工具：{@link Runtime#exec} 并逐行收集标准输出。
 * <p>
 * 失败时记录 trace 日志并返回空列表，不抛异常。
 *
 * @author alessandro[at]perucchi[dot]org
 */
public class ExecutingCommand {

    private ExecutingCommand() {
    }

    /**
     * 按空格拆分命令字符串并执行。
     *
     * @param cmdToRun 完整命令行
     * @return  stdout 行列表；失败时为空
     */
    public static List<String> runNative(String cmdToRun) {
        String[] cmd = cmdToRun.split(" ");
        return runNative(cmd);
    }

    /**
     * 执行命令数组并读取 stdout 至进程结束。
     *
     * @param cmdToRunWithArgs 命令及参数数组
     * @return 输出行列表
     */
    public static List<String> runNative(String[] cmdToRunWithArgs) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(cmdToRunWithArgs);
        } catch (SecurityException e) {
            AnsiLog.trace("Couldn't run command {}:", Arrays.toString(cmdToRunWithArgs));
            AnsiLog.trace(e);
            return new ArrayList<String>(0);
        } catch (IOException e) {
            AnsiLog.trace("Couldn't run command {}:", Arrays.toString(cmdToRunWithArgs));
            AnsiLog.trace(e);
            return new ArrayList<String>(0);
        }

        ArrayList<String> sa = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                sa.add(line);
            }
            p.waitFor();
        } catch (IOException e) {
            AnsiLog.trace("Problem reading output from {}:", Arrays.toString(cmdToRunWithArgs));
            AnsiLog.trace(e);
            return new ArrayList<String>(0);
        } catch (InterruptedException ie) {
            AnsiLog.trace("Problem reading output from {}:", Arrays.toString(cmdToRunWithArgs));
            AnsiLog.trace(ie);
            Thread.currentThread().interrupt();
        } finally {
            IOUtils.close(reader);
        }
        return sa;
    }

    /**
     * 执行命令并返回首行输出。
     *
     * @param cmd2launch 命令字符串
     * @return 第一行或空串
     */
    public static String getFirstAnswer(String cmd2launch) {
        return getAnswerAt(cmd2launch, 0);
    }

    /**
     * 执行命令并返回指定行（0 起）的输出。
     *
     * @param cmd2launch 命令字符串
     * @param answerIdx 行索引
     * @return 该行文本或空串
     */
    public static String getAnswerAt(String cmd2launch, int answerIdx) {
        List<String> sa = ExecutingCommand.runNative(cmd2launch);

        if (answerIdx >= 0 && answerIdx < sa.size()) {
            return sa.get(answerIdx);
        }
        return "";
    }

}
