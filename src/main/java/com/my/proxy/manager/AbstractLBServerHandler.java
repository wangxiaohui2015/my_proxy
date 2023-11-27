package com.my.proxy.manager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.log4j.Logger;

/**
 * Abstract LB server handler.
 * 
 * @author sunny
 *
 */
public abstract class AbstractLBServerHandler implements Runnable {

    private static Logger logger = Logger.getLogger(AbstractLBServerHandler.class);

    public static final int CLIENT_TIMEOUT = 5000;
    public static final int SERVER_TIME_OUT = 5000;
    private static final int BUFFER_SIZE = 10240;

    protected Socket clientSocket;
    protected Socket serverSocket;

    public AbstractLBServerHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    protected void closeSocket() {
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

    protected static class TransferDataThread extends Thread {
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
            } catch (IOException ignore) {
            }
        }
    }
}
