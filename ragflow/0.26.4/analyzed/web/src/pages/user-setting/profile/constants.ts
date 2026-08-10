/**
 * profile/constants.ts — 用户资料昵称校验规则常量。
 */

/** 昵称最大长度（字符数）。 */
export const NICKNAME_MAX_LENGTH = 100;

/** 昵称允许 Unicode 字母、数字及 . _ ' - 空格。 */
export const NICKNAME_PATTERN = /^[\p{L}\p{N} ._'-]+$/u;
