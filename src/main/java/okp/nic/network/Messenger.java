package okp.nic.network;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Getter
@Slf4j
public class Messenger {


    private final List<PeerClient> connectedPeerList = new ArrayList<>();

    private final String host;
    private final int port;

    private final Controller controller;
    private PeerServer peerServer;
    private SignalClient signalClient;

    private final Gson gson = new Gson();

    public Messenger(String host, int port, Controller controller, String signalHost, String signalPort) {
        this.host = host;
        this.port = port;
        this.controller = controller;
        startServerPeer();
        connectToSignalServer("ws://" + signalHost + ":" + signalPort);
    }

    public void startServerPeer() {
        peerServer = new PeerServer(new InetSocketAddress(host, port), this);
        peerServer.start();
    }

    public void connectToSignalServer(String signalServerAddress) {
        try {
            signalClient = new SignalClient(new URI(signalServerAddress + "?address=" + "ws://" + host + ":" + port), this);
            signalClient.connectBlocking();
        } catch (Exception ex) {
            log.error("Ошибка при подключении к сигнальному серверу: " + ex);
        }
    }

    public void broadcastInsert(char value, int position) {
        Operation op = new Operation(value, "insert", position);
        String payload = gson.toJson(op);
        peerServer.broadcast(payload);
    }

    public void broadcastDelete(int position) {
        Operation op = new Operation('!', "delete", position);
        String payload = gson.toJson(op);
        peerServer.broadcast(payload);
    }

//    public void broadcastClear() {
//        Operation op = new Operation('!', "clear", 0);
//        String payload = gson.toJson(op);
//        peerServer.broadcast(payload);
//    }

    public void broadcastTextBlock(byte[] compressedBlock) {
        String payload = "COMPRESSED_TEXT:" + Base64.getEncoder().encodeToString(compressedBlock);
        peerServer.broadcast(payload);
    }

    public void handleRemotePeerConnected(String peerAddress) {
        String myFullAddress = "ws://" + host + ":" + port;
        log.info(myFullAddress + " начинает соединение с пиром " + peerAddress);
        try {
            PeerClient peerNode = new PeerClient(new URI(peerAddress), this);
            while (!connectedPeerList.contains(peerNode)) {
                boolean isSucceeded = peerNode.connectBlocking();
                if (isSucceeded) {
                    connectedPeerList.add(peerNode);
                } else {
                    log.error("Не удалось подключиться к пиру " + peerAddress);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        log.error("Ошибка прерывания потока в ожидании");
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Ошибка при подключении к пиру");
        }
    }

    public void handleRemotePeerDisconnected(String disconnectedPeer) {
        connectedPeerList.removeIf(peer -> peer.getRemotePeerAddress().equals("ws://" + disconnectedPeer));
    }

    public void handleRemoteInsert(String from, int position, char value) {
        controller.handleRemoteInsert(from, value, position);
    }

    public void handleRemoteDelete(int position) {
        controller.handleRemoteDelete(position);
    }

    public void handleRemoteCurrentStateRequest(String peerAddress) {
        byte[] text = controller.getCompressedText();
        try {
            boolean isSucceeded = false;
            for (PeerClient peer : connectedPeerList) {
                if (peerAddress.equals(peer.getRemotePeerAddress())) {
                    peer.send("CURRENT_STATE:" + "ws:/" + peerServer.getAddress() + ":FROM:" + text);
                    isSucceeded = true;
                }
            }
            if (!isSucceeded) {
                log.error("Не удалось отправить сообщение с текстом пиру " + peerAddress + ", нет подключения");
            }
        } catch (Exception ex) {
            log.error("Ошибка при подключении к пиру");
        }
    }

    public void handleRemoteTextInsert(String from, String compressedText) {
        byte[] decodedBlock = Base64.getDecoder().decode(compressedText);
        controller.insertText(from, decodedBlock);
    }

}
