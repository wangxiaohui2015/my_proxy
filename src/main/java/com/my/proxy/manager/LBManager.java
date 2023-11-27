
package com.my.proxy.manager;

import java.util.Map;

import org.apache.log4j.Logger;

import com.my.proxy.entity.BackendServer;
import com.my.proxy.lb.AbstractLoadBalancer;
import com.my.proxy.lb.SourceHashLBImpl;
import com.my.proxy.lb.URILBImpl;
import com.my.proxy.lb.WeightRRLBImpl;
import com.my.proxy.lb.WeightRandomLBImpl;
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

    // Protocols
    private static final String LB_PROTOCOL_HTTP = "http";
    private static final String LB_PROTOCOL_TCP = "tcp";

    // Modes
    private static final String LB_MODE_RANDOM = "random";
    private static final String LB_MODE_RR = "rr";
    private static final String LB_MODE_SOURCE = "source";
    private static final String LB_MODE_URI = "uri";

    // Client request keys
    public static final String PARAM_KEY_CLIENT_IP = "clientIp";
    public static final String PARAM_KEY_CLIENT_PORT = "clientPort";
    public static final String PARAM_KEY_CLIENT_URI = "clientURI";

    private AbstractLoadBalancer loadBalancer;

    private LBManager() {
    }

    public static final LBManager getInstance() {
        return instance;
    }

    public void init() throws Exception {
        String protocol = ConfigUtil.getInstance().getLbProtocol();
        String mode = ConfigUtil.getInstance().getLbMode();
        logger.info("Initializing load balancer, protocol is: " + protocol + ", mode is: " + mode);
        switch (mode) {
        case LB_MODE_RANDOM:
            loadBalancer = new WeightRandomLBImpl();
            break;
        case LB_MODE_RR:
            loadBalancer = new WeightRRLBImpl();
            break;
        case LB_MODE_SOURCE:
            loadBalancer = new SourceHashLBImpl();
            break;
        case LB_MODE_URI:
            if (isL7Protocol()) {
                loadBalancer = new URILBImpl();
            } else {
                logger.warn("uri mode can only used for HTTP protocol, current protocol is " + protocol
                        + ", change to default mode " + LB_MODE_RR);
                loadBalancer = new WeightRRLBImpl();
            }
            break;
        default:
            String errMsg = "cannot find load balancer.";
            logger.error(errMsg);
            throw new Exception(errMsg);
        }
    }

    public boolean isL4Protocol() {
        return LB_PROTOCOL_TCP.equals(ConfigUtil.getInstance().getLbProtocol());
    }

    public boolean isL7Protocol() {
        return LB_PROTOCOL_HTTP.equals(ConfigUtil.getInstance().getLbProtocol());
    }

    public BackendServer getBackendServer(Map<String, String> params) throws Exception {
        return loadBalancer.getServer(params);
    }
}
