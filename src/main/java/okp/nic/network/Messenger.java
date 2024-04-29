package okp.nic.network;

import com.google.gson.Gson;
import okp.nic.crdt.Char;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Messenger {
    private final MessengerListener controller;

    private final List<String> peerList;
    private final List<String> connectedPeerList = new ArrayList<>();

    private final String host;
    private final int port;

    private ServerPeer serverPeer;
    
    private final Gson gson = new Gson();

    public Messenger(String host, int port, MessengerListener controller, List<String> peerList) {
        this.host = host;
        this.port = port;
        this.controller = controller;
        this.peerList = peerList;
        init();
    }

    public void init() {
        startServerPeer();
        startClientPeers();
    }

    public void startServerPeer() {
        serverPeer = new ServerPeer(new InetSocketAddress(host, port));
        serverPeer.start();
    }

    public void startClientPeers() {
        System.out.println("MESSENGER - startClientPeers");
        while (connectedPeerList.size() < (peerList.size())) {
            for (String peer : peerList) {
                if (!connectedPeerList.contains(peer)) {
                    try {
                        ClientPeer peerNode = new ClientPeer(new URI(peer), this);
                        boolean isSucceeded = peerNode.connectBlocking();
                        if (isSucceeded) {
                            connectedPeerList.add(peer);
                        } else {
                            System.out.println("Failed connecting to " + peer);
                        }
                    } catch (Exception ex) {
                        System.out.println("error tapi gpp");
                    }
                }
            }
            if (connectedPeerList.size() < (peerList.size())) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    System.out.println("error pas sleep bre");
                }
            }
        }
    }

    public void broadcastInsert(Char data) {
        Operation op = new Operation(data, "insert");
        String payload = gson.toJson(op);
        serverPeer.broadcast(payload);
    }

    public void broadcastDelete(Char data) {
        System.out.println("[broadcastDelete] START");
        System.out.println("[broadcastDelete] >> preparing delete operation");
        Operation op = new Operation(data, "delete");
        System.out.println("[broadcastDelete] >> jsonify operation");
        String payload = gson.toJson(op);
        System.out.println("[broadcastDelete] call serverPeer->broadcast");
        serverPeer.broadcast(payload);
        System.out.println("[broadcastDelete] FINISH");
    }

    public void handleRemoteInsert(Char data) {
        controller.handleRemoteInsert(data);
    }

    public void handleRemoteDelete(Char data) {
        controller.handleRemoteDelete(data);
    }
}

/*

    public void addPeer(String peer) {
        peerList.add(peer);
        startClientPeers();
    }



 */