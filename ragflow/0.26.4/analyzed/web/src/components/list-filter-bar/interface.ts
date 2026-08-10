// interface.ts — 列表筛选栏类型：筛选项树、集合结构与选中值映射。

/** 单个筛选项：可嵌套 list，支持搜索与计数展示。 */
export type FilterType = {
  id: string;
  field?: string;
  label: string | JSX.Element;
  list?: FilterType[];
  value?: string | string[];
  count?: number;
  canSearch?: boolean;
};
/** 按 field 分组的筛选项集合，供筛选面板渲染。 */
export type FilterCollection = {
  field: string;
  label: string;
  list: FilterType[];
  canSearch?: boolean;
};
/** 当前选中值：field → id 数组，或嵌套 field → id 数组。 */
export type FilterValue = Record<
  string,
  Array<string> | Record<string, Array<string>>
>;
/** 筛选提交回调：用户确认筛选时更新 FilterValue。 */
export type FilterChange = (value: FilterValue) => void;
