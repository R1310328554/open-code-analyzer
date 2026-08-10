// use-build-structured-output.ts — Agent 结构化输出：解析 nodeId@field 引用、二级菜单与 JSON Schema 类型推导。

import { getStructuredDatatype } from '@/utils/canvas-util';
import { get, isPlainObject } from 'lodash';
import { ReactNode, useCallback } from 'react';
import {
  AgentStructuredOutputField,
  JsonSchemaDataType,
  Operator,
} from '../constant';
import useGraphStore from '../store';

/** 将 nodeId@outputField 形式的引用拆分为字符串数组。 */
function splitValue(value?: string) {
  return typeof value === 'string' ? value?.split('@') : [];
}
/** 从引用值中提取画布节点 ID（@ 前缀部分）。 */
function getNodeId(value: string) {
  return splitValue(value).at(0);
}

/** 判断当前输出项是否为 Agent 节点的 structured_output，以展示二级菜单。 */
export function useShowSecondaryMenu() {
  const { getOperatorTypeFromId } = useGraphStore((state) => state);

  const showSecondaryMenu = useCallback(
    (value: string, outputLabel: string) => {
      const nodeId = getNodeId(value);
      return (
        getOperatorTypeFromId(nodeId) === Operator.Agent &&
        outputLabel === AgentStructuredOutputField
      );
    },
    [getOperatorTypeFromId],
  );

  return showSecondaryMenu;
}

/** 根据引用值读取对应 Agent 节点的 structured_output JSON Schema 定义。 */
export function useGetStructuredOutputByValue() {
  const { getNode } = useGraphStore((state) => state);

  const getStructuredOutput = useCallback(
    (value: string) => {
      const node = getNode(getNodeId(value));
      const structuredOutput = get(
        node,
        `data.form.outputs.${AgentStructuredOutputField}`,
      );

      return structuredOutput;
    },
    [getNode],
  );

  return getStructuredOutput;
}

/** 在选项列表中匹配 Agent 结构化输出，拼接 schema 字段路径为展示 label。 */
export function useFindAgentStructuredOutputLabel() {
  const getOperatorTypeFromId = useGraphStore(
    (state) => state.getOperatorTypeFromId,
  );

  const findAgentStructuredOutputLabel = useCallback(
    (
      value: string,
      options: Array<{
        label: string;
        value: string;
        parentLabel?: string | ReactNode;
        icon?: ReactNode;
      }>,
    ) => {
      // 仅处理 Agent 节点的 structured_output 引用
      // agent structured output
      const fields = splitValue(value);
      if (
        getOperatorTypeFromId(fields.at(0)) === Operator.Agent &&
        fields.at(1)?.startsWith(AgentStructuredOutputField)
      ) {
        // 命中后合并选项 label 与 JSON Schema 字段后缀
        // is agent structured output
        const agentOption = options.find((x) => value.includes(x.value));
        const jsonSchemaFields = fields
          .at(1)
          ?.slice(AgentStructuredOutputField.length);

        return {
          ...agentOption,
          label: (agentOption?.label ?? '') + jsonSchemaFields,
          value: value,
        };
      }
    },
    [getOperatorTypeFromId],
  );

  return findAgentStructuredOutputLabel;
}

/** 递归遍历 JSON Schema，按字段路径解析 compositeDataType。 */
export function useFindAgentStructuredOutputTypeByValue() {
  const { getOperatorTypeFromId } = useGraphStore((state) => state);
  const filterStructuredOutput = useGetStructuredOutputByValue();

  const findTypeByValue = useCallback(
    (
      values: unknown,
      target: string,
      path: string = '',
    ): string | undefined => {
      const properties =
        get(values, 'properties') || get(values, 'items.properties');

      if (isPlainObject(values) && properties) {
        for (const [key, value] of Object.entries(properties)) {
          const nextPath = path ? `${path}.${key}` : key;
          const { dataType, compositeDataType } = getStructuredDatatype(value);

          if (nextPath === target) {
            return compositeDataType;
          }

          if (
            [JsonSchemaDataType.Object, JsonSchemaDataType.Array].some(
              (x) => x === dataType,
            )
          ) {
            const type = findTypeByValue(value, target, nextPath);
            if (type) {
              return type;
            }
          }
        }
      }
    },
    [],
  );

  const findAgentStructuredOutputTypeByValue = useCallback(
    (value?: string) => {
      if (!value) {
        return;
      }
      const fields = splitValue(value);
      const nodeId = fields.at(0);
      const jsonSchema = filterStructuredOutput(value);

      if (
        getOperatorTypeFromId(nodeId) === Operator.Agent &&
        fields.at(1)?.startsWith(AgentStructuredOutputField)
      ) {
        const jsonSchemaFields = fields
          .at(1)
          ?.slice(AgentStructuredOutputField.length + 1);

        if (jsonSchemaFields) {
          const type = findTypeByValue(jsonSchema, jsonSchemaFields);
          return type;
        }
      }
    },
    [filterStructuredOutput, findTypeByValue, getOperatorTypeFromId],
  );

  return findAgentStructuredOutputTypeByValue;
}

/** 返回「节点名称 / 输出字段」形式的结构化输出展示文案。 */
export function useFindAgentStructuredOutputLabelByValue() {
  const { getNode } = useGraphStore((state) => state);

  const findAgentStructuredOutputLabel = useCallback(
    (value?: string) => {
      if (value) {
        const operatorName = getNode(getNodeId(value ?? ''))?.data.name;

        if (operatorName) {
          return operatorName + ' / ' + splitValue(value).at(1);
        }
      }

      return '';
    },
    [getNode],
  );

  return findAgentStructuredOutputLabel;
}
