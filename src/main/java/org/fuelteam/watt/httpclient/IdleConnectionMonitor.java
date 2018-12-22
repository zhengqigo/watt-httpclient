package org.fuelteam.watt.httpclient;

import java.util.concurrent.TimeUnit;

import org.apache.http.conn.HttpClientConnectionManager;

public class IdleConnectionMonitor extends Thread {

    private final HttpClientConnectionManager connMgr;

    // 空闲链接超时时间，默认60000ms，超时链接将在下一次空闲链接检查时被销毁
    private int idleTimeout = 60000;

    // 检查空闲链接的间隔周期，默认60000ms
    private int checkTime = 60000;

    private volatile boolean shutdown = false;

    public IdleConnectionMonitor(HttpClientConnectionManager connMgr) {
        super("IdleConnectionMonitor");
        this.connMgr = connMgr;
    }

    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(checkTime);
                    connMgr.closeExpiredConnections();
                    connMgr.closeIdleConnections(idleTimeout, TimeUnit.MILLISECONDS);
                }
            }
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public void trigger() {
        synchronized (this) {
            notifyAll();
        }
    }

    public void shutdown() {
        this.shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}