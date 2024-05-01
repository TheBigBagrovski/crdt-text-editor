package okp.nic.network;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SignalServer extends WebSocketServer {

    private final Map<WebSocket, String> clients = new HashMap<>();

    public SignalServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String peerAddress = handshake.getResourceDescriptor().split("\\?")[1].split("=")[1];

        System.out.println("New connection: " + peerAddress);
        clients.put(conn, peerAddress);

        // Send welcome message to the new client
        conn.send("SIGNAL:WELCOME");
        conn.send(sendClientList(getConnectedClients()));
        // Broadcast new connection to other clients
        broadcastMessage("SIGNAL:CONNECTED:" + peerAddress, conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        WebSocket clientToRemove = getClient(conn);
        if (clientToRemove != null) {
            broadcastMessage("SIGNAL:DISCONNECTED:" + clientToRemove.getRemoteSocketAddress().toString(), clientToRemove);
            clients.remove(clientToRemove);
            // Отправляем остальным клиентам информацию об отключении
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message from " + conn.getRemoteSocketAddress() + ": " + message);
        // Здесь можно обработать какие-то команды, если это необходимо
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("An error occurred on connection " + conn + ": " + ex);
    }

    @Override
    public void onStart() {
        System.out.println("Signal server started on port: " + getPort());
    }

    private Set<String> getConnectedClients(/*WebSocket excluded*/) {
        Set<String> connectedClients = new HashSet<>();
        for (WebSocket client : clients.keySet()) {
//            if (!client.equals(excluded)) {
            connectedClients.add(clients.get(client));
//            }
        }
        return connectedClients;
    }

    private WebSocket getClient(WebSocket conn) {
        for (WebSocket client : clients.keySet()) {
            if (client.equals(conn)) {
                return client;
            }
        }
        return null;
    }

    private void broadcastMessage(String message, WebSocket exclude) {
        for (WebSocket client : clients.keySet()) {
//            String peerAddress = clients.get(client); // Retrieve stored user-defined address
            // Create a new ClientPeer instance to send the message
//                ClientPeer tempPeer = new ClientPeer(new URI(peerAddress), null);
//                client.connectBlocking();
            if (!client.equals(exclude)) {
                client.send(message);
            }
//                tempPeer.close();
        }
    }

//    private void broadcastMessage(String message) {
//        for (WebSocket client : clients.keySet()) {
//            client.send(message);
//        }
//    }

    public String sendClientList(Set<String> clientList) {
        StringBuilder message = new StringBuilder("SIGNAL:INITIAL:");
        for (String client : clientList) {
            message.append(client).append(", ");
        }
        return message.toString();
    }

    public static void main(String[] args) {
        int port = 5556; // Порт для WebSocket сервера
        SignalServer server = new SignalServer(new InetSocketAddress(port));
        server.start();
    }
}