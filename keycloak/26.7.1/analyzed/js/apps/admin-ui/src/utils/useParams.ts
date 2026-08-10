/**
 * react-router-dom useParams 的类型安全封装。
 * 将路由参数字典断言为调用方定义的 Record<string, string>，避免可选 undefined 干扰。
 */
import { useParams as useParamsRR } from "react-router-dom";

export const useParams = <T extends Record<string, string>>() =>
  useParamsRR<T>() as T;
