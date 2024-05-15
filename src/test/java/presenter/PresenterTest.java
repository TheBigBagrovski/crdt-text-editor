package presenter;

import okp.nic.model.Document;
import okp.nic.network.messenger.Messenger;
import okp.nic.presenter.PresenterImpl;
import okp.nic.utils.Utils;
import okp.nic.view.editor.chat.ChatService;
import okp.nic.view.editor.peers.PeerService;
import okp.nic.view.editor.texteditor.TextEditor;
import okp.nic.view.editor.texteditor.TextEditorView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.swing.*;
import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PresenterTest {

    private PresenterImpl presenter;
    @Mock
    private Messenger messenger;

    @Mock
    private Document document;

    @Mock
    private TextEditor textEditor;

    @BeforeEach
    void setUp() {
        presenter = new PresenterImpl("localhost", 8080);
        messenger = Mockito.mock(Messenger.class);
        document = Mockito.mock(Document.class);
        textEditor = Mockito.mock(TextEditorView.class);
        PeerService peerService = Mockito.mock(PeerService.class);
        when(textEditor.getPeerService()).thenReturn(peerService);
        presenter.setMessenger(messenger);
        presenter.setDocument(document);
        presenter.setTextEditor(textEditor);
    }

    @Test
    void testClear() {
        presenter.clear();

        verify(document).clearDocument();
        verify(textEditor).clear();
    }

    @Test
    void testOnLocalInsert() throws Exception {
        char value = 'a';
        int pos = 5;

        presenter.onLocalInsert(value, pos);

        verify(document).insertChar(presenter.getSiteId(), pos, value);
        verify(messenger).broadcastInsert(value, pos);
    }

    @Test
    void testOnLocalDelete() throws Exception {
        int pos = 5;

        presenter.onLocalDelete(pos);

        verify(document).deleteChar(pos);
        verify(messenger).broadcastDelete(pos);
    }

    @Test
    void testOnLocalInsertBlock() {
        int pos = 5;
        String text = "Test text";

        presenter.onLocalInsertBlock(pos, text);

        verify(document).insertTextBlock(presenter.getSiteId(), pos, text);
        verify(messenger).broadcastInsertBlock(pos, text);
    }

    @Test
    void testOnLocalDeleteRange() {
        int startPos = 5;
        int endPos = 10;

        presenter.onLocalDeleteRange(startPos, endPos);

        verify(document).deleteRange(startPos, endPos);
        verify(messenger).broadcastDeleteRange(startPos, endPos);
    }

    @Test
    void testSendChatMessage() {
        String message = "Test message";

        when(textEditor.getChatService()).thenReturn(Mockito.mock(ChatService.class));

        presenter.sendChatMessage(message);

        verify(textEditor.getChatService()).showChatMessage(Utils.getUtfString("Вы"), message);
        verify(messenger).broadcastChatMessage(message);
    }

    @Test
    void testGetCurrentDocument() {
        when(document.getContent()).thenReturn("Test document");

        String result = presenter.getCurrentDocument();

        assertEquals("Test document", result);
    }

    @Test
    void testHandleRemoteInsert() {
        String from = "remoteUser";
        char value = 'a';
        int pos = 5;

        presenter.handleRemoteInsert(from, value, pos);

        verify(document).insertChar(from, pos, value);
        verify(textEditor).insertChar(value, pos);
    }

    @Test
    void testHandleRemoteDelete() {
        int pos = 5;

        presenter.handleRemoteDelete(pos);

        verify(document).deleteChar(pos);
        verify(textEditor).deleteChar(pos);
    }

    @Test
    void testHandleRemoteTextUpdate() {
        String from = "remoteUser";
        String text = "Test text";

        presenter.handleRemoteTextUpdate(from, text);

        verify(document).insertTextBlock(from, 0, text);
        verify(textEditor).insertTextBlock(text, 0);
        verify(textEditor).setCaretPosition(0);
        verify(textEditor).unpause();
    }

    @Test
    void testHandleRemoteInsertBlock() {
        String from = "remoteUser";
        int pos = 5;
        String text = "Test text";

        when(textEditor.getCaretPosition()).thenReturn(10);

        presenter.handleRemoteInsertBlock(from, pos, text);

        verify(document).insertTextBlock(from, pos, text);
        verify(textEditor).insertTextBlock(text, pos);
    }

    @Test
    void testHandleRemoteDeleteRange() {
        int startPos = 5;
        int endPos = 10;

        presenter.handleRemoteDeleteRange(startPos, endPos);

        verify(document).deleteRange(startPos, endPos);
        verify(textEditor).deleteRange(startPos, endPos);
        verify(textEditor).setCaretPosition(startPos);
    }

    @Test
    void testAddPeerName() {
        String peerAddress = "ws://remotehost:8080";
        String name = "RemoteUser";

        presenter.addPeerName(peerAddress, name);

        verify(textEditor.getPeerService()).addPeerName(name + " [remotehost:8080]");
    }

    @Test
    void testRemovePeerName() {
        String peerAddress = "ws://remotehost:8080";
        String name = "RemoteUser";

        presenter.removePeerName(peerAddress, name);

        verify(textEditor.getPeerService()).removePeerName(name + " [remotehost:8080]");
    }

    @Test
    void testHandleRemoteChatMessage() {
        String from = "RemoteUser";
        String message = "Test message";

        when(textEditor.getChatService()).thenReturn(Mockito.mock(ChatService.class));

        presenter.handleRemoteChatMessage(from, message);

        verify(textEditor.getChatService()).showChatMessage(from, message);
    }

}