package okp.nic.network;

import com.google.gson.Gson;
import lombok.Getter;
import okp.nic.crdt.Char;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Messenger {
    private final MessengerListener controller;

    private final List<String> connectedPeerList = new ArrayList<>();

    private final String host;
    private final int port;

    private ServerPeer serverPeer;

    private String ssUrl = "ws://192.168.0.49:5556";

    private final Gson gson = new Gson();

    public Messenger(String host, int port, MessengerListener controller) {
        this.host = host;
        this.port = port;
        this.controller = controller;
        init();
    }

    public void init() {
        startServerPeer();
        connectToPeer(ssUrl); // Connect to the signal server
    }

    public void startServerPeer() {
        serverPeer = new ServerPeer(new InetSocketAddress(host, port));
        serverPeer.start();
    }

    public void handleRemotePeerConnected(String newPeer) {
        connectToPeer(newPeer);
    }

//    public void handleRemotePeerConnected(String newPeer) {
//        while (!connectedPeerList.contains(newPeer)) {
//            try {
//                ClientPeer peerNode = new ClientPeer(new URI(newPeer), this);
//                boolean isSucceeded = peerNode.connectBlocking();
//                if (isSucceeded) {
//                    connectedPeerList.add(newPeer);
//                } else {
//                    System.out.println("Failed connecting to " + newPeer);
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        System.out.println("error pas sleep bre");
//                    }
//                }
//            } catch (Exception ex) {
//                System.out.println("error tapi gpp");
//            }
//        }
//
//    }

    public void handleRemotePeerDisconnected(String disconnectedPeer) {
        for (String s : connectedPeerList) {
            if (s.equals(disconnectedPeer)) {
                connectedPeerList.remove(disconnectedPeer);
            }
        }
    }

    public void connectToPeer(String peerAddress) {
        System.out.println("MESSENGER - startClientPeers");
        String myFullAddress = "ws://" + host + ":" + port; // Construct address string
        while (!connectedPeerList.contains(peerAddress)) {
            try {
//                ClientPeer peerNode = new ClientPeer(new URI(peerAddress), this);
                ClientPeer peerNode = new ClientPeer(new URI(peerAddress + "?address=" + myFullAddress), this); // Include address in URL

                boolean isSucceeded = peerNode.connectBlocking();
                if (isSucceeded) {
                    connectedPeerList.add(peerAddress);
                } else {
                    System.out.println("Failed connecting to " + peerAddress);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("error pas sleep bre");
                    }
                }
            } catch (Exception ex) {
                System.out.println("Error connecting to signal server");
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