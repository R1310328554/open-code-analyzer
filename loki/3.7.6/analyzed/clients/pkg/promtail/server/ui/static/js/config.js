// Promtail 配置页前端脚本：点击按钮将 #config_yaml 内容选中并复制到剪贴板。
// 依赖 jQuery，页面加载完成后绑定 copyToClipboard 点击事件。

function init() {
    $("#copyToClipboard").on("click", function () {
        var range = document.createRange();
        range.selectNode(document.getElementById("config_yaml"));
        window.getSelection().empty();
        window.getSelection().addRange(range);
        document.execCommand("copy");
        window.getSelection().empty();
    });
}

$(init);
