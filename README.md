# Masla
A fully asynchronous Java API gateway, validated with millions of concurrent C-end users, supporting multiple access protocols including HTTP, HTTPS, and HTTP/2.

# Dependency

running only jdk, not dependency other compensate

# Feature
- Service Discovery
  Masla API Gateway currently only supports Nacos as the service registry. It performs service discovery via Nacos to automatically detect dynamic changes in services.

- Traffic Control
  Masla provides built-in support for traffic control. You can configure rate limiting for each API using a token bucket algorithm, which is single-node rate limiting.

- Traffic Circuit Breaking
  Masla API Gateway has a default circuit breaking mechanism. When the failure rate reaches 50%, circuit breaking is triggered. It supports intelligent auto-upgrade and auto-recovery mechanisms.

- Black/White List
  Service-level support including matching by path, client IP, and header parameters.

Global blacklist support.

- Warm-up / Slow Start
  Supports a slow start mechanism for newly added service instances, similar to TCP slow start, where traffic is gradually increased. Once the warm-up window ends, normal traffic distribution resumes.

- Load Balancing
  Masla provides Round-Robin as the default load balancing strategy.

- Health Check
  Requires services to support /healthcheck endpoint. With health check support, zero-downtime deployment can be achieved.


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