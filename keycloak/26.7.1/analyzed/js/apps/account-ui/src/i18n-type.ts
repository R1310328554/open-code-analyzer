/**
 * 翻译键类型占位符（当前别名为 any）。
 * 便于在代码中检索所有 t() 调用点；后续应逐步替换为类型安全的翻译键定义。
 */
// This type is aliased to any, so that we can find all the places where we use it.
// In the future all casts to this type should be removed from the code, so
// that we can have a proper type-safe translation function.
export type TFuncKey = any;
