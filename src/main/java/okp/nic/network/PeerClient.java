package okp.nic.network;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
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
//        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(String message) {
//        System.out.println("CLIENT received message: " + message);
        log.info("Пир-клиент получил сообщение: " + message);
        Operation op = gson.fromJson(message, Operation.class);
        if (op.getType().equals("insert")) {
            log.info("onMessage --> INSERT");
            messenger.handleRemoteInsert(op.getData());
        } else if (op.getType().equals("delete")) {
            log.info("onMessage --> DELETE");
            messenger.handleRemoteDelete(op.getData());
        }
    }

    @Override
    public void onError(Exception ex) {
        log.error("Возникла ошибка в пир-клиенте: " + ex);
    }

}