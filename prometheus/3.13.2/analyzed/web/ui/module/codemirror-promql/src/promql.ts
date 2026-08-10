// CodeMirror PromQL 语言扩展入口：定义 LRLanguage、补全与 lint 策略热插拔。

// Copyright 2021 The Prometheus Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import { parser } from '@prometheus-io/lezer-promql';
import { Extension } from '@codemirror/state';
import { CompleteConfiguration, CompleteStrategy, newCompleteStrategy } from './complete';
import { LintStrategy, newLintStrategy, promQLLinter } from './lint';
import { CompletionContext } from '@codemirror/autocomplete';
import { LRLanguage } from '@codemirror/language';

// LanguageType 切换 Lezer 顶层规则，支持完整 PromQL 或仅指标名补全模式。
export enum LanguageType {
  PromQL = 'PromQL',
  MetricName = 'MetricName',
}

export function promQLLanguage(top: LanguageType): LRLanguage {
  return LRLanguage.define({
    parser: parser.configure({
      top: top,
    }),
    languageData: {
      closeBrackets: { brackets: ['(', '[', '{', "'", '"', '`'] },
      commentTokens: { line: '#' },
    },
  });
}

// PromQLExtension 封装补全与 lint 策略，asExtension 组装 CodeMirror Extension 数组。
/**
 * This class holds the state of the completion extension for CodeMirror and allow hot-swapping the complete strategy.
 */
export class PromQLExtension {
  private complete: CompleteStrategy;
  private lint: LintStrategy;
  private enableCompletion: boolean;
  private enableLinter: boolean;

  constructor() {
    this.complete = newCompleteStrategy();
    this.lint = newLintStrategy();
    this.enableLinter = true;
    this.enableCompletion = true;
  }

  setComplete(conf?: CompleteConfiguration): PromQLExtension {
    this.complete = newCompleteStrategy(conf);
    return this;
  }

  getComplete(): CompleteStrategy {
    return this.complete;
  }

  activateCompletion(activate: boolean): PromQLExtension {
    this.enableCompletion = activate;
    return this;
  }

  setLinter(linter: LintStrategy): PromQLExtension {
    this.lint = linter;
    return this;
  }

  getLinter(): LintStrategy {
    return this.lint;
  }

  activateLinter(activate: boolean): PromQLExtension {
    this.enableLinter = activate;
    return this;
  }

  destroy(): void {
    this.complete.destroy?.();
  }

// asExtension 按开关组合语言、自动补全与 promQLLinter 诊断扩展。
  asExtension(languageType = LanguageType.PromQL): Extension {
    const language = promQLLanguage(languageType);
    let extension: Extension = [language];
    if (this.enableCompletion) {
      const completion = language.data.of({
        autocomplete: (context: CompletionContext) => {
          return this.complete.promQL(context);
        },
      });
      extension = extension.concat(completion);
    }
    if (this.enableLinter) {
      extension = extension.concat(promQLLinter(this.lint.promQL, this.lint));
    }
    return extension;
  }
}
// PromQL 扩展模块为 Prometheus UI 编辑器提供语法高亮与智能提示。
