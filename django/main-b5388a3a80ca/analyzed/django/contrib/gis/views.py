from django.http import Http404
from django.utils.translation import gettext as _


# 向后兼容的 feed 视图分发器，按 slug 实例化并配置 Feed 类
def feed(request, url, feed_dict=None):
    """Provided for backwards compatibility."""
    if not feed_dict:
        raise Http404(_("No feeds are registered."))

    slug = url.partition("/")[0]
    try:
        f = feed_dict[slug]
    except KeyError:
        raise Http404(_("Slug %r isn’t registered.") % slug)

    instance = f()
    instance.feed_url = getattr(f, "feed_url", None) or request.path
    instance.title_template = f.title_template or ("feeds/%s_title.html" % slug)
    instance.description_template = f.description_template or (
        "feeds/%s_description.html" % slug
    )
    return instance(request)
