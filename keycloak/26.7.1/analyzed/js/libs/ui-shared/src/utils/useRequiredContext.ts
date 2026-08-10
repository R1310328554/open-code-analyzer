import type { Context } from "react";
import { useContext } from "react";
import { isDefined } from "./isDefined";

/**
 * 封装 {@link useContext}，当解析结果为 `null` 或 `undefined` 时抛出异常。
 * 适用于必须存在 Provider 且值不可为空的 Context。
 *
 * @param context 传给 `useContext` 的 React Context
 * @returns 非空的 Context 值
 */
export function useRequiredContext<T>(context: Context<T>): NonNullable<T> {
  const resolved = useContext(context);

  if (isDefined(resolved)) {
    return resolved;
  }

  throw new Error(
    `No provider found for ${
      context.displayName ? `the '${context.displayName}'` : "an unknown"
    } context, make sure it is included in your component hierarchy.`,
  );
}
