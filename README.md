# Masla
A fully asynchronous Java API gateway, validated with millions of concurrent C-end users, supporting multiple access protocols including HTTP, HTTPS, and HTTP/2.

# Dependency

running only jdk, not dependency other compensate

# Feature
- service discovery

 masla api gateway current only support nacos register center，通过nacos 服务发现来
 自动设别service的动态变更

- 流量控制

masla 默认支持流量控制，通过配置的形式，可以为每个api配置限流，通过令牌桶实现，是单机限流
- 流量熔断

masla api gateway 默认支持熔断机制，默认异常比例达到50%时，会触发熔断，熔断支持智能自动升降级自动恢复

- 黑白名单
  - service level,support path, client ip, header params match
- 全局黑名单
- 慢启动
  - 支持对新加入的service 实例的流量执行类似tcp慢启动的机制，慢启动期间缓慢给流量，慢启动窗口过后，就正常给流量
- 负载均衡
  - 默认提供RoundRobin 负载均衡器
- 健康检查
  - 需要service支持/healthcheck的请求，支持健康检查的service，来实现无损发布。


# Architect


# Startup

