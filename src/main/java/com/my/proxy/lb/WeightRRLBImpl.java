package com.my.proxy.lb;

import java.util.Map;

import com.my.proxy.entity.BackendServer;

/**
 * LB weight round robin algorithm, all server weight is 1 by default.
 * 
 * @author sunny
 *
 */
public class WeightRRLBImpl extends AbstractLoadBalancer {

    private long requestCount = 0;

    @Override
    protected BackendServer getBackendServer(Map<String, String> params) throws Exception {
        requestCount = requestCount == Long.MAX_VALUE ? 0 : ++requestCount;
        return servers.get((int) (requestCount % servers.size()));
    }
}
