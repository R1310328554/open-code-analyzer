/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.quarkus.runtime.cli;

import java.util.Collection;
import java.util.Map;

import picocli.CommandLine;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Column;
import picocli.CommandLine.Help.TextTable;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.UsageMessageSpec;

/**
 * 基于 Quarkus CLI 风格的子命令列表渲染器，用于帮助信息中的 Commands 区块。
 * 实现 {@link picocli.CommandLine.IHelpSectionRenderer}。
 */
class SubCommandListRenderer implements CommandLine.IHelpSectionRenderer {

    /** 渲染层级缩进的子命令名称与描述表格。 */
    @Override
    public String render(Help help) {
        CommandSpec spec = help.commandSpec();
        if (spec.subcommands().isEmpty()) {
            return "";
        }

        Column commands = new Column(24, 2, Column.Overflow.SPAN);
        Column descriptions = new Column(spec.usageMessage().width() - 24, 2,
                Column.Overflow.WRAP);

        TextTable textTable = TextTable.forColumns(help.colorScheme(), commands, descriptions);
        textTable.setAdjustLineBreaksForWideCJKCharacters(spec.usageMessage().adjustLineBreaksForWideCJKCharacters());

        addHierarchy(spec.subcommands().values(), textTable, "");
        return textTable.toString();
    }

    /** 递归添加子命令及其嵌套子命令行。 */
    private void addHierarchy(Collection<CommandLine> collection, CommandLine.Help.TextTable textTable,
            String indent) {
        collection.stream().distinct().forEach(subcommand -> {
            // 命令名与别名以逗号分隔
            String names = String.join(", ", subcommand.getCommandSpec().names());
            String description = description(subcommand.getCommandSpec().usageMessage());
            textTable.addRowValues(indent + names, description);

            Map<String, CommandLine> subcommands = subcommand.getSubcommands();
            if (!subcommands.isEmpty()) {
                addHierarchy(subcommands.values(), textTable, indent + "  ");
            }
        });
    }

    /** 优先取 header 首行，否则取 description 首行。 */
    private String description(UsageMessageSpec usageMessage) {
        if (usageMessage.header().length > 0) {
            return usageMessage.header()[0];
        }
        if (usageMessage.description().length > 0) {
            return usageMessage.description()[0];
        }
        return "";
    }
}
