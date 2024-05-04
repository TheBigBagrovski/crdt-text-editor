package okp.nic.network;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okp.nic.InputDialogs;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import javax.swing.*;
import java.awt.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static okp.nic.Utils.findAvailablePort;
import static okp.nic.Utils.isPortAvailable;

@Slf4j
@Getter
public class SignalServer extends WebSocketServer {

//    private boolean isRunning = false;
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
        log.error("Ошибка при попытке подключиться к сигнальному серверу по адресу " + conn.getRemoteSocketAddress());
    }

    @Override
    public void onStart() {
        String socketAddress = getAddress().toString();
        log.info("Сигнальный сервер запущен на сокете: " + socketAddress);
        new SocketInfoWindow(socketAddress);
    }

    private void broadcastMessage(String message, WebSocket exclude) {
        for (WebSocket client : clients.keySet()) {
            if (!client.equals(exclude)) {
                client.send(message);
            }
        }
    }

    public static void main(String[] args) {
        String address;
        boolean validAddress = false;
        do {
            address = InputDialogs.getSignalServerAddress();
            if (address == null) {
                log.info("Пользователь отменил ввод");
                return;
            }
            try {
                InetSocketAddress testAddress = new InetSocketAddress(address, 0);
                if (!testAddress.isUnresolved()) {
                    validAddress = true;
                } else {
                    log.error("Введите корректный адрес");
                }
            } catch (IllegalArgumentException e) {
                log.error("Некорретный формат адреса");
            }
        } while (!validAddress);

        String port;
        boolean validPort = false;
        do {
            port = InputDialogs.getSignalServerPort();
            if (port == null) {
                log.info("Пользователь отменил ввод");
                return;
            } else if (port.isEmpty()) {
                port = String.valueOf(findAvailablePort());
                break;
            }
            try {
                int portNumber = Integer.parseInt(port);
                if (isPortAvailable(portNumber)) {
                    validPort = true;
                } else {
                    log.error("Порт " + portNumber + " недоступен");
                }
            } catch (NumberFormatException e) {
                log.error("Некорректный номер порта");
            }
        } while (!validPort);

        InetSocketAddress isa = new InetSocketAddress(address, Integer.parseInt(port));
        SignalServer server = new SignalServer(isa);
        server.start();
    }

    static class SocketInfoWindow extends JFrame {

        public SocketInfoWindow(String socketAddress) {
            super(new String("Адрес сигнального сервера".getBytes(), StandardCharsets.UTF_8));
            JLabel socketLabel = new JLabel(new String("Сигнальный сервер запущен на сокете: ".getBytes(), StandardCharsets.UTF_8) + socketAddress);
            socketLabel.setFont(new Font("Arial", Font.BOLD, 18));
            socketLabel.setHorizontalAlignment(SwingConstants.CENTER);
            JPanel panel = new JPanel();
            panel.add(socketLabel);
            panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
            add(panel);
            setSize(600, 100);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setVisible(true);
        }
    }

}