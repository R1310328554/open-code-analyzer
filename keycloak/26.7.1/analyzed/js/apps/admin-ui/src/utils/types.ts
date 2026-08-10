/** 编译期字符串字面量替换工具类型（用于 beerify/debeerify 类型推导）。 */

/** ReplaceString 递归选项：skipFirst 保留首次匹配不替换。 */
export type ReplaceStringOptions = {
  skipFirst?: boolean;
};

/**
 * 在字符串字面量类型 Input 中将 Search 全部替换为 Replacement。
 * 支持 skipFirst 跳过第一次出现（用于复杂路径类型）。
 */
export type ReplaceString<
  Input extends string,
  Search extends string,
  Replacement extends string,
  Options extends ReplaceStringOptions = object,
> = Input extends `${infer Head}${Search}${infer Tail}`
  ? Options["skipFirst"] extends true
    ? `${Head}${Search}${ReplaceString<Tail, Search, Replacement>}`
    : `${Head}${Replacement}${ReplaceString<Tail, Search, Replacement>}`
  : Input;
