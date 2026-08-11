// 管理后台左侧导航：折叠状态持久化与快速筛选应用/模型
"use strict";
{
    // 读取 localStorage 恢复侧栏展开/收起，并同步 main 区域位移
    const toggleNavSidebar = document.getElementById("toggle-nav-sidebar");
    if (toggleNavSidebar !== null) {
        const navSidebar = document.getElementById("nav-sidebar");
        const main = document.getElementById("main");
        let navSidebarIsOpen = localStorage.getItem(
            "django.admin.navSidebarIsOpen",
        );
        if (navSidebarIsOpen === null) {
            navSidebarIsOpen = "true";
        }
        main.classList.toggle("shifted", navSidebarIsOpen === "true");
        navSidebar.setAttribute("aria-expanded", navSidebarIsOpen);

        toggleNavSidebar.addEventListener("click", function () {
            if (navSidebarIsOpen === "true") {
                navSidebarIsOpen = "false";
            } else {
                navSidebarIsOpen = "true";
            }
            localStorage.setItem(
                "django.admin.navSidebarIsOpen",
                navSidebarIsOpen,
            );
            main.classList.toggle("shifted");
            navSidebar.setAttribute("aria-expanded", navSidebarIsOpen);
        });
    }

    // 收集导航链接标题，按输入实时隐藏不匹配的 app/model 行
    function initSidebarQuickFilter() {
        const options = [];
        const navSidebar = document.getElementById("nav-sidebar");
        if (!navSidebar) {
            return;
        }
        navSidebar.querySelectorAll("th[scope=row] a").forEach((container) => {
            options.push({ title: container.innerHTML, node: container });
        });

        // 支持 Esc 清空；筛选值写入 sessionStorage 以便刷新后保留
        function checkValue(event) {
            let filterValue = event.target.value;
            if (filterValue) {
                filterValue = filterValue.toLowerCase();
            }
            if (event.key === "Escape") {
                filterValue = "";
                event.target.value = ""; // clear input
            }
            let matches = false;
            for (const o of options) {
                let displayValue = "";
                if (filterValue) {
                    if (o.title.toLowerCase().indexOf(filterValue) === -1) {
                        displayValue = "none";
                    } else {
                        matches = true;
                    }
                }
                // show/hide parent <TR>
                o.node.parentNode.parentNode.style.display = displayValue;
            }
            if (!filterValue || matches) {
                event.target.classList.remove("no-results");
            } else {
                event.target.classList.add("no-results");
            }
            sessionStorage.setItem(
                "django.admin.navSidebarFilterValue",
                filterValue,
            );
        }

        const nav = document.getElementById("nav-filter");
        nav.addEventListener("change", checkValue, false);
        nav.addEventListener("input", checkValue, false);
        nav.addEventListener("keyup", checkValue, false);

        const storedValue = sessionStorage.getItem(
            "django.admin.navSidebarFilterValue",
        );
        if (storedValue) {
            nav.value = storedValue;
            checkValue({ target: nav, key: "" });
        }
    }
    window.initSidebarQuickFilter = initSidebarQuickFilter;
    initSidebarQuickFilter();
}
