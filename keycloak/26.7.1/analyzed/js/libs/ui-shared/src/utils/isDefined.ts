/**
 * 类型守卫：判断值既非 `undefined` 也非 `null`。
 * 收窄后 TypeScript 将类型视为 {@link NonNullable}。
 */
export function isDefined<T>(value: T): value is NonNullable<T> {
  return value !== undefined && value !== null;
}
