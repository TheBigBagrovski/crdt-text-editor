package okp.nic.network;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public class SignalServer extends WebSocketServer {

    private final Set<WebSocket> clients = new HashSet<>();

    public SignalServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String peerAddress = conn.getRemoteSocketAddress().toString();
        System.out.println("New connection: " + peerAddress);
        clients.add(conn);

        // Send welcome message to the new client
        conn.send("SIGNAL:WELCOME");

        // Broadcast new connection to other clients
        broadcastMessage("SIGNAL:CONNECTED:" + peerAddress);
    }

//    @Override
//    public void onOpen(WebSocket conn, ClientHandshake handshake) {
//        String peerAddress = handshake.getResourceDescriptor().split("\\?")[1];
//        System.out.println("New connection: " + peerAddress);
//        clients.add(conn);
//        // Отправляем новому клиенту список остальных клиентов
//        conn.send(sendClientList(getConnectedClients()));
//        // Отправляем остальным клиентам информацию о новом подключении
//        broadcastMessage("SIGNAL:CONNECTED:" + conn.getLocalSocketAddress().toString());
//    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        System.out.println("Closed connection: " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
        WebSocket clientToRemove = getClient(conn);
        if (clientToRemove != null) {
            clients.remove(clientToRemove);
            // Отправляем остальным клиентам информацию об отключении
            broadcastMessage("SIGNAL:DISCONNECTED:" + clientToRemove.getRemoteSocketAddress().toString());
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        System.out.println("Received message from " + conn.getRemoteSocketAddress() + ": " + message);
        // Здесь можно обработать какие-то команды, если это необходимо
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        System.err.println("An error occurred on connection " + conn.getRemoteSocketAddress() + ": " + ex);
    }

    @Override
    public void onStart() {
        System.out.println("Signal server started on port: " + getPort());
    }

    private Set<String> getConnectedClients() {
        Set<String> connectedClients = new HashSet<>();
        for (WebSocket client : clients) {
            connectedClients.add(client.getRemoteSocketAddress().toString());
        }
        return connectedClients;
    }

    private WebSocket getClient(WebSocket conn) {
        for (WebSocket client : clients) {
            if (client.equals(conn)) {
                return client;
            }
        }
        return null;
    }

    private void broadcastMessage(String message) {
        for (WebSocket client : clients) {
            client.send(message);
        }
    }

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