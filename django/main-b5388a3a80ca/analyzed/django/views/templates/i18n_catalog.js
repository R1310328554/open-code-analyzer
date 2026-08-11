{# django i18n_catalog.js 模板：注入 django.gettext/ngettext 等客户端 API #}
{% autoescape off %}{% autoescape off %}
'use strict';
{
  // 全局命名空间与 django 对象
  const globals = this;  const globals = this;
  const django = globals.django || (globals.django = {});

  {% if plural %}
  // 复数形式索引：由 gettext Plural-Forms 或默认中英规则决定
  django.pluralidx = function(n) {  django.pluralidx = function(n) {
    const v = {{ plural }};
    if (typeof v === 'boolean') {
      return v ? 1 : 0;
    } else {
      return v;
    }
  };
  {% else %}
  django.pluralidx = function(count) { return (count == 1) ? 0 : 1; };
  {% endif %}

  /* gettext 翻译库：catalog 合并与 gettext 系列函数 */  /* gettext library */

  django.catalog = django.catalog || {};
  {% if catalog_str %}
  const newcatalog = {{ catalog_str }};
  for (const key in newcatalog) {
    django.catalog[key] = newcatalog[key];
  }
  {% endif %}

  if (!django.jsi18n_initialized) {
    // 单数消息翻译，未命中则回退 msgid
    django.gettext = function(msgid) {    django.gettext = function(msgid) {
      const value = django.catalog[msgid];
      if (typeof value === 'undefined') {
        return msgid;
      } else {
        return (typeof value === 'string') ? value : value[0];
      }
    };

    // 复数消息：按 pluralidx 从 catalog 数组取值
    django.ngettext = function(singular, plural, count) {    django.ngettext = function(singular, plural, count) {
      const value = django.catalog[singular];
      if (typeof value === 'undefined') {
        return (count == 1) ? singular : plural;
      } else {
        return value.constructor === Array ? value[django.pluralidx(count)] : value;
      }
    };

    django.gettext_noop = function(msgid) { return msgid; };

    // 带上下文的 gettext（键为 context\x04msgid）
    django.pgettext = function(context, msgid) {    django.pgettext = function(context, msgid) {
      let value = django.gettext(context + '\x04' + msgid);
      if (value.includes('\x04')) {
        value = msgid;
      }
      return value;
    };

    django.npgettext = function(context, singular, plural, count) {
      let value = django.ngettext(context + '\x04' + singular, context + '\x04' + plural, count);
      if (value.includes('\x04')) {
        value = django.ngettext(singular, plural, count);
      }
      return value;
    };

    // Python 风格 %s / %(name)s 占位符插值
    django.interpolate = function(fmt, obj, named) {    django.interpolate = function(fmt, obj, named) {
      if (named) {
        return fmt.replace(/%\(\w+\)s/g, function(match){return String(obj[match.slice(2,-2)])});
      } else {
        return fmt.replace(/%s/g, function(match){return String(obj.shift())});
      }
    };


    /* 区域格式库：日期时间等 get_format */    /* formatting library */

    django.formats = {{ formats_str }};

    // 读取服务端注入的 formats 字典
    django.get_format = function(format_type) {    django.get_format = function(format_type) {
      const value = django.formats[format_type];
      if (typeof value === 'undefined') {
        return format_type;
      } else {
        return value;
      }
    };

    /* 将 API 挂到 window 供非 django 命名空间脚本使用 */    /* add to global namespace */
    globals.pluralidx = django.pluralidx;
    globals.gettext = django.gettext;
    globals.ngettext = django.ngettext;
    globals.gettext_noop = django.gettext_noop;
    globals.pgettext = django.pgettext;
    globals.npgettext = django.npgettext;
    globals.interpolate = django.interpolate;
    globals.get_format = django.get_format;

    django.jsi18n_initialized = true;
  }
};
{% endautoescape %}
