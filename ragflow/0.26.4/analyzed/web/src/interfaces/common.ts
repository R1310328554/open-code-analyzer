// common.ts — 通用分页、列表状态与模态框 Props 接口。

/** 标准分页状态：current、pageSize、total 与 onChange。 */
export interface Pagination {
  current: number;
  pageSize: number;
  total: number;
  onChange?: (page: number, pageSize: number) => void;
}

/** 带分页与搜索关键词的列表页基础 state。 */
export interface BaseState {
  pagination: Pagination;
  searchString: string;
}

/** 通用模态框 Props：显隐、loading、onOk 与 initialValues。 */
export interface IModalProps<T> {
  showModal?(): void;
  hideModal?(): void;
  switchVisible?(visible: boolean): void;
  visible?: boolean;
  loading?: boolean;
  onOk?(payload?: T): Promise<any> | void;
  initialValues?: T;
}
