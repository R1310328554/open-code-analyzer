// antd-compat.ts — 脱离 antd 后的分页、表单、上传等类型兼容层。

/** 分页组件 props，对齐 antd Table/Pagination 常用字段。 */
export type PaginationProps = {
  current?: number;
  pageSize?: number;
  total?: number;
  showSizeChanger?: boolean;
  showQuickJumper?: boolean;
  pageSizeOptions?: number[];
  onChange?: (page: number, pageSize?: number) => void;
};

/** Select/Cascader 选项节点类型。 */
export type DefaultOptionType = {
  label: string | React.ReactNode;
  value: string | number;
  disabled?: boolean;
  children?: DefaultOptionType[];
};

/** 上传文件列表项，含 status、percent 与 originFileObj。 */
export type UploadFile = {
  uid: string;
  name: string;
  status?: 'uploading' | 'done' | 'error' | 'removed';
  url?: string;
  thumbUrl?: string;
  response?: any;
  error?: any;
  size?: number;
  type?: string;
  lastModified?: number;
  percent?: number;
  originFileObj?: File;
};

/** 表格行选择配置。 */
export type TableRowSelection<T = any> = {
  selectedRowKeys?: React.Key[];
  onChange?: (selectedRowKeys: React.Key[], selectedRows: T[]) => void;
  getCheckboxProps?: (record: T) => {
    disabled?: boolean;
  };
};

/** 表单实例方法集合（get/set/validate/reset）。 */
export type FormInstance = {
  getFieldValue: (name: string | string[]) => any;
  getFieldsValue: (names?: string[]) => Record<string, any>;
  setFieldValue: (name: string | string[], value: any) => void;
  setFieldsValue: (values: Record<string, any>) => void;
  resetFields: (fields?: string[]) => void;
  validateFields: (fields?: string[]) => Promise<any>;
  getFieldsError: (fields?: string[]) => Array<{
    name: string | string[];
    errors: string[];
  }>;
  getFieldError: (name: string | string[]) => string[];
  isFieldTouched: (name: string | string[]) => boolean;
  isFieldsTouched: (fields?: string[]) => boolean;
};

/** Form.List 动态字段元数据。 */
export type FormListFieldData = {
  name: number;
  key: number;
  isListField?: boolean;
  fieldKey?: number;
};
