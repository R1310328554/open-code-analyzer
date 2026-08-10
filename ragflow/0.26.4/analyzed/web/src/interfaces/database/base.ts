// database/base.ts — 后端 API 统一响应包装类型。

/** 标准 HTTP JSON 响应：code、data、message、status。 */
export interface ResponseType<T = any> {
  code: number;
  data: T;
  message: string;
  status: number;
}

/** GET 类 Hook 返回：data + 可选 loading。 */
export interface ResponseGetType<T = any> {
  data: T;
  loading?: boolean;
}

/** POST/Mutation Hook 返回：data + loading + 扩展字段。 */
export interface ResponsePostType<T = any> {
  data: T;
  loading?: boolean;
  [key: string]: unknown;
}
