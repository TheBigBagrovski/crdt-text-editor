package okp.nic.network;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
@Slf4j
public class Messenger {
    private final Controller controller;

    private final List<String> connectedPeerList = new ArrayList<>();

    private final String host;
    private final int port;

    private PeerServer peerServer;
    private SignalClient signalClient;
    private final String serverAddress;

    private final Gson gson = new Gson();

    public Messenger(String host, int port, Controller controller, String signalHost, String signalPort) {
        this.host = host;
        this.port = port;
        this.controller = controller;
        serverAddress = "ws://" + signalHost + ":" + signalPort;
        init();
    }

    public void init() {
        startServerPeer();
        connectToSignalServer();
    }

    public void startServerPeer() {
        peerServer = new PeerServer(new InetSocketAddress(host, port));
        peerServer.setMessenger(this);
        peerServer.start();
    }

    public void connectToSignalServer() {
        try {
            signalClient = new SignalClient(new URI(serverAddress + "?address=" + "ws://" + host + ":" + port), this);
            signalClient.connectBlocking();
        } catch (Exception ex) {
            log.error("Ошибка при подключении к сигнальному серверу: " + ex);
        }
    }

    public void handleRemotePeerConnected(String peerAddress) {
        String myFullAddress = "ws://" + host + ":" + port;
        log.info(myFullAddress + " начинает соединение с пиром " + peerAddress);
        while (!connectedPeerList.contains(peerAddress)) {
            try {
                PeerClient peerNode = new PeerClient(new URI(peerAddress + "?address=" + myFullAddress));
                peerNode.setMessenger(this);
                boolean isSucceeded = peerNode.connectBlocking();
                if (isSucceeded) {
                    connectedPeerList.add(peerAddress);
                } else {
                    log.error("Не удалось подключиться к пиру " + peerAddress);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        log.error("Ошибка прерывания потока в ожидании");
                    }
                }
            } catch (Exception ex) {
                log.error("Ошибка при подключении к пиру");
            }
        }
    }

    public void handleRemotePeerDisconnected(String disconnectedPeer) {
        for (String s : connectedPeerList) {
            if (s.equals(disconnectedPeer)) {
                connectedPeerList.remove(disconnectedPeer);
            }
        }
    }

    public void broadcastInsert(char value, int position) {
//        log.info(connectedPeerList.toString());
        Operation op = new Operation(value, "insert", position);
        String payload = gson.toJson(op);
        peerServer.broadcast(payload);
    }

    public void broadcastDelete(int position) {
//        System.out.println("[broadcastDelete] START");
//        System.out.println("[broadcastDelete] >> preparing delete operation");
        Operation op = new Operation('!', "delete", position);
//        System.out.println("[broadcastDelete] >> jsonify operation");
        String payload = gson.toJson(op);
//        System.out.println("[broadcastDelete] call serverPeer->broadcast");
        peerServer.broadcast(payload);
//        System.out.println("[broadcastDelete] FINISH");
    }

    public void handleRemoteInsert(int position, char value) {
        controller.handleRemoteInsert( value, position);
    }

    public void handleRemoteDelete(int position) {
        controller.handleRemoteDelete(position);
    }

    public void sendCurrentState(WebSocket conn) {
        String text = controller.getDocument().content();
        conn.send("INITIAL_STATE:" + text);
    }

}
