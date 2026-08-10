// types.ts — 提供商弹窗：字段配置、ProviderConfig 与组件 Props 类型定义。

import { FormFieldType } from '@/components/dynamic-form';
import type { IModelInfo } from '@/interfaces/request/llm';
import type { ReactNode } from 'react';

/**
 * 表单字段类型：FormFieldType.* 与 DynamicForm 一一对应；
 * inputSelect 为项目扩展（输入框 + 下拉建议），由 ProviderModal 渲染为 Custom 字段。
 */
export type FieldType = FormFieldType | 'inputSelect';

/** shouldRender 字符串令牌，运行时按实例是否已存在等上下文解析为谓词函数。 */
export type ShouldRenderToken =
  | 'hideWhenInstanceExists'
  | 'modelTypeIncludesChat'
  | 'modelTypeSupportsToolCall'
  | 'modelTypeIncludesTtsAndNotExists'
  | 'showBaseUrl'
  | 'showGroupId';

/**
 * 下拉选项：label 可为 ReactNode；regionKey 保留 url 对象原始键，
 * 便于将用户选中的 URL 反查为提交时的 region 字段。
 */
export type SelectOption = {
  label: string | ReactNode;
  value: string;
  regionKey?: string;
};

/** 按工厂返回不同 i18n 键的文本解析器（如通用 base_url 在各厂商 tooltip 不同）。 */
export type FactoryTextResolver = (llmFactory: string) => string;

/** 单字段配置：名称、类型、校验与条件渲染。 */
export interface FieldConfig {
  /** 字段名（支持嵌套路径，如 model_info.model_type） */
  name: string;
  /** 标签 i18n 键 */
  label: string;
  /** 字段类型 */
  type: FieldType;
  /** 是否必填 */
  required?: boolean;
  /**
   * Placeholder i18n key. May be a static key, or a function that takes the
   * current `llmFactory` and returns the key (for per-provider placeholders).
   */
  placeholder?: string | FactoryTextResolver;
  /**
   * Tooltip i18n key. May be a static key, or a function that takes the
   * current `llmFactory` and returns the key (for per-provider tooltips).
   */
  tooltip?: string | FactoryTextResolver;
  /** Options (used for select/multiSelect/inputSelect) */
  options?: SelectOption[];
  /** Default value */
  defaultValue?: any;
  /**
   * Validation rules.
   * `message` is treated as an i18n key by the ProviderModal and translated
   * via `t()` at field-build time. In `Number` fields, `min` / `max` bound
   * the value; the message is shown when the bound is violated.
   */
  validation?: {
    min?: number;
    max?: number;
    message?: string;
  };
  /**
   * Conditional rendering: returns true to show the field
   * @param values current form values
   */
  shouldRender?: ((values: Record<string, any>) => boolean) | ShouldRenderToken;
}

/** 单个 LLM 提供商弹窗的完整配置（字段列表 + verify/submit 转换）。 */
export interface ProviderConfig {
  /** Corresponding LLMFactory value (also used as the field-config key) */
  llmFactory: string;
  /** Modal title */
  title: string;
  /** Field list (in render order) */
  fields: FieldConfig[];
  /**
   * Transform form values into verify API parameters
   * Used to construct api_key / base_url / region / model_info when the Verify button is clicked.
   * `modelInfo` is assembled from `values` by the transform itself: if `values.model_info`
   * is already an array (the picker-merged case), it is used as-is; otherwise the transform
   * falls back to assembling from individual form fields (model_name / model_type / max_tokens / is_tools).
   */
  verifyTransform?: (values: Record<string, any>) => {
    apiKey: string | object | Record<string, any>;
    baseUrl?: string;
    region?: string;
    modelInfo?: IModelInfo[];
  };
  /**
   * Transform form values into submit API parameters.
   * Used to handle special field name mapping (e.g. volcengine's endpoint_id -> ark_api_key).
   * `modelInfo` is assembled from `values` by the transform itself (same rules as verifyTransform).
   */
  submitTransform?: (values: Record<string, any>) => Record<string, any>;
  /**
   * Optional link at the bottom of the modal
   * e.g. the official documentation link for Ollama-family providers
   */
  docLink?: string;
  /**
   * i18n key for the docLink text (optional)
   * e.g. 'ollamaLink'; the { name: llmFactory } variable is passed in
   */
  docLinkI18nKey?: string;
  /**
   * Custom docLink text (optional, takes precedence over docLinkI18nKey)
   */
  docLinkText?: string;
}

/**
 * viewMode 保存回调载荷：viewMode 为 true 时走 onViewModeOk 而非 onOk。
 * LIST_MODEL 工厂传 modelInfos；否则 modelInfos 为空并由 formValues 携带可编辑字段。
 */
export interface IViewModeOkPayload {
  instanceName: string;
  llmFactory: string;
  modelInfos: IModelInfo[];
  formValues?: Record<string, any>;
}

/** ProviderModal 组件 Props。 */
export interface ProviderModalProps {
  visible: boolean;
  hideModal: () => void;
  llmFactory: string;
  loading: boolean;
  editMode?: boolean;
  /**
   * Read-only "edit models" mode: opens the modal pre-filled with an
   * existing instance's data and only allows editing the model-related
   * fields (model_name, model_type, max_tokens, is_tools) plus the
   * list-models picker (when applicable). All other fields are disabled.
   * On save, only `addInstanceModel` is invoked (not `addProviderInstance`).
   */
  viewMode?: boolean;
  initialValues?: Record<string, any>;
  /**
   * Base URL options for the input+select combo (from IAvailableProvider.url)
   * Used by base_url/api_base fields of type inputSelect
   */
  baseUrlOptions?: SelectOption[];
  onOk: (payload: any, isVerify?: boolean) => Promise<any>;
  onVerify: (payload: any) => Promise<any>;
  /**
   * Save handler used when `viewMode` is true. The modal calls this with
   * the list of selected models (LIST_MODEL_PROVIDERS) or the editable
   * model-related form values (non-LIST_MODEL_PROVIDERS). If omitted,
   * the modal falls back to `onOk` and submits the standard payload.
   */
  onViewModeOk?: (payload: IViewModeOkPayload) => Promise<any>;
}
