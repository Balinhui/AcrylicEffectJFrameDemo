# 警告
此方案牺牲了JFrame的跨平台性，而且性能较低下，请勿用于生产环境，仅供参考

# Readme.md
en [English 英文](README.en.md)

# 展示
![](Pictures/demo.png)

# 介绍
这是一个带有亚克力效果的JFrame，demo.java说明如何使用

使用JNA第三方库实现

JNA负责调用`Windows API`来实现亚克力效果，接口继承`StdCallLibrary`，加载本地的`dwmapi`。通过调用`DwmSetWindowAttribute`函数实现。

见微软官方文档[DwmSetWindowAttribute](https://learn.microsoft.com/zh-cn/windows/win32/api/dwmapi/nf-dwmapi-dwmsetwindowattribute)
