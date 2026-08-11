// 关联对象弹窗回调页：解析服务端常量并通知 opener 关闭弹窗
"use strict";
{
    // 从 django-admin-popup-response-constants 读取 action 与对象数据
    const initData = JSON.parse(
        document.getElementById("django-admin-popup-response-constants").dataset
            .popupResponse,
    );
    // 按 change/delete/add 调用 opener 上对应的 dismiss*Popup 方法
    switch (initData.action) {
        case "change":
            opener.dismissChangeRelatedObjectPopup(
                window,
                initData.value,
                initData.obj,
                initData.new_value,
            );
            break;
        case "delete":
            opener.dismissDeleteRelatedObjectPopup(window, initData.value);
            break;
        default:
            opener.dismissAddRelatedObjectPopup(
                window,
                initData.value,
                initData.obj,
                initData.optgroup,
            );
            break;
    }
}
