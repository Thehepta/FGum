function main() {

     // hook 所有 View 的点击事件
      Java.perform(function () {
        var View = Java.use('android.view.View');
        var OnClickListener = Java.use('android.view.View$OnClickListener');

        // hook View 的 setOnClickListener 方法
        View.setOnClickListener.implementation = function (listener) {
          this.setOnClickListener(OnClickListener.$new({
            onClick: function (view) {
              // 按钮被点击时执行的代码
              console.log('Button was clicked!');
              // 调用原始方法以确保按钮的点击事件仍然被处理
              listener.onClick(view);
            }
          }));
        };
      });
}

setImmediate(main);