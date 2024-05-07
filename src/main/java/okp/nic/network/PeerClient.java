package okp.nic.network;

import com.google.gson.Gson;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

@Slf4j
@Setter
public class PeerClient extends WebSocketClient {

    private final Gson gson = new Gson();
    private Messenger messenger;

    public PeerClient(URI serverURI) {
        super(serverURI);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.info("Новое соединение с пир-клиентом");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("Соединение с пир-клиентом прервано с кодом " + code + ", причина: " + reason);
    }

    @Override
    public void onMessage(String message) {
        log.info("Пир-клиент получил сообщение: " + message);
        if (message.startsWith("INITIAL_STATE:")) {
            String payload = message.substring("INITIAL_STATE:".length());
//            List<Char> charList = gson.fromJson(payload, new TypeToken<List<Char>>() {
//            }.getType());
//            for (Char c : charList) {
//                messenger.handleRemoteInsert(c);
            messenger.getController().clear();
            int i = 0;
            for (char c : payload.toCharArray()) {
                messenger.handleRemoteInsert(i++, c);
            }
        } else {
            Operation op = gson.fromJson(message, Operation.class);
            if (op.getType().equals("insert")) {
                log.info("onMessage --> INSERT");
                messenger.handleRemoteInsert(op.getPosition(), op.getData());
            } else if (op.getType().equals("delete")) {
                log.info("onMessage --> DELETE");
                messenger.handleRemoteDelete(op.getPosition());
            }
        }

    }

    @Override
    public void onError(Exception ex) {
        log.error("Возникла ошибка в пир-клиенте: " + ex);
    }
}