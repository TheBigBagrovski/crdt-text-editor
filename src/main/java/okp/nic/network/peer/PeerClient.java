package okp.nic.network.peer;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okp.nic.network.Messenger;
import okp.nic.network.operation.DeleteOperation;
import okp.nic.network.operation.InsertOperation;
import okp.nic.network.operation.Operation;
import okp.nic.network.signal.SignalMessageType;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

import static okp.nic.network.signal.SignalMessageType.WELCOME;

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
        for (PeerMessageType type : PeerMessageType.values()) {
            if (message.startsWith(type.getPrefix())) {
                String content = message.substring(type.getPrefix().length());
                switch (type) {
                    case COMPRESSED_TEXT:
                        messenger.handleRemoteTextInsert(remotePeerAddress, content);
                        break;
                    case OPERATION:
                        Operation op = gson.fromJson(content, Operation.class);
                        if (op instanceof InsertOperation) {
                            InsertOperation insertOp = (InsertOperation) op;
                            messenger.handleRemoteInsert(remotePeerAddress, insertOp.getPosition(), insertOp.getData());
                        } else if (op instanceof DeleteOperation) {
                            DeleteOperation deleteOp = (DeleteOperation) op;
                            messenger.handleRemoteDelete(deleteOp.getPosition());
                        }
                }
            }
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("При соединении с " + remotePeerAddress + " возникла ошибка: " + ex);
    }

}