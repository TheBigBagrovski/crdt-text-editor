package network;

import at.favre.lib.crypto.bcrypt.BCrypt;
import okp.nic.network.peer.PeerMessageType;
import okp.nic.network.signal.SignalMessageType;
import okp.nic.network.signal.SignalServer;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignalServerTest {

    @Mock
    private WebSocket webSocket;

    @Mock
    private ClientHandshake handshake;

    private SignalServer signalServer;
    private Map<WebSocket, String[]> clients;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        signalServer = new SignalServer(new InetSocketAddress("localhost", 8080));
        clients = new HashMap<>();
        signalServer.setClients(clients);
        SignalServer.passwordHash = BCrypt.withDefaults().hashToString(12, "testpassword".toCharArray());
    }

    @Test
    void onOpen() {
        signalServer.onOpen(webSocket, handshake);
        verify(webSocket).send(SignalMessageType.PASSWORD_REQUEST.getPrefix());
    }

//    @Test
//    void onClose_ClientExists() {
//        when(webSocket.getResourceDescriptor()).thenReturn("/?address=192.168.1.2&name=TestUser");
//        clients.put(webSocket, new String[]{"192.168.1.2", "TestUser"});
//        signalServer.onClose(webSocket, 1000, "Normal closure", true);
//
//        assertFalse(clients.containsKey(webSocket));
//        verify(webSocket).send(SignalMessageType.PEER_DISCONNECTED.formatMessage("192.168.1.2"));
//    }

    @Test
    void onClose_ClientNotExists() {
        when(webSocket.getRemoteSocketAddress()).thenReturn(new InetSocketAddress("localhost", 1234));
        signalServer.onClose(webSocket, 1000, "Normal Closure", true);

        assertFalse(clients.containsKey(webSocket));
    }

//    @Test
//    void onMessage_ValidPassword() {
//        SignalServer.passwordHash = "SALT" + "testpassword"; // Задаем пароль для теста
//
//        when(webSocket.getResourceDescriptor()).thenReturn("/?address=192.168.1.2&name=TestUser2");
//        String message = PeerMessageType.PASSWORD.getPrefix() + BCrypt.withDefaults().hashToString(12, "testpassword".toCharArray());
//        signalServer.onMessage(webSocket, message);
//
////        assertTrue(clients.containsKey(webSocket));
//        verify(webSocket).send("SIGNAL:WELCOME.PEERS:NONE");
//        verify(webSocket).send(SignalMessageType.PEER_CONNECTED.formatMessage("192.168.1.2-TestUser2"));
//    }

    @Test
    void onMessage_InvalidPassword() {
        SignalServer.passwordHash = "testpassword"; // Установить пароль для теста
        when(webSocket.getResourceDescriptor()).thenReturn("/?address=192.168.1.2&name=TestUser2");
        String message = PeerMessageType.PASSWORD.getPrefix() + BCrypt.withDefaults().hashToString(12, "wrongpassword".toCharArray());
        signalServer.onMessage(webSocket, message);

        assertFalse(clients.containsKey(webSocket));
        verify(webSocket).close();
    }

    @Test
    void broadcastMessage() {
        WebSocket webSocket2 = mock(WebSocket.class);
        clients.put(webSocket, new String[]{"192.168.1.2", "TestUser"});
        clients.put(webSocket2, new String[]{"192.168.1.3", "TestUser2"});
        signalServer.broadcastMessage("Test message");
        verify(webSocket).send("Test message");
        verify(webSocket2).send("Test message");
    }

}
