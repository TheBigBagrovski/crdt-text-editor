package network;

import okp.nic.logger.Logger;
import okp.nic.network.messenger.Messenger;
import okp.nic.network.peer.PeerClient;
import okp.nic.network.peer.PeerMessageType;
import okp.nic.network.peer.PeerServer;
import okp.nic.network.signal.SignalClient;
import okp.nic.presenter.PresenterImpl;
import okp.nic.view.editor.log.LogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.net.URISyntaxException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static okp.nic.utils.TextCompressor.compress;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MessengerTest {

    @Mock
    private PresenterImpl presenter;
    @Mock
    private PeerServer peerServer;
    @Mock
    private SignalClient signalClient;
    @Mock
    private PeerClient peerClient;

    private Messenger messenger;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        messenger = new Messenger("localhost", 8080, "signalhost", "8888", "password", "testUser");
        messenger.setPresenter(presenter);
        messenger.setPeerServer(peerServer);
        messenger.setSignalClient(signalClient);
        Logger.setLogService(mock(LogService.class));
    }

    @Test
    public void testHandlePasswordRequest() {
        assertDoesNotThrow(() -> messenger.handlePasswordRequest());
        verify(signalClient).send(anyString());
    }

    @Test
    public void testHandleRemoteCurrentStateRequest() throws Exception {
        String peerAddress = "ws://peerhost:9999";
        String currentDocument = "test document";
        Map<String, PeerClient> connectedPeers = new HashMap<>();
        connectedPeers.put(peerAddress, peerClient);
        messenger.setConnectedPeers(connectedPeers);
        when(presenter.getCurrentDocument()).thenReturn(currentDocument);

        messenger.handleRemoteCurrentStateRequest(peerAddress);

        verify(peerClient).send(PeerMessageType.UPDATE_TEXT.formatTextUpdateMessage(
                messenger.getHostFullAddress(), Base64.getEncoder().encodeToString(compress(currentDocument))));
    }

    @Test
    public void testHandleRemoteTextUpdate() {
        String from = "ws://peerhost:9999";
        String text = "test document";

        byte[] compressedText = compress(text);

        String encodedCompressedText = Base64.getEncoder().encodeToString(compressedText);

        assertDoesNotThrow(() -> messenger.handleRemoteTextUpdate(from, encodedCompressedText));
        verify(presenter).handleRemoteTextUpdate(from, text);
    }

    @Test
    public void testHandleRemotePeerConnected() throws InterruptedException {
        String peerAddress = "ws://peerhost:9999";
        String peerName = "peerUser";
        when(peerClient.connectBlocking()).thenReturn(true);
        assertDoesNotThrow(() -> messenger.handleRemotePeerConnected(peerAddress, peerName));
    }

    @Test
    public void testHandleRemotePeerDisconnected() {
        String disconnectedPeer = "ws://disconnected:1111";
        String disconnectedPeerName = "disconnectedUser";

        Map<String, String> peerNames = new HashMap<>();
        peerNames.put(disconnectedPeer, disconnectedPeerName);
        messenger.getPeerNames().putAll(peerNames);

        assertDoesNotThrow(() -> messenger.handleRemotePeerDisconnected(disconnectedPeer));

        verify(presenter).removePeerName(disconnectedPeer, disconnectedPeerName);
    }

    @Test
    public void testBroadcastInsert() {
        assertDoesNotThrow(() -> messenger.broadcastInsert('a', 5));
        verify(peerServer).broadcast(anyString());
    }

    @Test
    public void testBroadcastDelete() {
        assertDoesNotThrow(() -> messenger.broadcastDelete(5));
        verify(peerServer).broadcast(anyString());
    }

    @Test
    public void testBroadcastDeleteRange() {
        assertDoesNotThrow(() -> messenger.broadcastDeleteRange(5, 10));
        verify(peerServer).broadcast(anyString());
    }

    @Test
    public void testBroadcastTextUpdate() {
        assertDoesNotThrow(() -> messenger.broadcastTextUpdate("test text"));
        verify(peerServer).broadcast(anyString());
    }

    @Test
    public void testBroadcastInsertBlock() {
        assertDoesNotThrow(() -> messenger.broadcastInsertBlock(5, "test block"));
        verify(peerServer).broadcast(anyString());
    }

    @Test
    public void testBroadcastChatMessage() {
        assertDoesNotThrow(() -> messenger.broadcastChatMessage("test message"));
        verify(peerServer).broadcast(anyString());
    }

    @Test
    public void testHandleRemoteInsert() {
        String from = "ws://peer:9999";
        assertDoesNotThrow(() -> messenger.handleRemoteInsert(from, 5, 'a'));
        verify(presenter).handleRemoteInsert(from, 'a', 5);
    }

    @Test
    public void testHandleRemoteDelete() {
        assertDoesNotThrow(() -> messenger.handleRemoteDelete(5));
        verify(presenter).handleRemoteDelete(5);
    }

    @Test
    public void testHandleRemoteInsertBlock() throws Exception {
        String from = "ws://peerhost:9999";
        int pos = 5;
        String text = "test text";

        // Сжимаем текст, получаем массив байтов
        byte[] compressedText = compress(text);

        // Кодируем сжатый текст в base64
        String encodedCompressedText = Base64.getEncoder().encodeToString(compressedText);

        assertDoesNotThrow(() -> messenger.handleRemoteInsertBlock(from, pos, encodedCompressedText));
        verify(presenter).handleRemoteInsertBlock(from, pos, text);
    }

    @Test
    public void testHandleRemoteDeleteRange() {
        assertDoesNotThrow(() -> messenger.handleRemoteDeleteRange(5, 10));
        verify(presenter).handleRemoteDeleteRange(5, 10);
    }

    @Test
    public void testHandleRemoteChatMessage() {
        String from = "ws://peer:9999";
        String message = "test message";

        Map<String, String> peerNames = new HashMap<>();
        peerNames.put(from, "peerUser");
        messenger.getPeerNames().putAll(peerNames);

        assertDoesNotThrow(() -> messenger.handleRemoteChatMessage(from, message));
        verify(presenter).handleRemoteChatMessage("peerUser", message);
    }
}