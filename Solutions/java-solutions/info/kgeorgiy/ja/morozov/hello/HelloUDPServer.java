package info.kgeorgiy.ja.morozov.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

/**
 * This class can be used as a simple udp server.
 * It cans receive requests and give responses from different threads.
 */
public class HelloUDPServer implements HelloServer {
    private DatagramSocket mainSocket;
    private ExecutorService mainExecutor;
    // :NOTE: not needed
    private final BlockingQueue<DatagramPacket> freePacketQueue = new LinkedBlockingQueue<>();
    private final int CLOSE_TIMEOUT = 1000;

    private void sendToClient(DatagramPacket packet) {
        try {
            String request = "Hello, " +
                    new String(
                            packet.getData(),
                            packet.getOffset(),
                            packet.getLength(),
                            StandardCharsets.UTF_8
                    );
            byte[] responseMsg = request.getBytes(StandardCharsets.UTF_8);
            if (responseMsg.length <= packet.getData().length) {
                System.arraycopy(responseMsg, 0, packet.getData(), 0, responseMsg.length);
            } else {
                packet.setData(responseMsg);
            }
            packet.setLength(responseMsg.length);
            mainSocket.send(packet);
        } catch (IOException ignored) {
        } finally {
            freePacketQueue.add(packet);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(int port, int threads) {
        mainExecutor = Executors.newFixedThreadPool(threads + 1);
        int length;
        try {
            mainSocket = new DatagramSocket(port);
            length = mainSocket.getSendBufferSize();
            for (int i = 0; i < threads; i++) {
                freePacketQueue.add(new DatagramPacket(new byte[length], length));
            }
        } catch (SocketException e) {
            System.err.println("There is a problem with the socket: " + e.getMessage());
            return;
        }
        mainExecutor.execute(() -> {
            while (!mainSocket.isClosed()) {
                // :NOTE: use package
                DatagramPacket packet = null;
                try {
                    packet = freePacketQueue.take();
                    mainSocket.receive(packet);
                    DatagramPacket finalPacket = packet;
                    try {
                        mainExecutor.execute(() -> sendToClient(finalPacket));
                    } catch (RejectedExecutionException ignored) {}
                } catch (IOException | InterruptedException ignored) {
                    if (packet != null) {
                        freePacketQueue.add(packet);
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        mainSocket.close();
        mainExecutor.shutdown();
        try {
           if (!mainExecutor.awaitTermination(CLOSE_TIMEOUT, TimeUnit.MILLISECONDS)) {
               System.err.println("Timed out waiting for main thread to terminate");
           }
        } catch (InterruptedException ignored) {
            mainExecutor.shutdownNow();
        }
    }
}
