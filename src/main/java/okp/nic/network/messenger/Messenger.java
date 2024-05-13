package okp.nic.network.messenger;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.google.gson.Gson;
import lombok.Getter;
import okp.nic.logger.Logger;
import okp.nic.network.operation.DeleteOperation;
import okp.nic.network.operation.DeleteRangeOperation;
import okp.nic.network.operation.InsertBlockOperation;
import okp.nic.network.operation.InsertOperation;
import okp.nic.network.peer.PeerClient;
import okp.nic.network.peer.PeerMessageType;
import okp.nic.network.peer.PeerServer;
import okp.nic.network.signal.SignalClient;
import okp.nic.presenter.PresenterImpl;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static okp.nic.utils.TextCompressor.compress;
import static okp.nic.utils.TextCompressor.decompress;
import static okp.nic.utils.Utils.SALT;

@Getter
public class Messenger implements  PeerListener, PeerPublisher, SignalListener {

    private final Map<String, PeerClient> connectedPeers = new HashMap<>();
    private final Map<String, String> peerNames = new HashMap<>();

    private final String host;
    private final int port;
    private final String hostFullAddress;

    private PresenterImpl presenter;
    private PeerServer peerServer;
    private SignalClient signalClient;
    private final String name;
    private final String inputPassword;

    private final Gson gson = new Gson();

    public Messenger(String host, int port, String signalHost, String signalPort, String password, String name) {
        this.host = host;
        this.port = port;
        this.name = name;
        hostFullAddress = "ws://" + host + ":" + port;
        inputPassword = password;
        connectToSignalServer("ws://" + signalHost + ":" + signalPort, name);
    }

    @Override
    public void connectToSignalServer(String signalServerAddress, String name) {
        try {
            String uri = signalServerAddress + "?address=" + hostFullAddress + "&name=" + URLEncoder.encode(name);
            signalClient = new SignalClient(new URI(uri), this);
            signalClient.connectBlocking();
        } catch (Exception ex) {
            Logger.error("Ошибка при подключении к сигнальному серверу: " + ex);
        }
    }

    @Override
    public void startServerPeer() {
        presenter = new PresenterImpl(host, port);
        presenter.start(this, name);
        Logger.info("Указанный пароль верен");
        Logger.info("Получено WELCOME-сообщение от сигнального сервера");
        peerServer = new PeerServer(new InetSocketAddress(host, port), this);
        peerServer.setConnectionLostTimeout(0);
        peerServer.start();
    }

    @Override
    public void handlePasswordRequest() {
        String hashedPassword = BCrypt.withDefaults().hashToString(6, (SALT + inputPassword).toCharArray());
        signalClient.send(PeerMessageType.PASSWORD.formatMessage(hashedPassword));
    }

    @Override
    public void handleRemotePeerConnected(String peerAddress, String peerName) {
        Logger.info("Попытка соединения с пиром " + peerAddress);
        try {
            PeerClient peerNode = new PeerClient(new URI(peerAddress), this);
            while (!connectedPeers.containsKey(peerAddress)) {
                boolean isSucceeded = peerNode.connectBlocking();
                peerNode.setConnectionLostTimeout(0);
                if (isSucceeded) {
                    connectedPeers.put(peerAddress, peerNode);
                    peerNames.put(peerAddress, peerName);
                    presenter.addPeerName(peerAddress, peerName);
                } else {
                    Logger.error("Не удалось подключиться к пиру " + peerAddress);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Logger.error("Ошибка прерывания потока в ожидании");
                    }
                }
            }
        } catch (Exception ex) {
            Logger.error("Ошибка при подключении к пиру");
        }
    }

    @Override
    public void handleRemotePeerDisconnected(String disconnectedPeer) {
        Logger.info("Получено сообщение от сигнального сервера: " + disconnectedPeer + " отключается");
        connectedPeers.remove(disconnectedPeer);
        presenter.removePeerName(disconnectedPeer, peerNames.get(disconnectedPeer));
    }

    @Override
    public void broadcastInsert(char value, int pos) {
        InsertOperation op = new InsertOperation(pos, value);
        String payload = PeerMessageType.OPERATION.formatOperationMessage(op.getType(), gson.toJson(op));
        peerServer.broadcast(payload);
    }

    @Override
    public void broadcastDelete(int pos) {
        DeleteOperation op = new DeleteOperation(pos);
        String payload = PeerMessageType.OPERATION.formatOperationMessage(op.getType(), gson.toJson(op));
        peerServer.broadcast(payload);
    }

    @Override
    public void broadcastDeleteRange(int startPos, int endPos) {
        DeleteRangeOperation op = new DeleteRangeOperation(startPos, endPos);
        String payload = PeerMessageType.OPERATION.formatOperationMessage(op.getType(), gson.toJson(op));
        peerServer.broadcast(payload);
    }

    @Override
    public void broadcastTextUpdate(String text) {
        byte[] compressedText = compress(text);
        String payload = PeerMessageType.UPDATE_TEXT.formatTextUpdateMessage(hostFullAddress, Base64.getEncoder().encodeToString(compressedText));
        peerServer.broadcast(payload);
    }

    @Override
    public void broadcastInsertBlock(int pos, String text) {
        byte[] compressedBlock = compress(text);
        InsertBlockOperation op = new InsertBlockOperation(pos, Base64.getEncoder().encodeToString(compressedBlock));
        String payload = PeerMessageType.OPERATION.formatOperationMessage(op.getType(), gson.toJson(op));
        peerServer.broadcast(payload);
    }

    @Override
    public void broadcastChatMessage(String message) {
        String payload = PeerMessageType.CHAT_MESSAGE.formatMessage(message);
        peerServer.broadcast(payload);
    }

    @Override
    public void handleRemoteInsert(String from, int pos, char value) {
        presenter.handleRemoteInsert(from, value, pos);
    }

    @Override
    public void handleRemoteDelete(int pos) {
        presenter.handleRemoteDelete(pos);
    }

    @Override
    public void handleRemoteCurrentStateRequest(String peerAddress) {
        Logger.info("Получен запрос текста от сигнального сервера для " + peerAddress);
        byte[] text = compress(presenter.getCurrentDocument());
        try {
            if (connectedPeers.containsKey(peerAddress)) {
                connectedPeers.get(peerAddress).send(PeerMessageType.UPDATE_TEXT.formatTextUpdateMessage(
                        hostFullAddress, Base64.getEncoder().encodeToString(text)));
                Logger.info("Текущий файл отправлен " + peerAddress);
            } else {
                Logger.error("Не удалось отправить сообщение с текстом пиру " + peerAddress + ", нет подключения");
            }
        } catch (Exception ex) {
            Logger.error("Ошибка при подключении к пиру");
        }
    }

    @Override
    public void handleRemoteTextUpdate(String from, String compressedText) {
        Logger.info("Получено обновление файла от " + from);
        byte[] decodedBlock = Base64.getDecoder().decode(compressedText);
        String text = decompress(decodedBlock);
        presenter.handleRemoteTextUpdate(from, text);
    }

    @Override
    public void handleRemoteInsertBlock(String from, int pos, String compressedText) {
        byte[] decodedBlock = Base64.getDecoder().decode(compressedText);
        String text = decompress(decodedBlock);
        presenter.handleRemoteInsertBlock(from, pos, text);
    }

    @Override
    public void handleRemoteDeleteRange(int startPos, int endPos) {
        presenter.handleRemoteDeleteRange(startPos, endPos);
    }

    @Override
    public void handleRemoteChatMessage(String from, String message) {
        presenter.handleRemoteChatMessage(peerNames.get(from), message);
    }

}
