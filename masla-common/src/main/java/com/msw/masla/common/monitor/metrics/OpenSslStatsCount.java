package com.msw.masla.common.monitor.metrics;

import lombok.Data;
import java.util.concurrent.atomic.AtomicLong;

@Data
public class OpenSslStatsCount {

    private long timestamp;
    private String host;
    private String group;

    private AtomicLong accept;
    private AtomicLong accept_good;
    private AtomicLong accept_renegotiate;
    private AtomicLong number;
    private AtomicLong connect;
    private AtomicLong connect_good;
    private AtomicLong connect_renegotiate;
    private AtomicLong hits;
    private AtomicLong cb_hits;
    private AtomicLong misses;
    private AtomicLong timeouts;
    private AtomicLong cache_full;
    private AtomicLong ticket_key_fail;
    private AtomicLong ticket_key_new;
    private AtomicLong ticket_key_renew;
    private AtomicLong ticket_key_resume;

    public OpenSslStatsCount(){
        accept = new AtomicLong(0);
        accept_good = new AtomicLong(0);
        accept_renegotiate = new AtomicLong(0);
        number = new AtomicLong(0);
        connect = new AtomicLong(0);
        connect_good = new AtomicLong(0);
        connect_renegotiate = new AtomicLong(0);
        hits = new AtomicLong(0);
        cb_hits = new AtomicLong(0);
        misses = new AtomicLong(0);
        timeouts = new AtomicLong(0);
        cache_full = new AtomicLong(0);
        ticket_key_fail = new AtomicLong(0);
        ticket_key_new = new AtomicLong(0);
        ticket_key_renew = new AtomicLong(0);
        ticket_key_resume = new AtomicLong(0);
    }

    public static class OpenSslStatsCountHolder{
        private static OpenSslStatsCount openSslStatsCount = new OpenSslStatsCount();
    }

    public static OpenSslStatsCount getInstances(){
        return OpenSslStatsCount.OpenSslStatsCountHolder.openSslStatsCount;
    }

    public static void clear(){
        OpenSslStatsCount openSslStatsCount = OpenSslStatsCount.getInstances();
        openSslStatsCount.setTimestamp(0);
        openSslStatsCount.setGroup(null);
        openSslStatsCount.setHost(null);
        openSslStatsCount.setAccept(new AtomicLong(0));
        openSslStatsCount.setAccept_good(new AtomicLong(0));
        openSslStatsCount.setAccept_renegotiate(new AtomicLong(0));
        openSslStatsCount.setNumber(new AtomicLong(0));
        openSslStatsCount.setConnect(new AtomicLong(0));
        openSslStatsCount.setConnect_good(new AtomicLong(0));
        openSslStatsCount.setConnect_renegotiate(new AtomicLong(0));
        openSslStatsCount.setHits(new AtomicLong(0));
        openSslStatsCount.setCb_hits(new AtomicLong(0));
        openSslStatsCount.setMisses(new AtomicLong(0));
        openSslStatsCount.setCache_full(new AtomicLong(0));
        openSslStatsCount.setTicket_key_fail(new AtomicLong(0));
        openSslStatsCount.setTicket_key_new(new AtomicLong(0));
        openSslStatsCount.setTicket_key_renew(new AtomicLong(0));
        openSslStatsCount.setTicket_key_resume(new AtomicLong(0));
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public AtomicLong getAccept() {
        return accept;
    }

    public void setAccept(AtomicLong accept) {
        this.accept = accept;
    }

    public AtomicLong getAccept_good() {
        return accept_good;
    }

    public void setAccept_good(AtomicLong accept_good) {
        this.accept_good = accept_good;
    }

    public AtomicLong getAccept_renegotiate() {
        return accept_renegotiate;
    }

    public void setAccept_renegotiate(AtomicLong accept_renegotiate) {
        this.accept_renegotiate = accept_renegotiate;
    }

    public AtomicLong getNumber() {
        return number;
    }

    public void setNumber(AtomicLong number) {
        this.number = number;
    }

    public AtomicLong getConnect() {
        return connect;
    }

    public void setConnect(AtomicLong connect) {
        this.connect = connect;
    }

    public AtomicLong getConnect_good() {
        return connect_good;
    }

    public void setConnect_good(AtomicLong connect_good) {
        this.connect_good = connect_good;
    }

    public AtomicLong getConnect_renegotiate() {
        return connect_renegotiate;
    }

    public void setConnect_renegotiate(AtomicLong connect_renegotiate) {
        this.connect_renegotiate = connect_renegotiate;
    }

    public AtomicLong getHits() {
        return hits;
    }

    public void setHits(AtomicLong hits) {
        this.hits = hits;
    }

    public AtomicLong getCb_hits() {
        return cb_hits;
    }

    public void setCb_hits(AtomicLong cb_hits) {
        this.cb_hits = cb_hits;
    }

    public AtomicLong getMisses() {
        return misses;
    }

    public void setMisses(AtomicLong misses) {
        this.misses = misses;
    }

    public AtomicLong getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(AtomicLong timeouts) {
        this.timeouts = timeouts;
    }

    public AtomicLong getCache_full() {
        return cache_full;
    }

    public void setCache_full(AtomicLong cache_full) {
        this.cache_full = cache_full;
    }

    public AtomicLong getTicket_key_fail() {
        return ticket_key_fail;
    }

    public void setTicket_key_fail(AtomicLong ticket_key_fail) {
        this.ticket_key_fail = ticket_key_fail;
    }

    public AtomicLong getTicket_key_new() {
        return ticket_key_new;
    }

    public void setTicket_key_new(AtomicLong ticket_key_new) {
        this.ticket_key_new = ticket_key_new;
    }

    public AtomicLong getTicket_key_renew() {
        return ticket_key_renew;
    }

    public void setTicket_key_renew(AtomicLong ticket_key_renew) {
        this.ticket_key_renew = ticket_key_renew;
    }

    public AtomicLong getTicket_key_resume() {
        return ticket_key_resume;
    }

    public void setTicket_key_resume(AtomicLong ticket_key_resume) {
        this.ticket_key_resume = ticket_key_resume;
    }
}
