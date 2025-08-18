package info.kgeorgiy.ja.morozov.hello;

import info.kgeorgiy.java.advanced.hello.NewHelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class can be used as a simple udp client that can send requests and receive responses.
 * Requests can be sent from different threads.
 */
public class HelloUDPClient implements NewHelloClient {
    private final int RECEIVE_TIMEOUT = 100;

    private boolean checkResponse(String request, String response) {
        StringBuilder inNormalFormat = new StringBuilder();
        for (int i = 0; i < response.length(); i++) {
            char c = response.charAt(i);
            if (Character.isDigit(c)) {
                inNormalFormat.append(Character.getNumericValue(c));
            } else {
                inNormalFormat.append(c);
            }
        }
        int pos = inNormalFormat.indexOf(request);
        if (pos == -1) {
            return false;
        }
        pos += request.length();
        return pos >= inNormalFormat.length() || !Character.isDigit(inNormalFormat.charAt(pos));
    }

    private void sendToServer(
            int threadNumber,
            Request request,
            DatagramSocket curSocket,
            DatagramPacket requestPacket,
            DatagramPacket responsePacket
    ) throws UnknownHostException, SocketException {

        String requestMsg = request.template().replace("$", Integer.toString(threadNumber));
        byte[] msg = requestMsg.getBytes(StandardCharsets.UTF_8);
        InetAddress addr = InetAddress.getByName(request.host());
        if (requestPacket.getData().length >= msg.length) {
            System.arraycopy(msg, 0, requestPacket.getData(), 0, msg.length);
        } else {
            requestPacket.setData(msg);
        }
        requestPacket.setLength(msg.length);
        requestPacket.setAddress(addr);
        requestPacket.setPort(request.port());
        while (true) {
            try {
                curSocket.send(requestPacket);
                curSocket.receive(responsePacket);
                String responseMsg = new String(
                        responsePacket.getData(),
                        responsePacket.getOffset(),
                        responsePacket.getLength(),
                        StandardCharsets.UTF_8
                );
                if (!checkResponse(requestMsg, responseMsg)) {
                    continue;
                }
                System.out.println(request + " " + responseMsg);
                break;
            } catch (IOException ignored) {

            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newRun(List<Request> requests, int threads) {
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 0; i < threads; i++) {
                int finalI = i;
                executor.execute(() -> {
                    try (DatagramSocket curSocket = new DatagramSocket()) {
                        curSocket.setSoTimeout(RECEIVE_TIMEOUT);
                        DatagramPacket requestPacket = new DatagramPacket(
                                new byte[curSocket.getSendBufferSize()],
                                curSocket.getReceiveBufferSize()
                        );
                        DatagramPacket responsePacket = new DatagramPacket(
                                new byte[curSocket.getReceiveBufferSize()],
                                curSocket.getSendBufferSize()
                        );
                        for (Request request : requests) {
                            try {
                                sendToServer(finalI + 1, request, curSocket, requestPacket, responsePacket);
                            } catch (SocketException e) {
                                System.err.println("There is an error with the socket " + e);
                            } catch (UnknownHostException e) {
                                System.err.println("Unknown host: " + e);
                            }
                        }
                    } catch (SocketException e) {
                        System.err.println("There is an error with the socket " + e);
                    }
                });
            }
        }
    }
}