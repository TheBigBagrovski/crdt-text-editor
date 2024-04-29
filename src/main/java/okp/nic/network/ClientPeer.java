package okp.nic.network;

import com.google.gson.Gson;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

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
        Operation op = gson.fromJson(message, Operation.class);
        if (op.getType().equals("insert")) {
            System.out.println("onMessage --> INSERT");
            messenger.handleRemoteInsert(op.getData());
        } else if (op.getType().equals("delete")) {
            System.out.println("onMessage --> DELETE");
            messenger.handleRemoteDelete(op.getData());
        }
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

}