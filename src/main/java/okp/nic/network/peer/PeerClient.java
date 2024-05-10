package okp.nic.network.peer;

import com.google.gson.Gson;
import lombok.Getter;
import okp.nic.logger.Logger;
import okp.nic.network.Messenger;
import okp.nic.network.operation.DeleteOperation;
import okp.nic.network.operation.InsertOperation;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class PeerClient extends WebSocketClient {

    private final Gson gson = new Gson();
    private final String remotePeerAddress;
    private final Messenger messenger;
    private final Logger logger;

    public PeerClient(URI serverURI, Messenger messenger, Logger logger) {
        super(serverURI);
        remotePeerAddress = "ws://" + serverURI.getHost() + ":" + serverURI.getPort();
        this.messenger = messenger;
        this.logger = logger;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        logger.info("Установлено соединение с " + remotePeerAddress);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Соединение с " + remotePeerAddress + " прервано с кодом " + code);
    }

    @Override
    public void onMessage(String message) {
        for (PeerMessageType type : PeerMessageType.values()) {
            if (message.startsWith(type.getPrefix())) {
                String content = message.substring(type.getPrefix().length());
                switch (type) {
                    case TEXT_BLOCK:
                        int pos = 0;
                        String text = "";
                        Pattern pattern = Pattern.compile("^(\\d+):(.*)$");
                        Matcher matcher = pattern.matcher(content);
                        if (matcher.find()) {
                            pos = Integer.parseInt(matcher.group(1));
                            text = matcher.group(2);
                        }
                        messenger.handleRemoteBlockInsert(remotePeerAddress, pos, text);
                        break;
                    case OPERATION:
                        if (content.startsWith("INSERT:")) {
                            InsertOperation insertOp = gson.fromJson(content.substring("INSERT:".length()), InsertOperation.class);
                            messenger.handleRemoteInsert(remotePeerAddress, insertOp.getPosition(), insertOp.getData());
                        } else if (content.startsWith("DELETE:")) {
                            DeleteOperation deleteOp = gson.fromJson(content.substring("DELETE:".length()), DeleteOperation.class);
                            messenger.handleRemoteDelete(deleteOp.getPosition());
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void onError(Exception ex) {
        logger.error("При соединении с " + remotePeerAddress + " возникла ошибка: " + ex);
    }

}