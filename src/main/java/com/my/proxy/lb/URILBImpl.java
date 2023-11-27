package com.my.proxy.lb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.my.proxy.entity.BackendServer;
import com.my.proxy.manager.LBManager;
import com.my.proxy.util.ConfigUtil;

/**
 * LB weight URI algorithm, this is used for HTTP L7 proxy.
 * 
 * @author sunny
 *
 */
public class URILBImpl extends AbstractLoadBalancer {

	private Random random = new Random();

	@Override
	protected BackendServer getBackendServer(Map<String, String> params) throws Exception {
		String clientUri = params.get(LBManager.PARAM_KEY_CLIENT_URI);
		initBackendServersByURI(clientUri);
		return servers.get(random.nextInt(servers.size()));
	}

	private void initBackendServersByURI(String clientURI) {
		List<BackendServer> configuredServers = ConfigUtil.getInstance().getBackendServers();
		List<BackendServer> serverList = new ArrayList<>();
		configuredServers.forEach(server -> {
			if (!clientURI.matches(server.getUri())) {
				return;
			}
			for (int i = 0; i < server.getWeight(); i++) {
				serverList.add(server);
			}
		});
		Collections.shuffle(serverList);
		if (!serverList.isEmpty()) {
			servers = Collections.synchronizedList(serverList);
		}
	}
}
