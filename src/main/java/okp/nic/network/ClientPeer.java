package okp.nic.network;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientPeer extends WebSocketClient {

    private final Gson gson = new Gson();
    private final Messenger messenger;

    public ClientPeer(URI serverURI, Messenger messenger) {
        super(serverURI);
        this.messenger = messenger;
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        System.out.println("New PEER connection opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("CLIENT received message: " + message);
        // Проверка, является ли сообщение от сигнального сервера
        if (isSignalServerMessage(message)) {
            // Обработка сообщения от сигнального сервера
            handleSignalServerMessage(message);
        } else {
            // Обработка сообщения от других пиров
            Operation op = gson.fromJson(message, Operation.class);
            if (op.getType().equals("insert")) {
                System.out.println("onMessage --> INSERT");
                messenger.handleRemoteInsert(op.getData());
            } else if (op.getType().equals("delete")) {
                System.out.println("onMessage --> DELETE");
                messenger.handleRemoteDelete(op.getData());
            }
        }
    }

    private boolean isSignalServerMessage(String message) {
//         Здесь можно реализовать логику для определения, является ли сообщение от сигнального сервера
//         Например, можно определить по формату сообщения или ключевым словам в сообщении
//         В данном примере просто предполагается, что все сообщения от сигнального сервера начинаются с "SIGNAL:"
        return message.startsWith("SIGNAL:");
    }

    private void handleSignalServerMessage(String message) {
        // Обработка сообщения от сигнального сервера о подключении или отключении пира
        // Здесь реализуется логика для извлечения информации из сообщения и вызов соответствующего метода в Messenger
        System.out.println("На клиент " + messenger.getHost() + ":" + messenger.getPort() + " пришло сообщение: " + message);
        if (message.startsWith("SIGNAL:CONNECTED:")) {
            String peerAddress = message.substring("SIGNAL:CONNECTED:".length());
            ExecutorService es = Executors.newSingleThreadExecutor();
            es.execute(() -> messenger.handleRemotePeerConnected(peerAddress));
        } else if (message.startsWith("SIGNAL:DISCONNECTED:")) {
            String peerAddress = message.substring("SIGNAL:DISCONNECTED:".length());
            messenger.handleRemotePeerDisconnected(peerAddress);
        } else if (message.startsWith("SIGNAL:INITIAL:")) {
            String[] peers = message.substring("SIGNAL:INITIAL:".length()).split(", ");
            for (String peer : peers) {
                ExecutorService es = Executors.newSingleThreadExecutor();
                es.execute(() -> messenger.handleRemotePeerConnected(peer));
            }
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

}