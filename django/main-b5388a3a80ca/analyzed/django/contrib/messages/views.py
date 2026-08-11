from django.contrib import messages


# 表单提交成功时自动添加 messages 成功提示的混入类
class SuccessMessageMixin:
    """
    Add a success message on successful form submission.
    """

    success_message = ""

    # 校验通过后调用 get_success_message 并写入 success 消息
    def form_valid(self, form):
        response = super().form_valid(form)
        success_message = self.get_success_message(form.cleaned_data)
        if success_message:
            messages.success(self.request, success_message)
        return response

    # 用 cleaned_data 格式化 success_message 模板字符串
    def get_success_message(self, cleaned_data):
        return self.success_message % cleaned_data
