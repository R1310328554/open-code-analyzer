import type { ValidationRule, ValidationValue } from "react-hook-form";

// Simplified version of https://github.com/react-hook-form/react-hook-form/blob/ea0f3ed86457691f79987a703ae8d50b9e16e2ad/src/logic/getRuleValue.ts#L10-L21
// TODO: Can be removed if https://github.com/react-hook-form/react-hook-form/issues/12178 is resolved
/**
 * 从 react-hook-form 校验规则中提取实际校验值。
 * 规则可能是原始值、`{ value, message }` 对象或 RegExp（后者返回 undefined）。
 */
export function getRuleValue<T extends ValidationValue>(
  rule?: ValidationRule<T>,
): T | undefined {
  if (typeof rule === "undefined" || rule instanceof RegExp) {
    return;
  }

  if (typeof rule === "object") {
    return rule.value;
  }

  return rule;
}
