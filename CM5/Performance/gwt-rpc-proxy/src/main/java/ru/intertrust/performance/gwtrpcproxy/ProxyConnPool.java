package ru.intertrust.performance.gwtrpcproxy;

import org.apache.http.HttpHost;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.impl.nio.pool.BasicNIOPoolEntry;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.pool.NIOConnFactory;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.pool.PoolStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyConnPool extends BasicNIOConnPool {
    private static final Logger logger = LoggerFactory.getLogger(ProxyConnPool.class);

    public ProxyConnPool(
            final ConnectingIOReactor ioreactor,
            final ConnectionConfig config) {
        super(ioreactor, config);
    }

    public ProxyConnPool(
            final ConnectingIOReactor ioreactor,
            final NIOConnFactory<HttpHost, NHttpClientConnection> connFactory,
            final int connectTimeout) {
        super(ioreactor, connFactory, connectTimeout);
    }

    @Override
    public void release(final BasicNIOPoolEntry entry, boolean reusable) {
        logger.info("[proxy->origin] connection released " + entry.getConnection());
        super.release(entry, reusable);
        StringBuilder buf = new StringBuilder();
        PoolStats totals = getTotalStats();
        buf.append("[total kept alive: ").append(totals.getAvailable()).append("; ");
        buf.append("total allocated: ").append(totals.getLeased() + totals.getAvailable());
        buf.append(" of ").append(totals.getMax()).append("]");
        logger.info("[proxy->origin] " + buf.toString());
    }

}
