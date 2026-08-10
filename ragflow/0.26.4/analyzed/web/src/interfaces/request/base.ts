// base.ts — 通用分页/排序请求体基类。

/** 列表 API 通用分页参数：keywords、page、orderby 与 desc。 */
export interface IPaginationRequestBody {
  keywords?: string;
  page?: number;
  page_size?: number; // 每页条数；orderby 可选 name|create|doc_num|create_time|update_time，默认 create_time
  orderby?: string;
  desc?: string;
}
