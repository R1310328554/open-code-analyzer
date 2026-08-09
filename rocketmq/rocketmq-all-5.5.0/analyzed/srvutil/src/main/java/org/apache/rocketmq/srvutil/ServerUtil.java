/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.srvutil;

import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 服务端命令行工具：构建通用选项、解析参数并转为 Properties。
 */
public class ServerUtil {

    /** 向 Options 追加 -h/--help 与 -n/--namesrvAddr 等通用选项。 */
    public static Options buildCommandlineOptions(final Options options) {
        Option opt = new Option("h", "help", false, "Print help");  // 打印帮助
        opt.setRequired(false);
        options.addOption(opt);

        opt =
            new Option("n", "namesrvAddr", true,
                "Name server address list, eg: '192.168.0.1:9876;192.168.0.2:9876'");  // NameServer 地址列表
        opt.setRequired(false);
        options.addOption(opt);

        return options;
    }

    /** 解析命令行；若含 -h 则打印帮助并 exit(0)，解析失败则 exit(1)。 */
    public static CommandLine parseCmdLine(final String appName, String[] args, Options options,
        CommandLineParser parser) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
            if (commandLine.hasOption('h')) {
                hf.printHelp(appName, options, true);
                System.exit(0);
            }
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            hf.printHelp(appName, options, true);
            System.exit(1);
        }

        return commandLine;
    }

    /** 打印命令行帮助信息。 */
    public static void printCommandLineHelp(final String appName, final Options options) {
        HelpFormatter hf = new HelpFormatter();
        hf.setWidth(110);
        hf.printHelp(appName, options, true);
    }

    /** 将已解析的命令行选项转为 Properties（longOpt 为键）。 */
    public static Properties commandLine2Properties(final CommandLine commandLine) {
        Properties properties = new Properties();
        Option[] opts = commandLine.getOptions();

        if (opts != null) {
            for (Option opt : opts) {
                String name = opt.getLongOpt();
                String value = commandLine.getOptionValue(name);
                if (value != null) {
                    properties.setProperty(name, value);
                }
            }
        }

        return properties;
    }

}
