/** 仅显示日期的本地化格式选项（长日期样式）。 */
export const FORMAT_DATE_ONLY: Intl.DateTimeFormatOptions = {
  dateStyle: "long",
};

/** 仅显示时间的本地化格式选项（短时间样式）。 */
export const FORMAT_TIME_ONLY: Intl.DateTimeFormatOptions = {
  timeStyle: "short",
};

/** 日期与时间组合的默认格式选项。 */
export const FORMAT_DATE_AND_TIME: Intl.DateTimeFormatOptions = {
  ...FORMAT_DATE_ONLY,
  ...FORMAT_TIME_ONLY,
};

/**
 * 按指定 locale 与格式选项格式化日期时间。
 * 默认使用英文 locale 与日期+时间组合格式。
 */
export function formatDate(
  date: Date,
  locale: string = "en",
  options = FORMAT_DATE_AND_TIME,
) {
  return date.toLocaleString(locale, options);
}
