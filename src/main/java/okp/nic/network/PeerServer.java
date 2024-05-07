package okp.nic.network;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

@Slf4j
public class PeerServer extends WebSocketServer {

    private final Messenger messenger;

    public PeerServer(InetSocketAddress address, Messenger messenger) {
        super(address);
        this.messenger = messenger;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        log.info("К пир-серверу подключается " + "ws:/" + conn.getRemoteSocketAddress());

    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("Закрыто подключение " + conn.getRemoteSocketAddress() + " к пир-серверу с кодом " + code + ", причина: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        log.info("Пир-сервер получает сообщение от " + " ws:/" + conn.getRemoteSocketAddress() + ": " + message);
        if (message.startsWith("CURRENT_STATE:")) {
            String from = message.substring("CURRENT_STATE:".length(), message.indexOf(":FROM:"));
            String payload = message.substring(("CURRENT_STATE:" + from).length());
            messenger.getController().clear();
            int i = 0;
            for (char c : payload.toCharArray()) {
                messenger.handleRemoteInsert(from, i++, c);
            }
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error("Ошибка при подключении " + conn.getRemoteSocketAddress() + " к пир-серверу:" + ex);
    }

    @Override
    public void onStart() {
        log.info("Пир-сервер успешно запущен на сокете " + "ws:/" + getAddress());
    }

}