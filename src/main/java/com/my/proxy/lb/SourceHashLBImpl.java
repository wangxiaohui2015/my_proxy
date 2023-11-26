package com.my.proxy.lb;

import com.my.proxy.entity.BackendServer;

/**
 * LB source hash algorithm, all server weight is 1 by default.
 * 
 * @author sunny
 *
 */
public class SourceHashLBImpl extends AbstractLoadBalancer {

    @Override
    protected BackendServer getBackendServer(String clientIP, int clientPort) throws Exception {
        return servers.get(clientIP.hashCode() % servers.size());
    }
}
