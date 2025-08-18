package info.kgeorgiy.ja.morozov.hello;

import info.kgeorgiy.java.advanced.hello.NewHelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

public class HelloUDPNonblockingClient implements NewHelloClient {
    private final int RECEIVE_TIMEOUT = 100;

    private static class ChannelInfo {
        public int requestNumber = 0;
        public int channelNumber;
        ByteBuffer buffer;

        public ChannelInfo(int channelNumber, int capacity) {
            this.channelNumber = channelNumber;
            this.buffer = ByteBuffer.allocate(capacity);
        }
    }

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
            NewHelloClient.Request request,
            DatagramChannel ch,
            ByteBuffer buffer
    ) {
        String requestMsg = request.template().replace("$", Integer.toString(threadNumber));
        byte[] msg = requestMsg.getBytes(StandardCharsets.UTF_8);
        buffer.clear();
        buffer.put(msg, 0, msg.length);
        buffer.flip();
        InetSocketAddress socketAddress = new InetSocketAddress(request.host(), request.port());
        while (true) {
            try {
                ch.send(buffer, socketAddress);
                break;
            } catch (IOException ignored) {
                //System.out.println("ok");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void newRun(List<NewHelloClient.Request> requests, int threads) {
        Selector selector;
        try {
            selector = Selector.open();
            for (int i = 0; i < threads; i++) {
                DatagramChannel channel = DatagramChannel.open();
                channel.configureBlocking(false);
                channel.register(
                        selector,
                        SelectionKey.OP_WRITE | SelectionKey.OP_READ,
                        new ChannelInfo(i + 1, 1024)
                );
            }
        } catch (IOException ignored) {
            return;
        }

        int finished = threads;
        while (finished > 0) {
            try {
                selector.select(RECEIVE_TIMEOUT);
            } catch (IOException ignored) {
                continue;
            }
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isWritable()) {
                    ChannelInfo info = (ChannelInfo) key.attachment();
                    sendToServer(
                            info.channelNumber,
                            requests.get(info.requestNumber),
                            (DatagramChannel) key.channel(),
                            info.buffer
                    );
                    key.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    ChannelInfo info = (ChannelInfo) key.attachment();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    DatagramChannel channel = (DatagramChannel) key.channel();
                    try {
                        channel.receive(buffer);
                    } catch (IOException ignored) {
                        continue;
                    }
                    String requestMsg = requests.get(info.requestNumber)
                            .template().replace("$", Integer.toString(info.channelNumber));
                    if (checkResponse(requestMsg, new String(buffer.array(), 0, buffer.position(), StandardCharsets.UTF_8))) {
                        System.out.println(requestMsg + " " + buffer);
                        info.requestNumber++;
                    }
                    if (info.requestNumber == requests.size()) {
                        finished--;
                        continue;
                    }
                    key.interestOps(SelectionKey.OP_WRITE);
                }
                iterator.remove();
            }
        }
    }
}
