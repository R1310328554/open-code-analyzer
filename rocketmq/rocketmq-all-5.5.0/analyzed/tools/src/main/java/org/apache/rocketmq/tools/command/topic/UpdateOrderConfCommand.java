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
package org.apache.rocketmq.tools.command.topic;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.rocketmq.common.UtilAll;
import org.apache.rocketmq.common.namesrv.NamesrvUtil;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.srvutil.ServerUtil;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.apache.rocketmq.tools.command.SubCommand;
import org.apache.rocketmq.tools.command.SubCommandException;

/**
 * updateOrderConf 子命令：管理顺序 Topic 的 Broker 队列映射配置。
 * <p>支持 get/put/delete 三种操作，配置存于 NameServer KV。
 */
public class UpdateOrderConfCommand implements SubCommand {

    @Override
    public String commandName() {
        return "updateOrderConf";
    }

    @Override
    /** 返回命令描述。 */
    public String commandDesc() {
        return "Create or update or delete order conf.";
    }

    @Override
    public Options buildCommandlineOptions(Options options) {
        Option opt = new Option("t", "topic", true, "Topic 名称");
        opt.setRequired(true);
        options.addOption(opt);

        opt = new Option("v", "orderConf", true, "顺序配置（如 brokerName1:num;brokerName2:num）");
        opt.setRequired(false);
        options.addOption(opt);

        opt = new Option("m", "method", true, "操作类型：put|get|delete");
        opt.setRequired(true);
        options.addOption(opt);

        return options;
    }

    @Override
    /** 按 method 参数查询、更新或删除顺序 Topic 配置。 */
    public void execute(final CommandLine commandLine, final Options options,
        RPCHook rpcHook) throws SubCommandException {
        DefaultMQAdminExt defaultMQAdminExt = new DefaultMQAdminExt(rpcHook);
        defaultMQAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));

        try {
            String topic = commandLine.getOptionValue('t').trim();
            String type = commandLine.getOptionValue('m').trim();

            // 从 NameServer KV 读取顺序配置
            if ("get".equals(type)) {

                defaultMQAdminExt.start();
                String orderConf =
                    defaultMQAdminExt.getKVConfig(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG, topic);
                System.out.printf("get orderConf success. topic=[%s], orderConf=[%s] ", topic, orderConf);

                return;
            // 创建或更新顺序 Topic 的 Broker 队列映射
            } else if ("put".equals(type)) {

                defaultMQAdminExt.start();
                String orderConf = "";
                if (commandLine.hasOption('v')) {
                    orderConf = commandLine.getOptionValue('v').trim();
                }
                if (UtilAll.isBlank(orderConf)) {
                    throw new Exception("please set orderConf with option -v.");
                }

                defaultMQAdminExt.createOrUpdateOrderConf(topic, orderConf, true);
                System.out.printf("update orderConf success. topic=[%s], orderConf=[%s]", topic,
                    orderConf.toString());
                return;
            // 删除指定 Topic 的顺序配置
            } else if ("delete".equals(type)) {

                defaultMQAdminExt.start();
                defaultMQAdminExt.deleteKvConfig(NamesrvUtil.NAMESPACE_ORDER_TOPIC_CONFIG, topic);
                System.out.printf("delete orderConf success. topic=[%s]", topic);

                return;
            }

            ServerUtil.printCommandLineHelp("mqadmin " + this.commandName(), options);
        } catch (Exception e) {
            throw new SubCommandException(this.getClass().getSimpleName() + " command failed", e);
        } finally {
            defaultMQAdminExt.shutdown();
        }
    }
}
