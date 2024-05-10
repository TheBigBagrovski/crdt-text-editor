package okp.nic.network;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import java.net.URLEncoder;
import okp.nic.network.operation.DeleteOperation;
import okp.nic.network.operation.InsertOperation;
import okp.nic.network.peer.PeerClient;
import okp.nic.network.peer.PeerMessageType;
import okp.nic.network.peer.PeerServer;
import okp.nic.network.signal.SignalClient;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static okp.nic.Utils.SALT;
import static okp.nic.Utils.getUtfString;

@Getter
@Slf4j
public class Messenger {


    private final Map<String, PeerClient> connectedPeers = new HashMap<>();
    private final Map<String, String> peerNames = new HashMap<>();

    private final String host;
    private final int port;

    private final Controller controller;
    private PeerServer peerServer;
    private SignalClient signalClient;
    private final String name;

    private final Gson gson = new Gson();

    public Messenger(String host, int port, Controller controller, String signalHost, String signalPort, String password, String name) {
        this.host = host;
        this.port = port;
        this.controller = controller;
        this.name = name;
        connectToSignalServer("ws://" + signalHost + ":" + signalPort, password, name);
    }

    public void startServerPeer() {
        controller.start(this, name);
        peerServer = new PeerServer(new InetSocketAddress(host, port), this);
        peerServer.setConnectionLostTimeout(0);
        peerServer.start();
    }

    public void connectToSignalServer(String signalServerAddress, String password, String name) {
        try {
            String hashedPassword = BCrypt.withDefaults().hashToString(6, (SALT + password).toCharArray());
            signalClient = new SignalClient(new URI(
                    getUtfString(signalServerAddress + "?address=" + "ws://" + host + ":" + port + "&password=" + hashedPassword + "&name=" + URLEncoder.encode(name))), this);
            signalClient.connectBlocking();
        } catch (Exception ex) {
            log.error("Ошибка при подключении к сигнальному серверу: " + ex);
        }
    }

    public void broadcastInsert(char value, int position) {
        InsertOperation op = new InsertOperation(value, position);
        String payload = PeerMessageType.OPERATION.formatOperationMessage(op.getType(), gson.toJson(op));
        peerServer.broadcast(payload);
    }

    public void broadcastDelete(int position) {
        DeleteOperation op = new DeleteOperation(position);
        String payload = PeerMessageType.OPERATION.formatOperationMessage(op.getType(), gson.toJson(op));
        peerServer.broadcast(payload);
    }

    public void broadcastTextBlock(byte[] compressedBlock) {
        String payload = PeerMessageType.COMPRESSED_TEXT.formatMessage(Base64.getEncoder().encodeToString(compressedBlock));
        peerServer.broadcast(payload);
    }

    public void handleRemotePeerConnected(String peerAddress, String peerName) {
        String myFullAddress = "ws://" + host + ":" + port;
        log.info(myFullAddress + " начинает соединение с пиром " + peerAddress);
        try {
            PeerClient peerNode = new PeerClient(new URI(peerAddress), this);
            while (!connectedPeers.containsKey(peerAddress)) {
                boolean isSucceeded = peerNode.connectBlocking();
                peerNode.setConnectionLostTimeout(0);
                if (isSucceeded) {
                    connectedPeers.put(peerAddress, peerNode);
                    peerNames.put(peerAddress, peerName);
                    controller.handlePeerName(peerAddress, peerName);
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
        connectedPeers.remove("ws://" + disconnectedPeer);
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
            if (connectedPeers.containsKey(peerAddress)) {
                connectedPeers.get(peerAddress).send(PeerMessageType.CURRENT_STATE.formatMessage(
                        "ws:/" + peerServer.getAddress() + ":FROM:" + Base64.getEncoder().encodeToString(text)));
            } else {
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
