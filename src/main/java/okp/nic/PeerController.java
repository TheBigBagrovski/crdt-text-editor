//package okp.nic;
//
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.net.InetSocketAddress;
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.List;
//
//@AllArgsConstructor
//@Getter
//@Setter
//public class PeerController {
//    private List<String> peerList;
//    private List<String> connectedPeerList;
//
//    private ServerPeer serverPeer;
//    private String serverAddress;
//
//    private Messenger messenger;
//
//    public void startServerPeer(String host, int port) {
//        this.setServerPeer(new ServerPeer(new InetSocketAddress(host, port)));
//        this.serverPeer.start();
//    }
//
//    public void startClientPeers() {
//        while (this.connectedPeerList.size() < 2) {
//            for (String peer : this.peerList) {
//                if (!this.connectedPeerList.contains(peer)) {
//                    try {
//                        ClientPeer peerNode = new ClientPeer(new URI(peer), this.messenger);
//                        boolean isSucceeded = peerNode.connectBlocking();
//                        if (isSucceeded) {
//                            this.connectedPeerList.add(peer);
//                        } else {
//                            System.out.println("Failed connecting to " + peer);
//                        }
//                    }catch (Exception ex) {
//                        System.out.println("error tapi gpp");
//                    }
//                }
//            }
//            if (this.connectedPeerList.size() < 2) {
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    System.out.println("error pas sleep bre");
//                }
//            }
//        }
//    }
//
//}
