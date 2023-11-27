package com.my.proxy.lb;

import java.util.Map;
import java.util.Random;

import com.my.proxy.entity.BackendServer;

/**
 * LB weight random algorithm, all server weight is 1 by default.
 * 
 * @author sunny
 *
 */
public class WeightRandomLBImpl extends AbstractLoadBalancer {

    private Random random = new Random();

    @Override
    protected BackendServer getBackendServer(Map<String, String> params) throws Exception {
        return servers.get(random.nextInt(servers.size()));
    }
}
