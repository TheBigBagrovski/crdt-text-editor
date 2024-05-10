package okp.nic.network;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import lombok.Getter;
import okp.nic.logger.Logger;
import okp.nic.network.operation.DeleteOperation;
import okp.nic.network.operation.InsertOperation;
import okp.nic.network.peer.PeerClient;
import okp.nic.network.peer.PeerMessageType;
import okp.nic.network.peer.PeerServer;
import okp.nic.network.signal.SignalClient;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static okp.nic.Utils.SALT;

@Getter
public class Messenger {

    private final Map<String, PeerClient> connectedPeers = new HashMap<>();
    private final Map<String, String> peerNames = new HashMap<>();

    private final String host;
    private final int port;
    private final String hostFullAddress;

    private Controller controller;
    private PeerServer peerServer;
    private SignalClient signalClient;
    private final String name;
    private final String inputPassword;

    private final Gson gson = new Gson();

    private Logger logger;

    public Messenger(String host, int port, String signalHost, String signalPort, String password, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
        hostFullAddress = "ws://" + host + ":" + port;
        inputPassword = password;
        connectToSignalServer("ws://" + signalHost + ":" + signalPort, name);
    }

    public void connectToSignalServer(String signalServerAddress, String name) {
        try {
            String uri = signalServerAddress + "?address=" + hostFullAddress + "&name=" + URLEncoder.encode(name);
            signalClient = new SignalClient(new URI(uri), this);
            signalClient.connectBlocking();
        } catch (Exception ex) {
            logger.error("Ошибка при подключении к сигнальному серверу: " + ex);
        }
    }

    public void startServerPeer() {
        controller = new Controller(host, port);
        controller.start(this, name);
        logger = controller.getLogger();
        logger.info("Указанный пароль верен");
        logger.info("Получено WELCOME-сообщение от сигнального сервера");
        peerServer = new PeerServer(new InetSocketAddress(host, port), this, logger);
        peerServer.setConnectionLostTimeout(0);
        peerServer.start();
    }

    public void handlePasswordRequest() {
        String hashedPassword = BCrypt.withDefaults().hashToString(6, (SALT + inputPassword).toCharArray());
        signalClient.send(PeerMessageType.PASSWORD.formatMessage(hashedPassword));
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

    public void broadcastTextUpdate(byte[] compressedBlock) {
        String payload = PeerMessageType.UPDATE_TEXT.formatTextUpdateMessage(hostFullAddress, Base64.getEncoder().encodeToString(compressedBlock));
        peerServer.broadcast(payload);
    }

    public void broadcastTextBlock(byte[] compressedBlock, int pos) {
        String payload = PeerMessageType.TEXT_BLOCK.formatTextBlockMessage(pos, Base64.getEncoder().encodeToString(compressedBlock));
        peerServer.broadcast(payload);
    }

    public void handleRemotePeerConnected(String peerAddress, String peerName) {
        logger.info("Попытка соединения с пиром " + peerAddress);
        try {
            PeerClient peerNode = new PeerClient(new URI(peerAddress), this, logger);
            while (!connectedPeers.containsKey(peerAddress)) {
                boolean isSucceeded = peerNode.connectBlocking();
                peerNode.setConnectionLostTimeout(0);
                if (isSucceeded) {
                    connectedPeers.put(peerAddress, peerNode);
                    peerNames.put(peerAddress, peerName);
                    controller.addPeerName(peerAddress, peerName);
                } else {
                    logger.error("Не удалось подключиться к пиру " + peerAddress);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        logger.error("Ошибка прерывания потока в ожидании");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Ошибка при подключении к пиру");
        }
    }

    public void handleRemotePeerDisconnected(String disconnectedPeer) {
        logger.info("Получено сообщение от сигнального сервера: " + disconnectedPeer + " отключается");
        connectedPeers.remove(disconnectedPeer);
        controller.removePeerName(disconnectedPeer, peerNames.get(disconnectedPeer));
    }

    public void handleRemoteInsert(String from, int position, char value) {
        controller.handleRemoteInsert(from, value, position);
    }

    public void handleRemoteDelete(int position) {
        controller.handleRemoteDelete(position);
    }

    public void handleRemoteCurrentStateRequest(String peerAddress) {
        logger.info("Получен запрос текста от сигнального сервера для " + peerAddress);
        byte[] text = controller.getCompressedText();
        try {
            if (connectedPeers.containsKey(peerAddress)) {
                connectedPeers.get(peerAddress).send(PeerMessageType.UPDATE_TEXT.formatTextUpdateMessage(
                        hostFullAddress, Base64.getEncoder().encodeToString(text)));
                logger.info("Текущий файл отправлен " + peerAddress);
            } else {
                logger.error("Не удалось отправить сообщение с текстом пиру " + peerAddress + ", нет подключения");
            }
        } catch (Exception ex) {
            logger.error("Ошибка при подключении к пиру");
        }
    }

    public void handleUpdateText(String from, String compressedText) {
        logger.info("Получено обновление файла от " + from);
        byte[] decodedBlock = Base64.getDecoder().decode(compressedText);
        controller.updateText(from, decodedBlock);
    }

    public void handleRemoteBlockInsert(String from, int pos, String compressedText) {
        byte[] decodedBlock = Base64.getDecoder().decode(compressedText);
        controller.insertTextBlock(from, pos, decodedBlock);
    }

}
