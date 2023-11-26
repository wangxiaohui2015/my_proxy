
package com.my.proxy.manager;

import org.apache.log4j.Logger;

import com.my.proxy.entity.BackendServer;
import com.my.proxy.lb.AbstractLoadBalancer;
import com.my.proxy.lb.SourceHashLBImpl;
import com.my.proxy.lb.WeightRRLBImpl;
import com.my.proxy.lb.WeightRandomLBImple;
import com.my.proxy.util.ConfigUtil;

/**
 * Load balance manager, get back end server according to configuration.
 * 
 * @author sunny
 *
 */
public class LBManager {

    private static final Logger logger = Logger.getLogger(LBManager.class);

    private static final LBManager instance = new LBManager();
    private AbstractLoadBalancer loadBalancer;

    private static final String LB_MODE_RANDOM = "random";
    private static final String LB_MODE_RR = "rr";
    private static final String LB_MODE_SOURCE = "source";

    private LBManager() {}

    public static final LBManager getInstance() {
        return instance;
    }

    public void init() throws Exception {
        String lbMode = ConfigUtil.getInstance().getLbMode();
        logger.info("Initialize load balancer, LB mode is: " + lbMode);
        switch (lbMode) {
            case LB_MODE_RANDOM:
                loadBalancer = new WeightRandomLBImple();
                break;
            case LB_MODE_RR:
                loadBalancer = new WeightRRLBImpl();
                break;
            case LB_MODE_SOURCE:
                loadBalancer = new SourceHashLBImpl();
                break;
            default:
                String errMsg = "cannot find load balancer.";
                logger.error(errMsg);
                throw new Exception(errMsg);
        }
    }

    public BackendServer getBackendServer(String clientIp, int clientPort) throws Exception {
        return loadBalancer.getServer(clientIp, clientPort);
    }
}
