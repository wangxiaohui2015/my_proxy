
package com.my.proxy.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.my.proxy.entity.BackendServer;
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

    private ExecutorService mainService =
                    Executors.newFixedThreadPool(ConfigUtil.getInstance().getLbThreads());

    private LBServerManager() {}

    public static final LBServerManager getInstance() {
        return instance;
    }

    public void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(ConfigUtil.getInstance().getLbPort());
            logger.info("Start LB server successfully on port:"
                            + ConfigUtil.getInstance().getLbPort());
            while (true) {
                mainService.submit(new LBServerHandler(serverSocket.accept()));
            }
        } catch (IOException e) {
            logger.error("failed to start LBServer,", e);
        }
    }


    private static class LBServerHandler implements Runnable {

        public static final int CLIENT_TIMEOUT = 5000;
        public static final int SERVER_TIME_OUT = 5000;
        private static final int BUFFER_SIZE = 10240;

        private Socket clientSocket;
        private Socket serverSocket;

        public LBServerHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                logger.info(clientSocket.getRemoteSocketAddress().toString().replace("/", "")
                                + " connected to LB server.");
                clientSocket.setSoTimeout(CLIENT_TIMEOUT);

                // Get a server to handle client request
                BackendServer backendServer = LBManager.getInstance().getBackendServer(
                                clientSocket.getInetAddress().getHostAddress(),
                                clientSocket.getPort());
                logger.info("Got back end server: '" + backendServer.getName() + "'");
                serverSocket = new Socket();
                serverSocket.connect(new InetSocketAddress(backendServer.getIp(),
                                backendServer.getPort()));
                serverSocket.setSoTimeout(SERVER_TIME_OUT);

                // Create two threads to transfer data
                Thread t1 = new TransferDataThread(serverSocket.getInputStream(),
                                clientSocket.getOutputStream());
                Thread t2 = new TransferDataThread(clientSocket.getInputStream(),
                                serverSocket.getOutputStream());
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

        private void closeSocket() {
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (IOException e1) {
                logger.error("failed to close client socket.");
            }
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException e1) {
                logger.error("failed to close server socket.");
            }
        }

        private static class TransferDataThread extends Thread {
            private InputStream in;
            private OutputStream out;

            public TransferDataThread(InputStream in, OutputStream out) {
                this.in = in;
                this.out = out;
            }

            @Override
            public void run() {
                try {
                    byte[] data = new byte[BUFFER_SIZE];
                    int len = -1;
                    while ((len = in.read(data)) != -1) {
                        out.write(data, 0, len);
                    }
                } catch (IOException e) {
                    // Ignore this error.
                }
            }
        }
    }
}
