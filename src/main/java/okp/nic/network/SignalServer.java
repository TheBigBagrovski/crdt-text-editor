package okp.nic.network;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static okp.nic.Utils.findAvailablePort;

@Slf4j
@Getter
public class SignalServer extends WebSocketServer {

    private boolean isRunning = false;
    private final Map<WebSocket, String> clients = new HashMap<>();

    public SignalServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        String peerAddress = handshake.getResourceDescriptor().split("\\?")[1].split("=")[1];
        log.info("Новое подключение к сигнальному серверу: " + peerAddress);
        conn.send("SIGNAL:WELCOME");
        StringBuilder sb = new StringBuilder("SIGNAL:INITIAL:");
        for (String socket : clients.values()) {
            sb.append(socket).append(", ");
        }
        clients.put(conn, peerAddress);
        conn.send(sb.toString()); // Отправка новому пиру текущих подключенных клиентов
        broadcastMessage("SIGNAL:CONNECTED:" + peerAddress, conn); // Отправка подключенным клиентам данных о новом пире
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info("Закрывается соединение с сигнальным сервером: " + conn.getRemoteSocketAddress() + " с кодом " + code + ", причина: " + reason);
        if (clients.containsKey(conn)) {
            broadcastMessage("SIGNAL:DISCONNECTED:" + conn.getRemoteSocketAddress().toString(), conn);
            clients.remove(conn);
        } else {
            log.error(conn.getRemoteSocketAddress() + " не является клиентом сигнального сервера");
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        log.info("Сигнальный сервер получает сообщение от " + conn.getRemoteSocketAddress() + ": " + message);
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error("Ошибка при попытке подключиться к сигнальному серверу " + conn + ": " + ex);
    }

    @Override
    public void onStart() {
        isRunning = true;
        log.info("Сигнальный сервер запущен на сокете: " + getAddress());
    }

    private void broadcastMessage(String message, WebSocket exclude) {
        for (WebSocket client : clients.keySet()) {
            if (!client.equals(exclude)) {
                client.send(message);
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Введите адрес сигнального сервера: ");
        String host = scanner.nextLine();
        int port = findAvailablePort();
        InetSocketAddress isa = new InetSocketAddress(host, port);
        SignalServer server = new SignalServer(isa);
        server.start();
    }

}