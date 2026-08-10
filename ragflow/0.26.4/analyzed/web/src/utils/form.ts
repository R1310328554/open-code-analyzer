/**
 * form.ts — 表单与 LLM 设置辅助：变量开关过滤、选项构建、字段前缀。
 */

import { variableEnabledFieldMap } from '@/constants/chat';
import { TFunction } from 'i18next';
import { camelCase } from 'lodash';
import omit from 'lodash/omit';

/** 根据开关状态收集未启用的 LLM 参数字段名（带 prefix）。 */
export const excludeUnEnabledVariables = (
  values: any = {},
  prefix = 'llm_setting.',
) => {
  const unEnabledFields: Array<keyof typeof variableEnabledFieldMap> =
    Object.keys(variableEnabledFieldMap).filter((key) => !values[key]) as Array<
      keyof typeof variableEnabledFieldMap
    >;

  return unEnabledFields.map(
    (key) => `${prefix}${variableEnabledFieldMap[key]}`,
  );
};

/** 从表单值中 omit 开关字段、parameter 及未启用变量对应项。 */
export const removeUselessFieldsFromValues = (values: any, prefix?: string) => {
  const nextValues: any = omit(values, [
    ...Object.keys(variableEnabledFieldMap),
    'parameter',
    ...excludeUnEnabledVariables(values, prefix),
  ]);

  return nextValues;
};

/** 将枚举/常量对象转为 Select 选项，可选 i18n 翻译与 camelCase 键。 */
export function buildOptions(
  data: Record<string, any>,
  t?: TFunction<['translation', ...string[]], undefined>,
  prefix?: string,
  camel: boolean = false,
) {
  if (t) {
    return Object.values(data).map((val) => ({
      label: t(
        `${prefix ? prefix + '.' : ''}${typeof val === 'string' ? (camel ? camelCase(val) : val.toLowerCase()) : val}`,
      ),
      value: val,
    }));
  }
  return Object.values(data).map((val) => ({ label: val, value: val }));
}

/** 根据 initialLlmSetting 初始化各 LLM 参数开关布尔值。 */
export function setLLMSettingEnabledValues(
  initialLlmSetting?: Record<string, any>,
) {
  const values = Object.keys(variableEnabledFieldMap).reduce<
    Record<string, boolean>
  >((pre, field) => {
    pre[field] =
      initialLlmSetting === undefined
        ? false
        : !!initialLlmSetting[
            variableEnabledFieldMap[
              field as keyof typeof variableEnabledFieldMap
            ]
          ];
    return pre;
  }, {});
  return values;
}

/**
 * 为表单字段名添加前缀（如 chat.icon）。
 * Add prefix to form field name
 * @param prefix - The prefix to add (e.g., 'chat.', 'settings.')
 * @param name - The field name
 * @returns The prefixed field name
 * @example
 * prefixName('chat.', 'icon') // returns 'chat.icon'
 * prefixName('', 'name') // returns 'name'
 */
/** 拼接 prefix 与 name 得到完整表单字段路径。 */
export function prefixName(prefix: string, name: string): string {
  return `${prefix}${name}`;
}
