package com.msw.masla.filter.servlet;

import com.msw.masla.common.util.MaslaSpringContextUtil;
import com.msw.masla.core.async.MaslaDefaultProxyInvokerFactory;
import com.msw.masla.filter.spi.MaslaSpi;
import com.msw.masla.metrics.prometheus.PrometheusMetricsExporter;
import com.msw.masla.protocol.http.netty.context.SessionContext;
import com.msw.masla.protocol.http.netty.event.BaseEvent;
import com.msw.masla.protocol.http.netty.session.IOSession;
import com.msw.masla.protocol.http.netty.util.BufferUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@MaslaSpi(order = -1)
public class PrometheusMetricsServlet implements MaslaServlet {

    @Override
    public String mappingPath() {
        return "/actuator/.*";
    }

    @Override
    public void init(MaslaDefaultProxyInvokerFactory proxyInvokerFactory) {
        log.info("Masla init prometheus metrics, path: /actuator/prometheus");
    }

    @Override
    public void service(SessionContext<IOSession, HttpRequest, HttpResponse> requestContext, BaseEvent event) {
        try {
            // 获取PrometheusMeterRegistry并导出指标
            io.micrometer.prometheus.PrometheusMeterRegistry prometheusRegistry = null;
            try {
                prometheusRegistry = MaslaSpringContextUtil.getBean(io.micrometer.prometheus.PrometheusMeterRegistry.class);
            } catch (Exception e) {
                log.debug("PrometheusMeterRegistry not found in Spring context", e);
            }

            if (prometheusRegistry != null) {
                // 刷新指标（确保读取最新值）
                try {
                    PrometheusMetricsExporter exporter =
                            (PrometheusMetricsExporter)MaslaSpringContextUtil.getBean("prometheusMetricsExporter");
                    if (exporter != null) {
                        exporter.refreshAndRegisterMetrics();
                    }
                } catch (Exception e) {
                    log.debug("PrometheusMetricsExporter not found, skip refresh", e);
                }

                // 获取Prometheus格式的指标数据
                String metricsData = prometheusRegistry.scrape();

                // 直接创建响应，避免使用createResponse方法添加额外header
                byte[] content = metricsData.getBytes(StandardCharsets.UTF_8);
                ByteBuf buf = BufferUtils.SERVER_POOL_ALLOCATOR.ioBuffer(content.length);
                buf.writeBytes(content);
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        buf);

                // 设置正确的Content-Type，不添加其他header
                response.headers().set("Content-Type", "text/plain; version=0.0.4; charset=utf-8");
                requestContext.getSession().writeAndFlush(response);
            } else {
                // 服务不可用时的响应
                String errorMsg = "# Prometheus metrics not available. Please check if prometheus is enabled in configuration.\n";
                byte[] content = errorMsg.getBytes(StandardCharsets.UTF_8);
                ByteBuf buf = BufferUtils.SERVER_POOL_ALLOCATOR.ioBuffer(content.length);
                buf.writeBytes(content);
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.SERVICE_UNAVAILABLE,
                        buf);
                response.headers().set("Content-Type", "text/plain; charset=utf-8");
                requestContext.getSession().writeAndFlush(response);
            }
        } catch (Exception e) {
            log.error("Masla failed to export Prometheus metrics:", e);
            // 错误响应，确保格式正确
            String errorMsg = "# Failed to export metrics: " + e.getMessage() + "\n";
            byte[] content = errorMsg.getBytes(StandardCharsets.UTF_8);
            ByteBuf buf = BufferUtils.SERVER_POOL_ALLOCATOR.ioBuffer(content.length);
            buf.writeBytes(content);
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    buf);
            response.headers().set("Content-Type", "text/plain; charset=utf-8");
            requestContext.getSession().writeAndFlush(response);
        } finally {
            requestContext.getEvent().recycle();
            requestContext.recycle();
        }

    }


    @Override
    public void destory() {

    }
}
