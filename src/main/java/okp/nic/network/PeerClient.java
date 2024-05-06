package okp.nic.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import okp.nic.crdt.Char;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class PeerClient extends WebSocketClient {

    private final Gson gson = new Gson();
    private final Messenger messenger;

    public PeerClient(URI serverURI, Messenger messenger) {
        super(serverURI);
        this.messenger = messenger;
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
        if (message.startsWith("SIGNAL:INITIAL_STATE:")) {
            String payload = message.substring("SIGNAL:INITIAL_STATE:".length());
//            List<Char> charList = gson.fromJson(payload, new TypeToken<List<Char>>() {
//            }.getType());
            char[] arr = payload.toCharArray();
//            int index = 0;
//            messenger.getController().loadTextInEditor(arr);
            int index = 0;
            for (char c : arr) {
                messenger.handleRemoteInsert(messenger.getController().getCrdt().generateChar(c, index++));
            }
//            for (char c : arr) {
//                messenger.getController().insertToTextEditor(c, index++);
//            }
        } else {
            Operation op = gson.fromJson(message, Operation.class);
            if (op.getType().equals("insert")) {
                log.info("onMessage --> INSERT");
                messenger.handleRemoteInsert(op.getData());
            } else if (op.getType().equals("delete")) {
                log.info("onMessage --> DELETE");
                messenger.handleRemoteDelete(op.getData());
            }
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("Возникла ошибка в пир-клиенте: " + ex);
    }

}