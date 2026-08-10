// interface.ts — Agent 画布表单与 Begin 节点相关的 TypeScript 类型定义。

import { FormInstance } from '@/interfaces/antd-compat';
import { RAGFlowNodeType } from '@/interfaces/database/agent';

/** 算子配置表单通用 props：值变更回调、form 实例与节点上下文。 */
export interface IOperatorForm {
  onValuesChange?(changedValues: any, values: any): void;
  form?: FormInstance;
  node?: RAGFlowNodeType;
  nodeId?: string;
}

/** 下游算子选择表单 props，仅携带节点引用。 */
export interface INextOperatorForm {
  node?: RAGFlowNodeType;
  nodeId?: string;
}

/** 生成类算子参数项：key 与可选 component_id 绑定。 */
export interface IGenerateParameter {
  id?: string;
  key: string;
  component_id?: string;
}

/** Invoke 算子变量引用，扩展 IGenerateParameter 并含 value。 */
export interface IInvokeVariable extends IGenerateParameter {
  value?: string;
}

/** Categorize 锚点坐标：top/right 偏移与槽位索引 idx。 */
export type IPosition = { top: number; right: number; idx: number };

/** Begin 节点单条输入项：类型、默认值、可选项与展示名。 */
export interface BeginQuery {
  key: string;
  type: string;
  value: string;
  optional: boolean;
  name: string;
  options: (number | string | boolean)[];
}

/** Begin 节点 inputs 区块：头像、标题、输入映射与开场白等。 */
export type IInputs = {
  avatar: string;
  title: string;
  inputs: Record<string, BeginQuery>;
  prologue: string;
  mode: string;
};

/** 节点 outputs 映射：字段名 → { type, value }。 */
export type IOutputs = Record<
  string,
  {
    type?: string;
    value?: string;
  }
>;
