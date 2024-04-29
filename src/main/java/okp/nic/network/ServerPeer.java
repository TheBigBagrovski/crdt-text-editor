package okp.nic.network;


import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;

public class ServerPeer extends WebSocketServer {

    public ServerPeer(InetSocketAddress address) {
        super(address);
        address.getHostName();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake clientHandshake) {
        System.out.println("New connection to " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message from "	+ conn.getRemoteSocketAddress() + ": " + message);
//
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("An error occurred on connection " + conn.getRemoteSocketAddress()  + ":" + ex);
    }

    @Override
    public void onStart() {
        System.out.println("Node server started successfully");
    }

}

/*
//        Operation op = this.gson.fromJson(message, Operation.class);
//        if (op.getType().equals("insert")) {
//            System.out.println("onMessage --> INSERT");
//            this.messenger.handleRemoteInsert(op.getData());
//        } else if (op.getType().equals("delete")) {
//            System.out.println("onMessage --> DELETE");
//            this.messenger.handleRemoteDelete(op.getData());
//        }
 */