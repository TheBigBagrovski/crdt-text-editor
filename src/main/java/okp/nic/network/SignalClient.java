package okp.nic.network;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

@Slf4j
public class SignalClient extends WebSocketClient {

    private final Messenger messenger;

    public SignalClient(URI serverURI, Messenger messenger) {
        super(serverURI);
        this.messenger = messenger;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.info("Соединение с сигнальным сервером установлено");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Соединение с сигнальным сервером прервано с кодом " + code + ", причина: " + reason);
    }

    @Override
    public void onMessage(String message) {
        log.info("Получено сообщение от сигнального сервера: " + message);
        handleSignalServerMessage(message);
    }

    @Override
    public void onError(Exception ex) {
        log.error("Ошибка при подключении к сигнальному серверу: " + ex);
    }

    private void handleSignalServerMessage(String message) {
        if (message.startsWith("SIGNAL:CONNECTED:")) {
            String peerAddress = message.substring("SIGNAL:CONNECTED:".length());
            messenger.handleRemotePeerConnected(peerAddress);
        } else if (message.startsWith("SIGNAL:DISCONNECTED:")) {
            String peerAddress = message.substring("SIGNAL:DISCONNECTED:".length());
            messenger.handleRemotePeerDisconnected(peerAddress);
        } else if (message.startsWith("SIGNAL:INITIAL:")) {
            String peersList = message.substring("SIGNAL:INITIAL:".length());
            if (!peersList.isBlank()) {
                String[] peers = peersList.split(", ");
                for (String peer : peers) {
                    messenger.handleRemotePeerConnected(peer);
                }
            }
        }
    }
}