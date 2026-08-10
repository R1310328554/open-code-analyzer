// interface.ts — 知识库元数据模块 TypeScript 类型：API 结构、表格行与弹窗 props。

import { ReactNode } from 'react';
import { MetadataType } from './constant';
/** 后端 getMetaData summary 原始结构：值可为 typed 对象或二维数组。 */
export type IMetaDataReturnType = Record<
  string,
  | { type: string; values: Array<Array<string | number>> }
  | Array<Array<string | number>>
>;
/** 单文档 meta 提交 JSON：field -> string | string[]。 */
export type IMetaDataReturnJSONType = Record<
  string,
  Array<string | number> | string
>;

/** 元数据 schema 扁平配置项：key、type、description 与 enum 可选值。 */
export interface IMetaDataReturnJSONSettingItem {
  key: string;
  type?: string;
  description?: string;
  enum?: string[];
}
export interface IMetaDataJsonSchemaProperty {
  type?: string;
  description?: string;
  enum?: string[];
  items?: {
    type?: string;
    enum?: string[];
  };
  format?: string;
}
/** JSON Schema 形态的元数据定义，properties 映射字段约束。 */
export interface IMetaDataJsonSchema {
  type?: 'object';
  properties?: Record<string, IMetaDataJsonSchemaProperty>;
  additionalProperties?: boolean;
}
export type IMetaDataReturnJSONSettings =
  | IMetaDataJsonSchema
  | Array<IMetaDataReturnJSONSettingItem>;

/** 元数据字段值类型：string、list、time、number 等。 */
export type MetadataValueType =
  | 'string'
  | 'list'
  // | 'bool'
  // | 'enum'
  | 'time'
  | 'number';

/** 元数据管理表格单行：字段名、描述、枚举值列表与值类型。 */
export type IMetaDataTableData = {
  field: string;
  description: string;
  restrictDefinedValues?: boolean;
  values: string[];
  valueType?: MetadataValueType;
};

export type IBuiltInMetadataItem = {
  key: string;
  type: MetadataValueType;
};

/** 元数据管理主弹窗 props：表格数据、操作模式与内置字段等。 */
export type IManageModalProps = {
  documentIds?: string[];
  title: ReactNode;
  isShowDescription?: boolean;
  isDeleteSingleValue?: boolean;
  visible: boolean;
  hideModal: () => void;
  tableData?: IMetaDataTableData[];
  isCanAdd: boolean;
  type: MetadataType;
  otherData?: Record<string, any>;
  isEditField?: boolean;
  isAddValue?: boolean;
  isShowValueSwitch?: boolean;
  isVerticalShowValue?: boolean;
  builtInMetadata?: IBuiltInMetadataItem[];
  success?: (data: any) => void;
  secondTitle?: ReactNode;
  testId?: string;
  okButtonTestId?: string;
  addButtonTestId?: string;
  nestedModalTestId?: string;
  nestedModalOkButtonTestId?: string;
};

/** 单字段值编辑子弹窗 props，含 addUpdateValue/addDeleteValue 回调。 */
export interface IManageValuesProps {
  title: ReactNode;
  existsKeys: string[];
  visible: boolean;
  isEditField?: boolean;
  isAddValue?: boolean;
  isShowDescription?: boolean;
  isShowValueSwitch?: boolean;
  isShowType?: boolean;
  isVerticalShowValue?: boolean;
  isAddValueMode?: boolean;
  data: IMetaDataTableData;
  type: MetadataType;
  hideModal: () => void;
  onSave: (data: IMetaDataTableData) => void;
  addUpdateValue: (
    key: string,
    originalValue: string,
    newValue: string | string[],
    type?: MetadataValueType,
  ) => void;
  addDeleteValue: (key: string, value: string) => void;
  testId?: string;
  okButtonTestId?: string;
  addValueButtonTestId?: string;
}

export interface DeleteOperation {
  key: string;
  value?: string;
}

export interface UpdateOperation {
  key: string;
  match: string;
  value: string | string[];
  valueType?: MetadataValueType;
}

/** 待提交的元数据批量操作：删除列表与更新列表。 */
export interface MetadataOperations {
  deletes: DeleteOperation[];
  updates: UpdateOperation[];
}
export interface ShowManageMetadataModalOptions {
  title?: ReactNode | string;
}
/** 打开管理弹窗时的可选配置，metadata 为初始表格数据。 */
export type ShowManageMetadataModalProps = Partial<IManageModalProps> & {
  metadata?: IMetaDataTableData[];
  isCanAdd: boolean;
  type: MetadataType;
  record?: Record<string, any>;
  builtInMetadata?: IBuiltInMetadataItem[];
  options?: ShowManageMetadataModalOptions;
  title?: ReactNode | string;
  isDeleteSingleValue?: boolean;
  documentIds?: string[];
};
