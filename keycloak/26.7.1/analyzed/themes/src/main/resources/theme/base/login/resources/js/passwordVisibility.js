// 密码可见性切换：在 password/text 类型间切换并更新图标与 aria-label
const toggle = (button) =>  {
    const passwordElement = document.getElementById(button.getAttribute('aria-controls'));
    if (passwordElement.type === "password") {
        // 显示明文密码
        passwordElement.type = "text";
        button.children.item(0).className = button.dataset.iconHide;
        button.setAttribute("aria-label", button.dataset.labelHide);
    } else if(passwordElement.type === "text") {
        // 隐藏密码
        passwordElement.type = "password";
        button.children.item(0).className = button.dataset.iconShow;
        button.setAttribute("aria-label", button.dataset.labelShow);
    }
}

// 为所有带 data-password-toggle 的按钮绑定点击切换
document.querySelectorAll('[data-password-toggle]')
    .forEach(button => button.onclick = () => toggle(button));
