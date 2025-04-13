# Warning
This solution sacrifices the cross-platform nature of JFrame and has low performance, so it should not be used in a production environment, and is for reference only

# Readme.md
zh [Chinese 中文](README.md)

# Show
![](Pictures/demo.png)

# Introduction
This is a JFrame with Acrylic effects, demo.java instructions on how to use it

Implemented using JNA libraries

JNA is responsible for calling the `Windows API` to implement the Acrylic effect, and the interface inherits from the `StdCallLibrary`, loading a local `dwmapi` library. This is done by calling the `DwmSetWindowAttribute` functions.

See Microsoft [DwmSetWindowAttribute](https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmsetwindowattribute)