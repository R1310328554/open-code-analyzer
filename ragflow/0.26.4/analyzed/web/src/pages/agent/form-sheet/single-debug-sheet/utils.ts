// utils.ts — 单组件 Code 调试输出分组与布局判定。

import { Operator } from '../../constant';
import { CodeOutputContract } from '../../form/code-form/utils';

/** Code 调试结果中的系统保留字段名（错误、附件等）。 */
const SYSTEM_OUTPUT_NAMES = new Set([
  '_ERROR',
  '_ARTIFACTS',
  'attachments',
  '_ATTACHMENT_CONTENT',
]);

/** 分组后的 Code 调试输出：期望/实际类型、内容与系统字段。 */
export type GroupedCodeExecDebugOutput = {
  expectedType: string;
  actualType: string;
  rawResult: unknown;
  content: string;
  systemOutputs: Record<string, unknown>;
};

/** 按 contract 将调试 payload 拆分为业务结果与 systemOutputs。 */
export function groupCodeExecDebugOutput(
  data: Record<string, unknown> | undefined,
  contract: CodeOutputContract | null,
): GroupedCodeExecDebugOutput {
  const businessName = contract?.name ?? '';
  const source = data ?? {};
  const systemOutputs = Object.fromEntries(
    Object.entries(source).filter(([key]) => SYSTEM_OUTPUT_NAMES.has(key)),
  );

  return {
    expectedType: contract?.type ?? '',
    actualType: String(source.actual_type ?? ''),
    rawResult:
      source.raw_result ?? (businessName ? source[businessName] : undefined),
    content: String(source.content ?? ''),
    systemOutputs,
  };
}

/** 是否使用 CodeExec 专用调试面板（label 为 Operator.Code）。 */
export function shouldUseCodeExecDebugLayout(label?: string): boolean {
  return label === Operator.Code;
}
