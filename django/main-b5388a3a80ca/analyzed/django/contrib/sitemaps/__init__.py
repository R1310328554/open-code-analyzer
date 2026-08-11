from django.apps import apps as django_apps
from django.conf import settings
from django.core import paginator
from django.core.exceptions import ImproperlyConfigured
from django.utils import translation


# 站点地图基类 — 生成 URL 列表供 XML 模板渲染
class Sitemap:
    # This limit is defined by Google. See the index documentation at
    # https://www.sitemaps.org/protocol.html#index.
    limit = 50000

    # If protocol is None, the URLs in the sitemap will use the protocol
    # with which the sitemap was requested.
    protocol = None

    # Enables generating URLs for all languages.
    i18n = False

    # Override list of languages to use.
    languages = None

    # Enables generating alternate/hreflang links.
    alternates = False

    # Add an alternate/hreflang link with value 'x-default'.
    x_default = False

    # 读取属性或调用可调用属性；i18n 模式下解包 (item, lang_code)
    def _get(self, name, item, default=None):
        try:
            attr = getattr(self, name)
        except AttributeError:
            return default
        if callable(attr):
            if self.i18n:
                # Split the (item, lang_code) tuples again for the location,
                # priority, lastmod and changefreq method calls.
                item, lang_code = item
            return attr(item)
        return attr

    # 返回该条目应展示的语言列表
    def get_languages_for_item(self, item):
        """Languages for which this item is displayed."""
        return self._languages()

    # 返回配置的 languages 或 settings.LANGUAGES
    def _languages(self):
        if self.languages is not None:
            return self.languages
        return [lang_code for lang_code, _ in settings.LANGUAGES]

    # i18n 时展开为 (item, lang_code) 元组列表
    def _items(self):
        if self.i18n:
            # Create (item, lang_code) tuples for all items and languages.
            # This is necessary to paginate with all languages already
            # considered.
            items = [
                (item, lang_code)
                for item in self.items()
                for lang_code in self.get_languages_for_item(item)
            ]
            return items
        return self.items()

    # 在指定语言上下文中解析 location
    def _location(self, item, force_lang_code=None):
        if self.i18n:
            obj, lang_code = item
            # Activate language from item-tuple or forced one before calling
            # location.
            with translation.override(force_lang_code or lang_code):
                return self._get("location", item)
        return self._get("location", item)

    # 按 Google 上限 limit 对条目分页
    @property
    def paginator(self):
        return paginator.Paginator(self._items(), self.limit)

    # 子类重写：返回待收录的对象列表
    # 返回 queryset 克隆以避免过早求值
    def items(self):
        return []

    # 默认用 get_absolute_url 作为路径
    def location(self, item):
        return item.get_absolute_url()

    # 确定 URL 协议，默认 https
    def get_protocol(self, protocol=None):
        # Determine protocol
        return self.protocol or protocol or "https"

    # 从 Site 模型或传入 site 获取域名
    def get_domain(self, site=None):
        # Determine domain
        if site is None:
            if django_apps.is_installed("django.contrib.sites"):
                Site = django_apps.get_model("sites.Site")
                try:
                    site = Site.objects.get_current()
                except Site.DoesNotExist:
                    pass
            if site is None:
                raise ImproperlyConfigured(
                    "To use sitemaps, either enable the sites framework or pass "
                    "a Site/RequestSite object in your view."
                )
        return site.domain

    # 生成指定页的 URL 信息字典列表
    def get_urls(self, page=1, site=None, protocol=None):
        protocol = self.get_protocol(protocol)
        domain = self.get_domain(site)
        return self._urls(page, protocol, domain)

    # 汇总所有条目的最新 lastmod
    # 按 date_field 降序取第一条作为最新 lastmod
    def get_latest_lastmod(self):
        if not hasattr(self, "lastmod"):
            return None
        if callable(self.lastmod):
            try:
                return max([self.lastmod(item) for item in self.items()], default=None)
            except TypeError:
                return None
        else:
            return self.lastmod

    # 分页遍历条目，组装 location/priority/lastmod/alternates
    def _urls(self, page, protocol, domain):
        urls = []
        latest_lastmod = None
        all_items_lastmod = True  # track if all items have a lastmod

        paginator_page = self.paginator.page(page)
        for item in paginator_page.object_list:
            loc = f"{protocol}://{domain}{self._location(item)}"
            priority = self._get("priority", item)
            lastmod = self._get("lastmod", item)

            if all_items_lastmod:
                all_items_lastmod = lastmod is not None
                if all_items_lastmod and (
                    latest_lastmod is None or lastmod > latest_lastmod
                ):
                    latest_lastmod = lastmod

            url_info = {
                "item": item,
                "location": loc,
                "lastmod": lastmod,
                "changefreq": self._get("changefreq", item),
                "priority": str(priority if priority is not None else ""),
                "alternates": [],
            }

            if self.i18n and self.alternates:
                item_languages = self.get_languages_for_item(item[0])
                for lang_code in item_languages:
                    loc = f"{protocol}://{domain}{self._location(item, lang_code)}"
                    url_info["alternates"].append(
                        {
                            "location": loc,
                            "lang_code": lang_code,
                        }
                    )
                if self.x_default and settings.LANGUAGE_CODE in item_languages:
                    lang_code = settings.LANGUAGE_CODE
                    loc = f"{protocol}://{domain}{self._location(item, lang_code)}"
                    loc = loc.replace(f"/{lang_code}/", "/", 1)
                    url_info["alternates"].append(
                        {
                            "location": loc,
                            "lang_code": "x-default",
                        }
                    )

            urls.append(url_info)

        if all_items_lastmod and latest_lastmod:
            self.latest_lastmod = latest_lastmod

        return urls


# 通用站点地图 — 基于 QuerySet 与可选 date_field 生成条目
class GenericSitemap(Sitemap):
    priority = None
    changefreq = None

    # 从 info_dict 取 queryset 与 date_field
    def __init__(self, info_dict, priority=None, changefreq=None, protocol=None):
        self.queryset = info_dict["queryset"]
        self.date_field = info_dict.get("date_field")
        self.priority = self.priority or priority
        self.changefreq = self.changefreq or changefreq
        self.protocol = self.protocol or protocol

    def items(self):
        # Make sure to return a clone; we don't want premature evaluation.
        return self.queryset.filter()

    # 从 date_field 读取条目的修改时间
    def lastmod(self, item):
        if self.date_field is not None:
            return getattr(item, self.date_field)
        return None

    def get_latest_lastmod(self):
        if self.date_field is not None:
            return (
                self.queryset.order_by("-" + self.date_field)
                .values_list(self.date_field, flat=True)
                .first()
            )
        return None
