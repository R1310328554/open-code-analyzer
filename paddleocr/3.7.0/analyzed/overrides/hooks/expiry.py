# MkDocs 页面过期钩子：根据 git 修订日期与 expiry_days 标记文档是否过期
import re
from datetime import datetime


    # 页面上下文回调：读取 meta 日期，写入 is_expired / last_update 到模板变量
def on_page_context(context, page, config, nav):
    expiry_days = config.get("extra", {}).get("expiry_days", 365)

        # 从 git_revision_date_localized 等字段提取 YYYY-MM-DD 并与阈值比较
    def compute_expiry(meta):
        revision = (
            meta.get("git_revision_date_localized")
            or meta.get("git_creation_date_localized")
            or meta.get("revision_date")
        )
        is_expired = False
        last_update = None
        if revision:
            m = re.search(r"(\d{4}-\d{2}-\d{2})", str(revision))
            if m:
                last_update = m.group(1)
                try:
                    dt = datetime.strptime(last_update, "%Y-%m-%d")
                    if (datetime.now() - dt).days > expiry_days:
                        is_expired = True
                except Exception:
                    # 无法解析日期时，保持不显示过期提示
                    pass
        return is_expired, last_update

    # 将过期状态同步到 page 对象与 Jinja context 供主题渲染
    page.is_expired, page.last_update = compute_expiry(page.meta)
    context["is_expired"] = page.is_expired
    context["last_update"] = page.last_update
    context["expiry_days"] = expiry_days

    return context
