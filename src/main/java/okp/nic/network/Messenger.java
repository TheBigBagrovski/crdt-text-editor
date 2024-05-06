package okp.nic.network;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okp.nic.crdt.Char;
import okp.nic.vectorclock.VersionVector;
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
        peerServer = new PeerServer(new InetSocketAddress(host, port), this);
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
                PeerClient peerNode = new PeerClient(new URI(peerAddress + "?address=" + myFullAddress), this);

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

    public void broadcastInsert(Char data) {
//        log.info(connectedPeerList.toString());
        Operation op = new Operation(data, "insert");
        String payload = gson.toJson(op);
        peerServer.broadcast(payload);
    }

    public void broadcastDelete(Char data) {
//        System.out.println("[broadcastDelete] START");
//        System.out.println("[broadcastDelete] >> preparing delete operation");
        Operation op = new Operation(data, "delete");
//        System.out.println("[broadcastDelete] >> jsonify operation");
        String payload = gson.toJson(op);
//        System.out.println("[broadcastDelete] call serverPeer->broadcast");
        peerServer.broadcast(payload);
//        System.out.println("[broadcastDelete] FINISH");
    }

    public void handleRemoteInsert(Char data) {
        System.out.println("MeshandleRemoteInsert");

        controller.handleRemoteInsert(data);
    }

    public void handleRemoteDelete(Char data) {
        controller.handleRemoteDelete(data);
    }

//    public void sendCurrentState(WebSocket conn) {
//        List<Char> text = controller.getCurrentText();
//        conn.send("SIGNAL:INITIAL_STATE:" + gson.toJson(text));
//    }
    public void sendCurrentState(WebSocket conn) {
//        List<Char> text = controller.getCurrentText();
        StringBuilder sb = new StringBuilder();
        for (Char c : controller.getCurrentText()) {
            sb.append(c.getValue());
        }
        conn.send("SIGNAL:INITIAL_STATE:" + sb);
    }

}
