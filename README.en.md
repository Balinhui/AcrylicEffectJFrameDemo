## Warning
This solution sacrifices the cross-platform nature of JFrame and has low performance, so it should not be used in a production environment, and is for reference only

## Show
<img src="./Pictures/demo.png" alt="image-20241217173028697" style="zoom: 30%;" />

## Introduce
This is a JFrame with Win11 mica effects, demo.java instructions on how to use it

Implemented using JNA libraries

JNA is responsible for calling the Windows API to implement the mica effect, and the interface inherits from the Library, which is implemented using the `SetWindowCompositionAttribute` method