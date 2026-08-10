// setting.ts — 用户/个人设置路由键、时区列表与浏览器默认时区。

/** 用户设置页路由基础 segment。 */
export const UserSettingBaseKey = 'user-setting';

/** 用户设置子页：资料、密码、模型、系统、API、团队、MCP、登出。 */
export enum UserSettingRouteKey {
  Profile = 'profile',
  Password = 'password',
  Model = 'model',
  System = 'system',
  Api = 'api',
  Team = 'team',
  MCP = 'mcp',
  Logout = 'logout',
}

/** 个人资料设置页路由基础 segment。 */
export const ProfileSettingBaseKey = 'profile-setting';

/** 个人资料设置子页（含套餐、Prompt、分块等扩展项）。 */
export enum ProfileSettingRouteKey {
  Profile = 'profile',
  Plan = 'plan',
  Model = 'model',
  System = 'system',
  Api = 'api',
  Team = 'team',
  Prompt = 'prompt',
  Chunk = 'chunk',
  Logout = 'logout',
}

/** 按 UTC 偏移排序的 IANA 时区列表，含 offset 与展示名。 */
export const TimezoneList = Object.freeze(
  Intl.supportedValuesOf('timeZone')
    .map((tz) => {
      const dtf = new Intl.DateTimeFormat('en-US', {
        hourCycle: 'h24',
        timeZone: tz,
        timeZoneName: 'longOffset',
      });

      const offsetString = dtf.formatToParts(new Date()).at(-1)!.value;
      const match = /^GMT(?<sign>\+|-)(?<hours>\d{2}):(?<minutes>\d{2})$/i.exec(
        offsetString,
      );

      const hours = match?.groups?.hours ?? '00';
      const minutes = match?.groups?.minutes ?? '00';
      const sign = match?.groups?.sign;

      return Object.freeze({
        name: `${offsetString} ${tz}`,
        id: tz,
        offset:
          (sign === '-' ? -1 : 1) * (Number(hours) * 60 + Number(minutes)),
        offsetString,
      });
    })
    .sort((a, b) => a.offset - b.offset),
);

/** 浏览器当前 IANA 时区，用于匹配默认项。 */
const navigatorTz = new Intl.DateTimeFormat().resolvedOptions().timeZone;
/** 与浏览器时区匹配的默认 TimezoneList 条目。 */
export const DEFAULT_TIMEZONE = TimezoneList.find(
  (tz) => tz.name === navigatorTz,
)!;
