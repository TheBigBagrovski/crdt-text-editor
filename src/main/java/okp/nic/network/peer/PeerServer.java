package okp.nic.network.peer;

import okp.nic.logger.Logger;
import okp.nic.network.Messenger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PeerServer extends WebSocketServer {

    private final Messenger messenger;
    private final Logger logger;

    public PeerServer(InetSocketAddress address, Messenger messenger, Logger logger) {
        super(address);
        this.messenger = messenger;
        this.logger = logger;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
//        logger.info("К пир-серверу подключается " + "ws:/" + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
//        logger.info("Закрыто подключение " + conn.getRemoteSocketAddress() + " к пир-серверу с кодом " + code + ", причина: " + getUtfString(reason));
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String prefix = PeerMessageType.UPDATE_TEXT.getPrefix();
        if (message.startsWith(prefix)) {
            String content = message.substring(prefix.length());
            String from = "";
            String text = "";
            Pattern pattern = Pattern.compile("^:<(.*?)>:(.*)$");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                from = matcher.group(1);
                text = matcher.group(2);
            }
            messenger.handleUpdateText(from, text);
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
//        logger.error("Ошибка при подключении " + conn.getRemoteSocketAddress() + " к пир-серверу:" + ex);
    }

    @Override
    public void onStart() {
        logger.info("Пир-сервер успешно запущен на сокете " + "ws:/" + getAddress());
    }

}