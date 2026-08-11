// filters.js — 用 sessionStorage 持久化变更列表侧边栏过滤器展开状态
/**
 * Persist changelist filters state (collapsed/expanded).
 */
"use strict";
{
    // 从 sessionStorage 恢复各 details 过滤器的 open 状态
    // Init filters.
    let filters = JSON.parse(
        sessionStorage.getItem("django.admin.filtersState"),
    );

    if (!filters) {
        filters = {};
    }

    Object.entries(filters).forEach(([key, value]) => {
        const detailElement = document.querySelector(
            `[data-filter-title='${CSS.escape(key)}']`,
        );

        // Check if the filter is present, it could be from other view.
        if (detailElement) {
            value
                ? detailElement.setAttribute("open", "")
                : detailElement.removeAttribute("open");
        }
    });

    // Save filter state when clicks.
    const details = document.querySelectorAll("details");
    details.forEach((detail) => {
        detail.addEventListener("toggle", (event) => {
            filters[`${event.target.dataset.filterTitle}`] = detail.open;
            sessionStorage.setItem(
                "django.admin.filtersState",
                JSON.stringify(filters),
            );
        });
    });
}
