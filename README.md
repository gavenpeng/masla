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
- 全局黑名单

# Architect


# Startup

