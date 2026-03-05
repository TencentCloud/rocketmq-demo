## 安装sdk
首先根据 [https://github.com/apache/rocketmq-clients/tree/master/cpp](https://github.com/apache/rocketmq-clients/tree/master/cpp) 的指引，安装 rocketmq5 c++ SDK。
注意：推荐使用 CMake 编译 SDK，在编译 SDK 之前，需要下载安装 [grpc](https://grpc.io/)。推荐使用 1.46.3 版本的 grpc，更高版本的 grpc 与 SDK 存在兼容性问题。
## 修改`CMakeLists.txt`
修改`CMakeLists.txt`中的`/path/to/sdk/include`以及`/path/to/sdk/build`，使得 cmake 能够正确找到 SDK 的头文件和库。
例如：[https://github.com/apache/rocketmq-clients/tree/master/cpp](https://github.com/apache/rocketmq-clients/tree/master/cpp) 被下载到 `~/sdk` 目录，同时，sdk 被编译到 `~/sdk/build` 目录，那么，`/path/to/sdk/include` 应该被设置为 `~/sdk/include`，`/path/to/sdk/build` 应该被设置为 `~/sdk/build`。
## 编译运行
在各个 `Example*.cpp` 文件中，分别设置好 topic、ak、sk、access_point 等信息，即可编译运行。