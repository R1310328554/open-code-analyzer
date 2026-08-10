// common.ts — 全局通用常量：文件图标、语言/i18n 映射、MIME、预览类型与主题。

/** 文件扩展名 → 静态 SVG 图标文件名映射。 */
export const fileIconMap = {
  aep: 'aep.svg',
  ai: 'ai.svg',
  avi: 'avi.svg',
  css: 'css.svg',
  csv: 'csv.svg',
  dmg: 'dmg.svg',
  doc: 'doc.svg',
  docx: 'docx.svg',
  eps: 'eps.svg',
  exe: 'exe.svg',
  fig: 'fig.svg',
  gif: 'gif.svg',
  html: 'html.svg',
  indd: 'indd.svg',
  java: 'java.svg',
  jpeg: 'jpeg.svg',
  jpg: 'jpg.svg',
  js: 'js.svg',
  json: 'json.svg',
  md: 'md.svg',
  mdx: 'mdx.svg',
  mkv: 'mkv.svg',
  mp3: 'mp3.svg',
  mp4: 'mp4.svg',
  mpeg: 'mpeg.svg',
  pdf: 'pdf.svg',
  png: 'png.svg',
  ppt: 'ppt.svg',
  pptx: 'pptx.svg',
  psd: 'psd.svg',
  rss: 'rss.svg',
  sql: 'sql.svg',
  svg: 'svg.svg',
  tiff: 'tiff.svg',
  txt: 'txt.svg',
  wav: 'wav.svg',
  webp: 'webp.svg',
  xls: 'xls.svg',
  xlsx: 'xlsx.svg',
  xml: 'xml.svg',
};

// TODO: 改用标准 BCP 47 语言标签与显示名
/** 设置页可选界面语言列表（英文标识）。 */
export const LanguageList = [
  'English',
  'Chinese',
  'Traditional Chinese',
  'Russian',
  'Indonesian',
  'Spanish',
  'Vietnamese',
  'Japanese',
  'Portuguese BR',
  'German',
  'French',
  'Italian',
  'Bulgarian',
  'Arabic',
  'Turkish',
  'Dutch',
];
/** 语言标识 → 本地化显示名称。 */
export const LanguageMap = {
  English: 'English',
  Chinese: '简体中文',
  'Traditional Chinese': '繁體中文',
  Russian: 'Русский',
  Indonesian: 'Bahasa Indonesia',
  Indonesia: 'Indonesia',
  Spanish: 'Español',
  Vietnamese: 'Tiếng việt',
  Japanese: '日本語',
  'Portuguese BR': 'Português BR',
  German: 'Deutsch',
  French: 'Français',
  Italian: 'Italiano',
  Bulgarian: 'Български',
  Arabic: 'العربية',
  Turkish: 'Türkçe',
  Dutch: 'Nederlands',
};

/** i18n 语言缩写枚举（BCP 47 风格）。 */
export enum LanguageAbbreviation {
  En = 'en',
  Zh = 'zh-Hans',
  ZhTraditional = 'zh-Hant',
  Ru = 'ru',
  Id = 'id',
  Ja = 'ja',
  Es = 'es',
  Vi = 'vi',
  PtBr = 'pt-BR',
  De = 'de',
  Fr = 'fr',
  It = 'it',
  Bg = 'bg',
  Ar = 'ar',
  Tr = 'tr',
  Ko = 'ko',
  Nl = 'nl',
}

/** 语言缩写 → 本地化显示名称。 */
export const LanguageAbbreviationMap = {
  [LanguageAbbreviation.En]: 'English',
  [LanguageAbbreviation.Zh]: '简体中文',
  [LanguageAbbreviation.ZhTraditional]: '繁體中文',
  [LanguageAbbreviation.Ru]: 'Русский',
  [LanguageAbbreviation.Id]: 'Bahasa Indonesia',
  [LanguageAbbreviation.Es]: 'Español',
  [LanguageAbbreviation.Vi]: 'Tiếng việt',
  [LanguageAbbreviation.Ja]: '日本語',
  [LanguageAbbreviation.PtBr]: 'Português BR',
  [LanguageAbbreviation.De]: 'Deutsch',
  [LanguageAbbreviation.Fr]: 'Français',
  [LanguageAbbreviation.It]: 'Italiano',
  [LanguageAbbreviation.Bg]: 'Български',
  [LanguageAbbreviation.Ar]: 'العربية',
  [LanguageAbbreviation.Tr]: 'Türkçe',
  [LanguageAbbreviation.Ko]: '한국어',
  [LanguageAbbreviation.Nl]: 'Nederlands',
};

/** 语言显示名/别名 → i18n 资源目录缩写。 */
export const LanguageTranslationMap = {
  English: 'en',
  Chinese: 'zh-Hans',
  'Traditional Chinese': 'zh-Hant',
  Russian: 'ru',
  Indonesian: 'id',
  Indonesia: 'id',
  Spanish: 'es',
  Vietnamese: 'vi',
  Japanese: 'ja',
  Korean: 'ko',
  'Portuguese BR': 'pt-BR',
  'pt-br': 'pt-BR',
  'pt-BR': 'pt-BR',
  German: 'de',
  French: 'fr',
  Italian: 'it',
  Tamil: 'ta',
  Telugu: 'te',
  Kannada: 'ka',
  Thai: 'th',
  Greek: 'el',
  Hindi: 'hi',
  Ukrainian: 'uk',
  Bulgarian: 'bg',
  Arabic: 'ar',
  Turkish: 'tr',
  Dutch: 'nl',
};

/** 常见上传/预览文件 MIME 类型枚举。 */
export enum FileMimeType {
  Bmp = 'image/bmp',
  Csv = 'text/csv',
  Odt = 'application/vnd.oasis.opendocument.text',
  Doc = 'application/msword',
  Docx = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  Gif = 'image/gif',
  Htm = 'text/htm',
  Html = 'text/html',
  Jpg = 'image/jpg',
  Jpeg = 'image/jpeg',
  Pdf = 'application/pdf',
  Png = 'image/png',
  Ppt = 'application/vnd.ms-powerpoint',
  Pptx = 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
  Tiff = 'image/tiff',
  Txt = 'text/plain',
  Xls = 'application/vnd.ms-excel',
  Xlsx = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  Mp4 = 'video/mp4',
  Json = 'application/json',
  Md = 'text/markdown',
  Mdx = 'text/mdx',
}

/** RAGFlow 云端默认域名。 */
export const Domain = 'cloud.ragflow.io';

//#region 文件预览相关扩展名
/** 可按图片预览的扩展名列表。 */
export const Images = [
  'jpg',
  'jpeg',
  'png',
  'gif',
  'bmp',
  'tif',
  'tiff',
  'webp',
  // 'svg',
  'ico',
];

// 无需 FileViewer 组件即可内嵌预览的类型
/** 支持内嵌预览的文档/表格/图片扩展名集合。 */
export const ExceptiveType = [
  'xlsx',
  'xls',
  'pdf',
  'docx',
  'md',
  'mdx',
  ...Images,
];

/** 前端文档预览白名单（与 ExceptiveType 相同）。 */
export const SupportedPreviewDocumentTypes = [...ExceptiveType];
//#endregion

/** 外部对接或迁移来源平台标识。 */
export enum Platform {
  RAGFlow = 'RAGFlow',
  Dify = 'Dify',
  FastGPT = 'FastGPT',
  Coze = 'Coze',
}

/** 界面主题：深色、浅色或跟随系统。 */
export enum ThemeEnum {
  Dark = 'dark',
  Light = 'light',
  System = 'system',
}
