package com.msw.masla.metrics.frame;


import com.msw.masla.common.config.MaslaConfConfig;
import com.msw.masla.common.constant.MetricsConstants;
import com.msw.masla.common.monitor.metrics.MetricsEntry;
import com.msw.masla.common.util.MaslaSpringContextUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author gavin.peng 2020-02-01 21:48
 */
@Slf4j
public class AsyncAppAgregateAdminReporter extends AppAgregateAdminReporter {

    private LinkedBlockingQueue<AsyncMerticEntry> queueOne;
    private LinkedBlockingQueue<AsyncMerticEntry> queueTwo;
    private Thread[] sendThreadArr = new Thread[SEND_THREAD_COUNT];
    private final static int SEND_THREAD_COUNT = 2;
    private static AsyncAppAgregateAdminReporter reporter = new AsyncAppAgregateAdminReporter();



    private AsyncAppAgregateAdminReporter() {
        super();
        this.queueOne = new LinkedBlockingQueue<AsyncMerticEntry>(5000);
        this.queueTwo = new LinkedBlockingQueue<AsyncMerticEntry>(5000);

        for (int i = 0; i < SEND_THREAD_COUNT; i++) {
            ApiMetricSendTask target = ((i & 1) == 0)
                    ? new ApiMetricSendTask(this.queueTwo)
                    : new ApiMetricSendTask(this.queueOne);

            Thread sendThread = new Thread(target);
            sendThread.setDaemon(true);
            sendThread.setName("Masla-mertic-send-thread-" + (i + 1));
            sendThread.start();
            sendThreadArr[i] = sendThread;
        }
    }


    public static AsyncAppAgregateAdminReporter getReporter(){
        return reporter;
    }


    @Override
    public void stop() {
        super.stop();

        for (int i = 0; i < SEND_THREAD_COUNT; i++) {
            sendThreadArr[i].interrupt();
        }
    }

    @Override
    public void send(MetricsEntry entry, Long code) {
        AsyncMerticEntry asyncMerticEntry = new AsyncMerticEntry(entry, code);
        this.enterQueue(asyncMerticEntry);
//    sentMetrics(JSON.toJSONString(entry), code);
    }

    @Override
    public void send(MetricsEntry entry, int urlIndex) {
        AsyncMerticEntry asyncMerticEntry = new AsyncMerticEntry(entry, (long) urlIndex);
        this.enterQueue(asyncMerticEntry);
    }

    @Override
    public void send(MetricsEntry entry) {
        AsyncMerticEntry asyncMerticEntry = new AsyncMerticEntry(entry, 0L);
        this.enterQueue(asyncMerticEntry);
    }


    private void enterQueue(AsyncMerticEntry asyncMerticEntry) {
        if (!MetricsConstants.reportSet.contains(asyncMerticEntry.metricsEntry.getType())) {
            return;
        }
        long code = asyncMerticEntry.code % urlList.size();
        if ((code & 1) == 0L) {
            this.queueTwo.offer(asyncMerticEntry);
        } else {
            this.queueOne.offer(asyncMerticEntry);
        }
    }

    public class ApiMetricSendTask implements Runnable {

        private boolean running;
        private LinkedBlockingQueue<AsyncMerticEntry> sendQueue;

        public ApiMetricSendTask(LinkedBlockingQueue<AsyncMerticEntry> queue) {
            this.sendQueue = queue;
            this.running = true;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    AsyncMerticEntry merticEntry;
                    if (MaslaSpringContextUtil.getMaslaConfConfigBean().isReportMaslaMetrics()) {
                        merticEntry = this.sendQueue.take();
                    } else {
                        //下线后，全部丢弃,然后阻塞住
                        this.sendQueue.clear();
                        merticEntry = this.sendQueue.take();
                        //新生成的忽略，继续clear和block住
                        continue;
                    }
                    String metricsType = merticEntry.getMetricsEntry().getType();


                    sentMetrics(merticEntry.getMetricsEntry(), merticEntry.getCode());
                        log.info("Masla start send {} data", metricsType);
                } catch (InterruptedException e) {
                    //被中断，直接退出
                    running = false;
                } catch (Throwable e) {
                    log.error("Masla send metric to console failed:", e);
                }

            }
        }
    }


    class AsyncMerticEntry {
        private long code;
        private MetricsEntry metricsEntry;

        public AsyncMerticEntry(MetricsEntry metricsEntry, Long code) {
            this.code = code;
            this.metricsEntry = metricsEntry;
        }

        public long getCode() {
            return code;
        }

        public MetricsEntry getMetricsEntry() {
            return metricsEntry;
        }
    }
}