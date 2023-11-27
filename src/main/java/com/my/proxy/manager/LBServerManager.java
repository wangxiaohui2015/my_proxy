
package com.my.proxy.manager;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.my.proxy.util.ConfigUtil;

/**
 * This is LB server manager, used to handle client request.
 * 
 * @author sunny
 *
 */
public class LBServerManager {

	private static Logger logger = Logger.getLogger(LBServerManager.class);
	private static final LBServerManager instance = new LBServerManager();

	private ExecutorService service = Executors.newFixedThreadPool(ConfigUtil.getInstance().getLbThreads());

	private LBServerManager() {
	}

	public static final LBServerManager getInstance() {
		return instance;
	}

	public void startServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(ConfigUtil.getInstance().getLbPort());
			logger.info("Started LB server successfully on port:" + ConfigUtil.getInstance().getLbPort());
			if (LBManager.getInstance().isL4Protocol()) {
				startServerForL4Proxy(serverSocket);
			} else if (LBManager.getInstance().isL7Protocol()) {
				startServerForL7Proxy(serverSocket);
			} else {
				logger.warn("Unknown protocol: " + ConfigUtil.getInstance().getLbProtocol() + ", will start L4 proxy.");
				startServerForL4Proxy(serverSocket);
			}
		} catch (IOException e) {
			logger.error("Failed to start LBServer,", e);
		}
	}

	private void startServerForL4Proxy(ServerSocket serverSocket) throws IOException {
		while (true) {
			service.submit(new LBServerL4Handler(serverSocket.accept()));
		}
	}

	private void startServerForL7Proxy(ServerSocket serverSocket) throws IOException {
		while (true) {
			service.submit(new LBServerL7Handler(serverSocket.accept()));
		}
	}
}
