/* admin jsi18n 模拟库：QUnit 测试中替代真实 JavaScriptCatalog 的 gettext/格式桩 */
"use strict";"use strict";
{
    // 在测试全局对象上挂载 django 命名空间
    const django = this.django;    const django = this.django;

    // 英语复数规则桩：单数返回 0，否则 1
    django.pluralidx = function (count) {    django.pluralidx = function (count) {
        return count === 1 ? 0 : 1;
    };

    /* gettext 恒等实现：直接返回 msgid，不做翻译 */
    /* gettext identity library */    /* gettext identity library */

    // gettext：原样返回消息 id
    django.gettext = function (msgid) {    django.gettext = function (msgid) {
        return msgid;
    };
    // ngettext：按 count 选择单复数字符串
    django.ngettext = function (singular, plural, count) {    django.ngettext = function (singular, plural, count) {
        return count === 1 ? singular : plural;
    };
    // gettext_noop：标记待翻译字符串但不翻译
    django.gettext_noop = function (msgid) {    django.gettext_noop = function (msgid) {
        return msgid;
    };
    // pgettext：带上下文的 gettext 桩
    django.pgettext = function (context, msgid) {    django.pgettext = function (context, msgid) {
        return msgid;
    };
    // npgettext：带上下文的 ngettext 桩
    django.npgettext = function (context, singular, plural, count) {    django.npgettext = function (context, singular, plural, count) {
        return count === 1 ? singular : plural;
    };

    // interpolate：按 %(name)s 或 %s 占位符填充字符串
    django.interpolate = function (fmt, obj, named) {    django.interpolate = function (fmt, obj, named) {
        if (named) {
            return fmt.replace(/%\(\w+\)s/g, function (match) {
                return String(obj[match.slice(2, -2)]);
            });
        } else {
            return fmt.replace(/%s/g, function (match) {
                return String(obj.shift());
            });
        }
    };

    /* 本地化格式常量：日期/时间/数字分组等默认值 */
    /* formatting library */    /* formatting library */

    django.formats = {
        DATETIME_FORMAT: "N j, Y, P",
        DATETIME_INPUT_FORMATS: [
            "%Y-%m-%d %H:%M:%S",
            "%Y-%m-%d %H:%M:%S.%f",
            "%Y-%m-%d %H:%M",
            "%Y-%m-%d",
            "%m/%d/%Y %H:%M:%S",
            "%m/%d/%Y %H:%M:%S.%f",
            "%m/%d/%Y %H:%M",
            "%m/%d/%Y",
            "%m/%d/%y %H:%M:%S",
            "%m/%d/%y %H:%M:%S.%f",
            "%m/%d/%y %H:%M",
            "%m/%d/%y",
        ],
        DATE_FORMAT: "N j, Y",
        DATE_INPUT_FORMATS: ["%Y-%m-%d", "%m/%d/%Y", "%m/%d/%y"],
        DECIMAL_SEPARATOR: ".",
        FIRST_DAY_OF_WEEK: 0,
        MONTH_DAY_FORMAT: "F j",
        NUMBER_GROUPING: 3,
        SHORT_DATETIME_FORMAT: "m/d/Y P",
        SHORT_DATE_FORMAT: "m/d/Y",
        THOUSAND_SEPARATOR: ",",
        TIME_FORMAT: "P",
        TIME_INPUT_FORMATS: ["%H:%M:%S", "%H:%M:%S.%f", "%H:%M"],
        YEAR_MONTH_FORMAT: "F Y",
    };

    // get_format：返回 formats 字典中的格式，未知键则原样返回
    django.get_format = function (format_type) {    django.get_format = function (format_type) {
        const value = django.formats[format_type];
        if (typeof value === "undefined") {
            return format_type;
        } else {
            return value;
        }
    };

    /* 将 django 上的 i18n 函数复制到全局，供 admin JS 直接调用 */
    /* add to global namespace */    /* add to global namespace */
    this.pluralidx = django.pluralidx;
    this.gettext = django.gettext;
    this.ngettext = django.ngettext;
    this.gettext_noop = django.gettext_noop;
    this.pgettext = django.pgettext;
    this.npgettext = django.npgettext;
    this.interpolate = django.interpolate;
    this.get_format = django.get_format;
}
