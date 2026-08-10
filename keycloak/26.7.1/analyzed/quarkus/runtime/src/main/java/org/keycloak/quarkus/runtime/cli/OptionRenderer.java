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

import org.keycloak.utils.StringUtil;

import picocli.CommandLine;
import picocli.CommandLine.Help.Ansi.Text;
import picocli.CommandLine.Help.ColorScheme;
import picocli.CommandLine.Help.IParamLabelRenderer;
import picocli.CommandLine.Model.OptionSpec;

import static org.keycloak.quarkus.runtime.cli.Picocli.NO_PARAM_LABEL;

import static picocli.CommandLine.Help.Ansi.OFF;

/**
 * Keycloak CLI 选项行渲染器：格式化短/长选项名与参数标签及描述列。
 */
public class OptionRenderer implements CommandLine.Help.IOptionRenderer {

    /** 短名与长名之间的分隔符。 */
    private static final String OPTION_NAME_SEPARATOR = ", ";
    private static final Text EMPTY_TEXT = OFF.text("");

    /** @return 两列表格单元格：选项名与单行描述 */
    @Override
    public Text[][] render(OptionSpec option, IParamLabelRenderer paramLabelRenderer, ColorScheme scheme) {
        String[] names = option.names();

        if (names.length > 2) {
            throw new CommandLine.PicocliException("Options should have 2 names at most.");
        }

        Text shortName = names.length > 1 ? scheme.optionText(names[0]) : EMPTY_TEXT;
        Text longName = createLongName(option, scheme);
        Text[][] result = new Text[1][];
        Text description = scheme.text(option.description()[0]);

        // 描述仅允许单行，换行由 TextTable 布局控制
        if (option.description().length > 1) {
            throw new CommandLine.PicocliException("Option[" + option + "] description should have a single line.");
        }

        if (EMPTY_TEXT.equals(shortName)) {
            result[0] = new Text[] { longName, description };
        } else {
            result[0] = new Text[] { shortName.concat(OPTION_NAME_SEPARATOR).concat(longName), description };
        }

        return result;
    }

    /** 生成长选项名，必要时附加参数占位符。 */
    private Text createLongName(OptionSpec option, ColorScheme scheme) {
        String longestName = option.longestName();
        Text name = scheme.optionText(longestName);
        String paramLabel = formatParamLabel(option);

        if (StringUtil.isNotBlank(paramLabel) && !NO_PARAM_LABEL.equals(paramLabel) && !option.usageHelp() && !option.versionHelp()) {
            name = name.concat(" ").concat(paramLabel);
        }

        return name;
    }

    /** 为无尖括号的参数标签自动补全 {@code <...>}。 */
    private String formatParamLabel(OptionSpec option) {
        String label = option.paramLabel();

        if (label.startsWith("<") || NO_PARAM_LABEL.equals(label)) {
            return label;
        }

        return "<" + label + ">";
    }

}
