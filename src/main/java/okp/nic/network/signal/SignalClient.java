package okp.nic.network.signal;

import lombok.extern.slf4j.Slf4j;
import okp.nic.network.messenger.Messenger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import static okp.nic.utils.Utils.getUtfString;

@Slf4j
public class SignalClient extends WebSocketClient {

    private final Messenger messenger;

    public SignalClient(URI serverURI, Messenger messenger) {
        super(serverURI);
        this.messenger = messenger;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.info(getUtfString("Соединение с сигнальным сервером установлено"));
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info(getUtfString("Соединение с сигнальным сервером прервано с кодом " + code + ", причина: ") + reason);
    }

    @Override
    public void onMessage(String message) {
        handleSignalServerMessage(message);
    }

    @Override
    public void onError(Exception ex) {
        log.error(getUtfString("Ошибка при подключении к сигнальному серверу: " + ex));
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
                                String[] info = peer.split("-");
                                messenger.handleRemotePeerConnected(info[0], info[1]);
                            }
                        }
                        break;
                    case PEER_CONNECTED:
                        String[] info = content.split("-");
                        messenger.handleRemotePeerConnected(info[0], info[1]);
                        break;
                    case PEER_DISCONNECTED:
                        messenger.handleRemotePeerDisconnected(content);
                        break;
                    case INITIAL_TEXT_REQUEST:
                        messenger.handleRemoteCurrentStateRequest(content);
                        break;
                    case PASSWORD_REQUEST:
                        messenger.handlePasswordRequest();
                        break;
                }
                break; // найден тип сообщения, выход из цикла
            }
        }
    }

}
