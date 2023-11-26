package com.my.proxy.lb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.my.proxy.entity.BackendServer;
import com.my.proxy.util.ConfigUtil;

/**
 * Interface of load balancer.
 * 
 * @author sunny
 *
 */
public abstract class AbstractLoadBalancer {

    protected List<BackendServer> servers;

    public AbstractLoadBalancer() {
        servers = ConfigUtil.getInstance().getBackendServers();
        List<BackendServer> serverList = new ArrayList<>();
        servers.forEach(server -> {
            for (int i = 0; i < server.getWeight(); i++) {
                serverList.add(server);
            }
        });
        Collections.shuffle(serverList);
        servers = Collections.synchronizedList(serverList);
    }

    protected abstract BackendServer getBackendServer(String clientIP, int clientPort)
                    throws Exception;

    public BackendServer getServer(String clientIP, int clientPort) throws Exception {
        checkServers();
        return getBackendServer(clientIP, clientPort);
    }

    public void addServer(BackendServer server) {
        for (int i = 0; i < server.getWeight(); i++) {
            servers.add(server);
        }
    }

    public void delServer(BackendServer server) {
        servers.removeIf(server1 -> server1.getIp().equals(server.getIp())
                        && server1.getPort() == server.getPort());
    }

    public void checkServers() throws Exception {
        if (servers.isEmpty()) {
            String errMsg = "No backend server is available.";
            throw new Exception(errMsg);
        }
    }
}
