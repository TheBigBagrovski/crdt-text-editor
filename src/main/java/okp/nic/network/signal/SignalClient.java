package okp.nic.network.signal;

import lombok.extern.slf4j.Slf4j;
import okp.nic.network.Messenger;
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
        for (SignalMessageType type : SignalMessageType.values()) {
            if (message.startsWith(type.getPrefix())) {
                String content = message.substring(type.getPrefix().length());
                switch (type) {
                    case WELCOME:
                        messenger.startServerPeer();
                        if (!content.isEmpty() && !content.equals("NONE")) {
                            String[] peers = content.split(", ");
                            for (String peer : peers) {
                                messenger.handleRemotePeerConnected(peer);
                            }
                        }
                        break;
                    case PEER_CONNECTED:
                        messenger.handleRemotePeerConnected(content);
                        break;
                    case PEER_DISCONNECTED:
                        messenger.handleRemotePeerDisconnected(content);
                        break;
                    case INITIAL_TEXT_REQUEST:
                        messenger.handleRemoteCurrentStateRequest(content);
                        break;
                }
                break; // найден тип сообщения, выход из цикла
            }
        }
    }

}
