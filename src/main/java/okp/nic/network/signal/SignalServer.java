package okp.nic.network.signal;

import at.favre.lib.crypto.bcrypt.BCrypt;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okp.nic.network.peer.PeerMessageType;
import okp.nic.view.InputDialogs;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static okp.nic.utils.PortChecker.findAvailablePort;
import static okp.nic.utils.PortChecker.isPortAvailable;
import static okp.nic.utils.Utils.getUtfString;

@Slf4j
@Getter
public class SignalServer extends WebSocketServer {

    private final Map<WebSocket, String[]> clients = new HashMap<>();
    private static String passwordHash;

    public SignalServer(InetSocketAddress address) {
        super(address);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        conn.send(SignalMessageType.PASSWORD_REQUEST.getPrefix());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        log.info(getUtfString("Закрывается соединение с сигнальным сервером: " + conn.getRemoteSocketAddress() + " с кодом " + code + ", причина: ") + reason);
        if (clients.containsKey(conn)) {
            clients.remove(conn);
            String peerAddress = conn.getResourceDescriptor().split("\\?")[1].split("&")[0].split("=")[1];
            broadcastMessage(SignalMessageType.PEER_DISCONNECTED.formatMessage(peerAddress));
        } else {
            log.error(getUtfString(conn.getRemoteSocketAddress() + " не является клиентом сигнального сервера"));
        }
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        String passwordPrefix = PeerMessageType.PASSWORD.getPrefix();
        if (message.startsWith(passwordPrefix)) {
            String providedPasswordHash = message.substring(passwordPrefix.length());
            BCrypt.Result result = BCrypt.verifyer().verify(passwordHash.toCharArray(), providedPasswordHash.getBytes());
            if (!result.verified) {
                log.error(getUtfString("Введен неверный пароль при попытке подключиться"));
                conn.close();
                return;
            }
            String peerAddress = conn.getResourceDescriptor().split("\\?")[1].split("&")[0].split("=")[1];
            String peerName = URLDecoder.decode(conn.getResourceDescriptor().split("\\?")[1].split("&")[1].split("=")[1]);
            log.info(getUtfString("Новое подключение к сигнальному серверу: " + peerAddress));
            conn.send(SignalMessageType.WELCOME.formatWelcomeMessage(clients.values())); // отправка новому пиру текущих подключенных клиентов
            broadcastMessage(SignalMessageType.PEER_CONNECTED.formatMessage(peerAddress + "-" + peerName)); // отправка подключенным клиентам данных о новом пире
            if (!clients.isEmpty()) {
                Iterator<WebSocket> iterator = clients.keySet().iterator();
                iterator.next().send(SignalMessageType.INITIAL_TEXT_REQUEST.formatMessage(peerAddress));
            }
            clients.put(conn, new String[]{peerAddress, peerName});
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error(getUtfString("Ошибка при попытке подключиться к сигнальному серверу"));
    }

    @Override
    public void onStart() {
        String socketAddress = getAddress().toString();
        log.info(getUtfString("Сигнальный сервер запущен на сокете: ") + socketAddress);
        InputDialogs.showInfoWindow(socketAddress);
    }

    private void broadcastMessage(String message) {
        for (WebSocket client : clients.keySet()) {
            client.send(message);
        }
    }

    public static void main(String[] args) {
        String[] info;
        String address;
        boolean validAddress = false;
        do {
            info = InputDialogs.getSignalServerAddress();
            if (info == null) {
                log.info(getUtfString("Пользователь отменил ввод"));
                return;
            }
            address = info[0];
            try {
                InetSocketAddress testAddress = new InetSocketAddress(address, 0);
                if (!testAddress.isUnresolved()) {
                    validAddress = true;
                } else {
                    log.error(getUtfString("Введите корректный адрес"));
                }
            } catch (IllegalArgumentException e) {
                log.error(getUtfString("Некорретный формат адреса"));
            }
        } while (!validAddress);
        passwordHash = info[1];
        String port;
        boolean validPort = false;
        do {
            port = InputDialogs.getSignalServerPort();
            if (port == null) {
                log.info(getUtfString("Пользователь отменил ввод"));
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
                    log.error(getUtfString("Порт " + portNumber + " недоступен"));
                }
            } catch (NumberFormatException e) {
                log.error(getUtfString("Некорректный номер порта"));
            }
        } while (!validPort);

        InetSocketAddress isa = new InetSocketAddress(address, Integer.parseInt(port));
        SignalServer server = new SignalServer(isa);
        server.start();
    }

}
