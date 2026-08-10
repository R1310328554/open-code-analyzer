// constant.ts — 数据集元数据：删除确认文案映射与字段值类型选项。

import { TFunction } from 'i18next';
import { MetadataValueType } from './interface';

/** 元数据操作场景：批量管理、单条更新、全局/单文件设置。 */
export enum MetadataType {
  Manage = 1,
  UpdateSingle = 2,
  Setting = 3,
  SingleFileSetting = 4,
}

/** 按 MetadataType 返回删除字段/值时的标题与警告 i18n 文案。 */
export const MetadataDeleteMap = (
  t: TFunction<'translation', undefined>,
): Record<
  MetadataType,
  {
    title: string;
    warnFieldText: string;
    warnValueText: string;
    warnFieldName: string;
    warnValueName: string;
  }
> => {
  return {
    [MetadataType.Manage]: {
      title: t('common.delete') + ' ' + t('knowledgeDetails.metadata.metadata'),
      warnFieldText: t('knowledgeDetails.metadata.deleteManageFieldAllWarn'),
      warnValueText: t('knowledgeDetails.metadata.deleteManageValueAllWarn'),
      warnFieldName: t('knowledgeDetails.metadata.fieldNameExists'),
      warnValueName: t('knowledgeDetails.metadata.valueExists'),
    },
    [MetadataType.Setting]: {
      title: t('common.delete') + ' ' + t('knowledgeDetails.metadata.metadata'),
      warnFieldText: t('knowledgeDetails.metadata.deleteSettingFieldWarn'),
      warnValueText: t('knowledgeDetails.metadata.deleteSettingValueWarn'),
      warnFieldName: t('knowledgeDetails.metadata.fieldExists'),
      warnValueName: t('knowledgeDetails.metadata.valueExists'),
    },
    [MetadataType.UpdateSingle]: {
      title: t('common.delete') + ' ' + t('knowledgeDetails.metadata.metadata'),
      warnFieldText: t('knowledgeDetails.metadata.deleteManageFieldSingleWarn'),
      warnValueText: t('knowledgeDetails.metadata.deleteManageValueSingleWarn'),
      warnFieldName: t('knowledgeDetails.metadata.fieldSingleNameExists'),
      warnValueName: t('knowledgeDetails.metadata.valueSingleExists'),
    },
    [MetadataType.SingleFileSetting]: {
      title: t('common.delete') + ' ' + t('knowledgeDetails.metadata.metadata'),
      warnFieldText: t('knowledgeDetails.metadata.deleteSettingFieldWarn'),
      warnValueText: t('knowledgeDetails.metadata.deleteSettingValueWarn'),
      warnFieldName: t('knowledgeDetails.metadata.fieldExists'),
      warnValueName: t('knowledgeDetails.metadata.valueSingleExists'),
    },
  };
};

/** 未指定类型时的默认元数据值类型 */
export const DEFAULT_VALUE_TYPE: MetadataValueType = 'string';
// const VALUE_TYPES_WITH_ENUM = new Set<MetadataValueType>(['enum']);
/** 各 MetadataValueType 在 UI 下拉中的展示标签 */
export const VALUE_TYPE_LABELS: Record<MetadataValueType, string> = {
  string: 'String',
  time: 'Time',
  number: 'Number',
  // bool: 'Bool',
  // enum: 'Enum',
  list: 'List',
  // int: 'Int',
  // float: 'Float',
};

export const metadataValueTypeEnum = Object.keys(VALUE_TYPE_LABELS).reduce(
  (acc, item) => {
    return { ...acc, [item]: item };
  },
  {} as Record<MetadataValueType, MetadataValueType>,
);

export const metadataValueTypeOptions = Object.entries(VALUE_TYPE_LABELS).map(
  ([value, label]) => ({ label, value }),
);

/** 安全获取类型标签，未知值回退 string。 */
export const getMetadataValueTypeLabel = (value?: MetadataValueType) =>
  VALUE_TYPE_LABELS[value || DEFAULT_VALUE_TYPE] || VALUE_TYPE_LABELS.string;
