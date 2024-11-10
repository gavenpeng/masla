package com.msw.masla.metrics.http;


import com.msw.masla.metrics.frame.AdminReporter;
import org.springframework.stereotype.Component;

/**
 * created by gavin.peng on 2020/2/11
 */
@Component
public class MetricConfConstants {

    public static String getMaslaAdminServerAddess() {
        return "";
    }

    public static void setMaslaAdminServerAddess(String maslaAdminServerAddess) {
        AdminReporter.fetchLatestAdminList();
    }

}
