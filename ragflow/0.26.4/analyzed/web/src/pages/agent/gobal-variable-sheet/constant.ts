// constant.ts — 全局变量侧栏表单：字段配置、默认值及类型→控件映射。

import { FormFieldConfig, FormFieldType } from '@/components/dynamic-form';
import { t } from 'i18next';
import { TypesWithArray } from '../constant';
import { buildConversationVariableSelectOptions } from '../utils';
export { TypesWithArray } from '../constant';
// const TypesWithoutArray = Object.values(JsonSchemaDataType).filter(
//   (item) => item !== JsonSchemaDataType.Array,
// );
// const TypesWithArray = [
//   ...TypesWithoutArray,
//   ...TypesWithoutArray.map((item) => `array<${item}>`),
// ];

/** 全局变量编辑表单字段：name/type/value/description，name 仅允许字母数字下划线。 */
export const GlobalFormFields = [
  {
    label: t('flow.name'),
    name: 'name',
    placeholder: t('common.namePlaceholder'),
    required: true,
    validation: {
      pattern: /^[a-zA-Z_0-9]+$/,
      message: t('flow.variableNameMessage'),
    },
    type: FormFieldType.Text,
  },
  {
    label: t('flow.type'),
    name: 'type',
    placeholder: '',
    required: true,
    type: FormFieldType.Select,
    options: buildConversationVariableSelectOptions(),
  },
  {
    label: t('flow.defaultValue'),
    name: 'value',
    placeholder: '',
    type: FormFieldType.Textarea,
  },
  {
    label: t('flow.description'),
    name: 'description',
    placeholder: t('flow.variableDescription'),
    type: FormFieldType.Textarea,
  },
] as FormFieldConfig[];

/** 新建全局变量时的默认空值，type 默认为 String。 */
export const GlobalVariableFormDefaultValues = {
  name: '',
  type: TypesWithArray.String,
  value: '',
  description: '',
};

/** 变量 JSON Schema 类型到 DynamicForm 控件类型的映射表。 */
export const TypeMaps = {
  [TypesWithArray.String]: FormFieldType.Textarea,
  [TypesWithArray.Number]: FormFieldType.Number,
  [TypesWithArray.Boolean]: FormFieldType.Checkbox,
  [TypesWithArray.Object]: FormFieldType.Textarea,
  [TypesWithArray.ArrayString]: FormFieldType.Textarea,
  [TypesWithArray.ArrayNumber]: FormFieldType.Textarea,
  [TypesWithArray.ArrayBoolean]: FormFieldType.Textarea,
  [TypesWithArray.ArrayObject]: FormFieldType.Textarea,
};
