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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.keycloak.config.OptionCategory;
import org.keycloak.quarkus.runtime.cli.command.AbstractCommand;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMapper;
import org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers;
import org.keycloak.utils.StringUtil;

import picocli.CommandLine;
import picocli.CommandLine.Model.ArgGroupSpec;
import picocli.CommandLine.Model.OptionSpec;

import static org.keycloak.quarkus.runtime.configuration.mappers.PropertyMappers.getMapper;
import static org.keycloak.utils.StringUtil.removeSuffix;

import static picocli.CommandLine.Help.Column.Overflow.SPAN;
import static picocli.CommandLine.Help.Column.Overflow.WRAP;

/**
 * Keycloak CLI 定制帮助渲染：按选项类别分组、过滤隐藏/不支持项并优化表格布局。
 */
public final class Help extends CommandLine.Help {

    /** 标准帮助选项名。 */
    static final String[] OPTION_NAMES = new String[] { "-h", "--help" };
    /** 帮助文本总宽度。 */
    private static final int HELP_WIDTH = 100;
    private static final String DEFAULT_OPTION_LIST_HEADING = "Options:";
    private static final String DEFAULT_COMMAND_LIST_HEADING = "Commands:";

    /** 是否显示全部选项（含隐藏与不支持项）。 */
    private boolean all;

    Help(CommandLine.Model.CommandSpec commandSpec, ColorScheme colorScheme) {
        super(commandSpec, colorScheme);
        if (commandSpec.userObject() instanceof AbstractCommand ac) {
            all = ac.isHelpAll();
        }
        configureUsageMessage(commandSpec);
    }

    /** 渲染非分组可见选项列表及各选项组区块。 */
    @Override
    public String optionList(Layout layout, Comparator<OptionSpec> optionSort, IParamLabelRenderer valueLabelRenderer) {
        List<OptionSpec> visibleOptionsNotInGroups = excludeHiddenAndGroupOptions(commandSpec().options());
        return optionListExcludingGroups(visibleOptionsNotInGroups, layout, optionSort, valueLabelRenderer) + optionListGroupSections();
    }

    /** 排除已归入 ArgGroup 及不可见选项。 */
    private List<OptionSpec> excludeHiddenAndGroupOptions(List<OptionSpec> all) {
        List<OptionSpec> result = new ArrayList<>(all);

        for (ArgGroupSpec group : optionSectionGroups()) {
            result.removeAll(group.allOptionsNested());
        }

        for (Iterator<OptionSpec> iter = result.iterator(); iter.hasNext(); ) {
            OptionSpec optionSpec = iter.next();

            if (!isVisible(optionSpec)) {
                iter.remove();
            }
        }

        return result;
    }

    /** 创建仅渲染可见选项的默认布局。 */
    @Override
    public Layout createDefaultLayout() {
        return new Layout(colorScheme(), createTextTable(), createDefaultOptionRenderer(), createDefaultParameterRenderer()) {
            @Override
            public void addOptions(List<OptionSpec> options, IParamLabelRenderer paramLabelRenderer) {
                for (OptionSpec optionSpec : options) {
                    if (isVisible(optionSpec)) {
                        addOption(optionSpec, paramLabelRenderer);
                    }
                }
            }
        };
    }

    /** 两列 TextTable：选项名与自动换行描述。 */
    private TextTable createTextTable() {
        int longOptionsColumnWidth = commandSpec().commandLine().getUsageHelpLongOptionsMaxWidth();
        int descriptionWidth = HELP_WIDTH - longOptionsColumnWidth;

        // 使用两列布局以更好控制选项名与描述的换行
        TextTable textTable = TextTable.forColumns(colorScheme(),
                new Column(longOptionsColumnWidth, 0, SPAN),  // " -cf, --config-file"
                new Column(descriptionWidth, 1, WRAP));

        textTable.setAdjustLineBreaksForWideCJKCharacters(commandSpec().usageMessage().adjustLineBreaksForWideCJKCharacters());

        return textTable;
    }

    @Override
    public IOptionRenderer createDefaultOptionRenderer() {
        return new OptionRenderer();
    }

    /** 为标题添加粗体与前后空行，并去除 Picocli 默认尾部空白。 */
    @Override
    public String createHeading(String text, Object... params) {
        if (StringUtil.isBlank(text)) {
            return super.createHeading(text, params);
        }

        // 去除 Picocli 默认 "Usage: " 等尾部空白，由本类统一控制换行
        String trimmedText = text.stripTrailing();
        return super.createHeading("%n@|bold " + trimmedText + "|@%n%n", params);
    }

    /** 位置参数由选项渲染器统一处理，此处返回空矩阵。 */
    @Override
    public IParameterRenderer createDefaultParameterRenderer() {
        return new IParameterRenderer() {
            @Override
            public Ansi.Text[][] render(CommandLine.Model.PositionalParamSpec param,
                    IParamLabelRenderer parameterLabelRenderer, ColorScheme scheme) {
                // 参数标签在选项渲染阶段统一格式化
                return new Ansi.Text[0][];
            }
        };
    }

    /** 过滤掉没有任何可见选项的分组。 */
    @Override
    public List<ArgGroupSpec> optionSectionGroups() {
        List<ArgGroupSpec> allGroupSpecs = super.optionSectionGroups();
        List<ArgGroupSpec> nonEmptyGroups = new ArrayList<>(allGroupSpecs);
        Iterator<ArgGroupSpec> argGroupSpecsIt = nonEmptyGroups.iterator();

        while (argGroupSpecsIt.hasNext()) {
            ArgGroupSpec argGroupSpec = argGroupSpecsIt.next();

            if (argGroupSpec.options().stream().anyMatch(this::isVisible)) {
                continue;
            }

            // 移除不含可见选项的分组
            argGroupSpecsIt.remove();
        }

        return nonEmptyGroups;
    }

    /** 配置 usage 消息缩写 synopsis 与列表标题。 */
    private void configureUsageMessage(CommandLine.Model.CommandSpec commandSpec) {
        commandSpec.usageMessage()
                .abbreviateSynopsis(true)
                .optionListHeading(DEFAULT_OPTION_LIST_HEADING)
                .commandListHeading(DEFAULT_COMMAND_LIST_HEADING);
    }

    /**
     * 判断选项是否应在帮助中展示。
     * 无描述、hidden、或非 --help-all 下的不支持项会被隐藏。
     */
    private boolean isVisible(OptionSpec option) {
        if (option.description().length == 0 || option.hidden()) {
            // 无描述或显式 hidden 的选项不展示
            return false;
        }

        if (all) {
            return true;
        }

        String optionName = option.longestName();

        OptionCategory category = null;
        if (option.group() != null && option.group().heading() != null) {
            category = OptionCategory.fromHeading(removeSuffix(option.group().heading(), ":"));
        }
        String kcKey = PropertyMappers.getKcKeyFromCliKey(optionName).orElse(null);
        if (kcKey == null) {
            return true;
        }
        PropertyMapper<?> mapper = getMapper(kcKey, category);

        if (mapper == null) {
            final var disabledMapper = PropertyMappers.getDisabledMapper(kcKey);

            // 展示无 enabledWhen 约束的禁用映射器
            return disabledMapper.flatMap(PropertyMapper::getEnabledWhen).isEmpty();
        }

        // 未请求 --help-all 时隐藏不支持的选项
        return PropertyMappers.isSupported(mapper);
    }

}
