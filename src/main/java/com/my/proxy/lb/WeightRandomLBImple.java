package com.my.proxy.lb;

import java.util.Random;

import com.my.proxy.entity.BackendServer;

/**
 * LB weight random algorithm, all server weight is 1 by default.
 * 
 * @author sunny
 *
 */
public class WeightRandomLBImple extends AbstractLoadBalancer {

    private Random random = new Random();

    @Override
    protected BackendServer getBackendServer(String clientIP, int clientPort) throws Exception {
        return servers.get(random.nextInt(servers.size()));
    }
}
