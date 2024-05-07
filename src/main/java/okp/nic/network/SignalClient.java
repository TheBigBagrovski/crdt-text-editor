package okp.nic.network;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

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
        if (message.startsWith("SIGNAL:CONNECTED:")) {
            String peerAddress = message.substring("SIGNAL:CONNECTED:".length());
            messenger.handleRemotePeerConnected(peerAddress);
        } else if (message.startsWith("SIGNAL:DISCONNECTED:")) {
            String peerAddress = message.substring("SIGNAL:DISCONNECTED:".length());
            messenger.handleRemotePeerDisconnected(peerAddress);
        } else if (message.startsWith("SIGNAL:PEERS:")) {
            String peersList = message.substring("SIGNAL:PEERS:".length());
            if (!peersList.isEmpty() && !peersList.equals("FIRST")) {
                String[] peers = peersList.split(", ");
                for (String peer : peers) {
                    messenger.handleRemotePeerConnected(peer);
                }
            }
        }
//        else if (message.startsWith("INITIAL_TEXT_REQ_TO:")) {
//            String peer = message.substring("INITIAL_TEXT_REQ_TO:".length());
//            try {
//                PeerClient ws = new PeerClient(new URI(peer));
//                ws.connectBlocking();
//                messenger.sendCurrentState(ws);
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }
}