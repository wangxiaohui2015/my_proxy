package com.my.proxy.manager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.my.proxy.entity.BackendServer;

/**
 * LB server L7 handler.
 * 
 * @author sunny
 *
 */
public class LBServerL7Handler extends AbstractLBServerHandler {

	private static Logger logger = Logger.getLogger(LBServerL7Handler.class);

	public LBServerL7Handler(Socket clientSocket) {
		super(clientSocket);
	}

	@Override
	public void run() {
		try {
			logger.info(clientSocket.getRemoteSocketAddress().toString().replace("/", "") + " connected to LB server.");
			clientSocket.setSoTimeout(CLIENT_TIMEOUT);

			InputStream clientIn = clientSocket.getInputStream();
			ByteArrayOutputStream bOut = new ByteArrayOutputStream();
			String uri = getRequestURI(clientIn, bOut);

			// Get a server to handle client request
			Map<String, String> params = new HashMap<String, String>();
			params.put(LBManager.PARAM_KEY_CLIENT_IP, clientSocket.getInetAddress().getHostAddress());
			params.put(LBManager.PARAM_KEY_CLIENT_PORT, String.valueOf(clientSocket.getPort()));
			params.put(LBManager.PARAM_KEY_CLIENT_URI, String.valueOf(uri));
			BackendServer backendServer = LBManager.getInstance().getBackendServer(params);
			logger.info("Got back end server: '" + backendServer.getName() + "'");

			serverSocket = new Socket();
			serverSocket.connect(new InetSocketAddress(backendServer.getIp(), backendServer.getPort()));
			serverSocket.setSoTimeout(SERVER_TIME_OUT);
			OutputStream serverOut = serverSocket.getOutputStream();

			serverOut.write(bOut.toByteArray());

			// Create two threads to transfer data
			Thread t1 = new TransferDataThread(serverSocket.getInputStream(), clientSocket.getOutputStream());
			Thread t2 = new TransferDataThread(clientIn, serverOut);
			t1.start();
			t2.start();
			t1.join();
			t2.join();
		} catch (Exception e) {
			logger.error("Exception occurred while handling client request.", e);
		} finally {
			closeSocket();
		}
	}

	private String getRequestURI(InputStream in, ByteArrayOutputStream bOut) {
		int data = -1;
		StringBuffer sb = new StringBuffer();
		try {
			while (data != '\r' && data != '\n') {
				data = in.read();
				sb.append((char) data);
				bOut.write(data);
			}
		} catch (IOException ignore) {
		}
		String line = sb.toString();
		if ("".equals(line)) {
			return "";
		}
		String[] strArr = line.split(" ");
		if (strArr.length >= 2) {
			return strArr[1];
		}
		return "";
	}
}
