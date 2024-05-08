package okp.nic.network;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

@Slf4j
@Getter
public class PeerClient extends WebSocketClient {

    private final Gson gson = new Gson();
    private final String remotePeerAddress;
    private final Messenger messenger;

    public PeerClient(URI serverURI, Messenger messenger) {
        super(serverURI);
        remotePeerAddress = "ws://" + serverURI.getHost() + ":" + serverURI.getPort();
        this.messenger = messenger;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.info("Установлено соединение с " + remotePeerAddress);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Соединение с " + remotePeerAddress + " прервано с кодом " + code + ", причина: " + reason);
    }

    @Override
    public void onMessage(String message) {
        log.info("От " + remotePeerAddress + " получено сообщение: " + message);
        if (message.startsWith("COMPRESSED_TEXT:")) {
            messenger.handleRemoteTextInsert(remotePeerAddress, message.substring("COMPRESSED_TEXT:".length()));
        } else {
            Operation op = gson.fromJson(message, Operation.class);
            switch (op.getType()) {
                case "insert":
                    messenger.handleRemoteInsert(remotePeerAddress, op.getPosition(), op.getData());
                    break;
                case "delete":
                    messenger.handleRemoteDelete(op.getPosition());
                    break;
//                case "clear":
//                    messenger.handleRemoteClear();
//                    break;
            }
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("При соединении с " + remotePeerAddress + " возникла ошибка: " + ex);
    }

}