/**
 * date.ts — 基于 dayjs 的日期时间格式化：标准日期、相对时段与秒数可读化。
 */

import dayjs from 'dayjs';

/** 按 format 格式化日期，默认 DD/MM/YYYY HH:mm:ss；空值返回空串。 */
export function formatDate(date: any, format?: string) {
  const thisFormat = format || 'DD/MM/YYYY HH:mm:ss';
  if (!date) {
    return '';
  }
  return dayjs(date).format(thisFormat);
}

/** 格式化为 HH:mm:ss 时间部分。 */
export function formatTime(date: any) {
  if (!date) {
    return '';
  }
  return dayjs(date).format('HH:mm:ss');
}

/** 返回当前日期的默认格式字符串。 */
export function today() {
  return formatDate(dayjs());
}

/** 返回昨天日期的默认格式字符串。 */
export function lastDay() {
  return formatDate(dayjs().subtract(1, 'days'));
}

/** 返回一周前日期的默认格式字符串。 */
export function lastWeek() {
  return formatDate(dayjs().subtract(1, 'weeks'));
}

/** 仅日期部分，格式 DD/MM/YYYY。 */
export function formatPureDate(date: any) {
  if (!date) {
    return '';
  }
  return dayjs(date).format('DD/MM/YYYY');
}

/** ISO 风格日期 YYYY-MM-DD，无效输入返回空串。 */
export function formatStandardDate(date: any) {
  if (!date) {
    return '';
  }
  const parsedDate = dayjs(date);
  if (!parsedDate.isValid()) {
    return '';
  }
  return parsedDate.format('YYYY-MM-DD');
}

/** 秒数转为 xh xm xs 可读字符串（如 3661 -> 1h 1m 1s）。 */
export function formatSecondsToHumanReadable(seconds: number): string {
  if (isNaN(seconds) || seconds < 0) {
    return '0s';
  }

  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  // const s = toFixed(seconds % 60, 3);
  const s = seconds % 60;
  const formattedSeconds = s === 0 ? '0' : s.toFixed(3).replace(/\.?0+$/, '');
  const parts = [];
  if (h > 0) parts.push(`${h}h `);
  if (m > 0) parts.push(`${m}m `);
  if (s || parts.length === 0) parts.push(`${formattedSeconds}s`);

  return parts.join('');
}
