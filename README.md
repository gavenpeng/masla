# Masla
A fully asynchronous Java API gateway, validated with millions of concurrent C-end users, supporting multiple access protocols including HTTP, HTTPS, and HTTP/2.

# Dependency

running only jdk, not dependency other compensate

# Feature


## 🧭 服务发现（Service Discovery）

Masla API 网关当前仅支持 **Nacos** 作为服务注册中心。  
它通过 Nacos 实现服务发现，自动感知服务的动态变更，确保请求始终路由到可用的服务实例。

---

## 🚦 流量控制（Traffic Control）

Masla 默认支持流量控制，基于 **令牌桶算法** 实现。

- 可为每个 API 单独配置限流策略
- 当前支持的是 **单节点限流**

---

## ⚡ 熔断机制（Traffic Circuit Breaking）

Masla 内置熔断机制：

- 当异常比例达到 **50%** 时自动触发熔断
- 熔断后流量将不再转发至异常服务
- 支持智能 **自动升级** 与 **自动恢复**

---

## 🔒 黑白名单（Black/White List）

支持多维度的访问控制：

- 服务级别控制，支持路径（Path）、客户端 IP、Header 参数匹配等方式
- 支持 **全局黑名单** 配置，统一限制不可信流量源

---

## 🌱 慢启动机制（Warm-up / Slow Start）

针对新加入的服务实例：

- 支持慢启动策略，流量在一段预热窗口内 **逐步增加**
- 类似 TCP 的慢启动机制
- 预热窗口结束后进入正常流量分发

---

## 🔁 负载均衡（Load Balancing）

Masla 默认提供 **Round-Robin（轮询）** 负载均衡策略：

- 请求将按顺序分发到所有可用实例
- 支持后续扩展其他负载均衡策略（如权重、最小连接数等）

---

## ❤️ 健康检查（Health Check）

- 要求服务实现 `/healthcheck` 接口
- Masla 会定期调用该接口，判断服务健康状态
- 可用于实现 **无损下线**、**灰度发布**、**零停机部署**

---

如果你需要英文版本、图示说明或补充实际使用案例，也可以告诉我，我可以继续整理。

# Architect


# Startup

1. 执行 Maven 打包命令
   在项目根目录下打开终端，执行以下命令：
```
mvn clean package
```
该命令会清理之前的构建文件，并重新编译和打包项目，生成可部署的压缩包 masla-0.0.1.tar.gz（文件名根据版本号可能不同）。

2. 解压压缩包

找到打包生成的压缩包文件 masla-0.0.1.tar.gz，执行如下命令解压：

```
tar -zxvf masla-0.0.1.tar.gz
```

解压后会生成一个目录，里面包含了所有可运行的程序文件。

3. 修改 masla.sh 中的 Nacos 配置
   找到和 Nacos 注册中心相关的配置项，按需修改地址、端口、命名空间等参数，确保与实际的 Nacos 服务环境匹配。
4. 启动 masla gateway

执行启动脚本启动服务：
```
sh run.sh start

```

该命令会启动 masla gateway 服务，确认启动成功后即可开始使用。