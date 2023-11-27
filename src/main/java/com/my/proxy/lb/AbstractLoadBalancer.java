package com.my.proxy.lb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.my.proxy.entity.BackendServer;
import com.my.proxy.util.ConfigUtil;

/**
 * Abstract load balancer, each LB algorithm need to extends this class.
 * 
 * @author sunny
 *
 */
public abstract class AbstractLoadBalancer {

	protected List<BackendServer> servers;

	public AbstractLoadBalancer() {
		initServers();
	}

	private void initServers() {
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

	protected abstract BackendServer getBackendServer(Map<String, String> params) throws Exception;

	public BackendServer getServer(Map<String, String> params) throws Exception {
		if (servers.isEmpty()) {
			String errMsg = "No backend server is available.";
			throw new Exception(errMsg);
		}
		return getBackendServer(params);
	}

	public void addServer(BackendServer server) {
		for (int i = 0; i < server.getWeight(); i++) {
			servers.add(server);
		}
	}

	public void delServer(BackendServer server) {
		servers.removeIf(server1 -> server1.getIp().equals(server.getIp()) && server1.getPort() == server.getPort());
	}
}
