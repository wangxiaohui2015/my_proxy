package com.my.proxy.manager;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.my.proxy.entity.BackendServer;
import com.my.proxy.util.ConfigUtil;

/**
 * LB server L4 handler.
 * 
 * @author sunny
 *
 */
public class LBServerL4Handler extends AbstractLBServerHandler {

    private static Logger logger = Logger.getLogger(LBServerL4Handler.class);

    public LBServerL4Handler(Socket clientSocket) {
        super(clientSocket);
    }

    @Override
    public void run() {
        try {
            logger.info(clientSocket.getRemoteSocketAddress().toString().replace("/", "") + " connected to LB server.");
            clientSocket.setSoTimeout(ConfigUtil.getInstance().getLbClientTimeout());

            // Get a server to handle client request
            Map<String, String> params = new HashMap<String, String>();
            params.put(LBManager.PARAM_KEY_CLIENT_IP, clientSocket.getInetAddress().getHostAddress());
            params.put(LBManager.PARAM_KEY_CLIENT_PORT, String.valueOf(clientSocket.getPort()));
            BackendServer backendServer = LBManager.getInstance().getBackendServer(params);
            logger.info("Got back end server: " + backendServer.getName() + ", ip: " + backendServer.getIp()
                    + ", port: " + backendServer.getPort());

            serverSocket = new Socket();
            serverSocket.connect(new InetSocketAddress(backendServer.getIp(), backendServer.getPort()));
            serverSocket.setSoTimeout(ConfigUtil.getInstance().getLbServerTimeout());

            // Create two threads to transfer data
            Thread t1 = new TransferDataThread(serverSocket.getInputStream(), clientSocket.getOutputStream());
            Thread t2 = new TransferDataThread(clientSocket.getInputStream(), serverSocket.getOutputStream());
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
}
