package com.my.proxy.lb;

import java.util.Map;

import com.my.proxy.entity.BackendServer;
import com.my.proxy.manager.LBManager;

/**
 * LB source hash algorithm, all server weight is 1 by default.
 * 
 * @author sunny
 *
 */
public class SourceHashLBImpl extends AbstractLoadBalancer {

    @Override
    protected BackendServer getBackendServer(Map<String, String> params) throws Exception {
        String clientIp = params.get(LBManager.PARAM_KEY_CLIENT_IP);
        return servers.get(clientIp.hashCode() % servers.size());
    }
}
