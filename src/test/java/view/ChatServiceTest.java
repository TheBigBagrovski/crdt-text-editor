package view;

import okp.nic.view.editor.chat.ChatPanel;
import okp.nic.view.editor.chat.ChatServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import javax.swing.text.Document;

import static org.mockito.Mockito.*;

class ChatServiceTest {

    @Mock
    private ChatPanel chatPanel;

    @Mock
    private JTextArea chatArea;

    private ChatServiceImpl chatService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(chatPanel.getChatArea()).thenReturn(chatArea);
        chatService = new ChatServiceImpl(chatPanel);
    }

    @Test
    void showChatMessage_AppendsMessageToChatArea() {
        String author = "Author";
        String message = "Test message";
        when(chatPanel.getChatArea().getDocument()).thenReturn(mock(Document.class));
        when(chatPanel.getChatArea().getDocument().getLength()).thenReturn(12);

        chatService.showChatMessage(author, message);

        verify(chatArea).append("Author: Test message\n");
        verify(chatArea).setCaretPosition(anyInt());
    }

    @Test
    void showChatMessage_SetsLineWrapAndWrapStyleWord() {
        String author = "Author";
        String message = "Test message";
        when(chatPanel.getChatArea().getDocument()).thenReturn(mock(Document.class));
        when(chatPanel.getChatArea().getDocument().getLength()).thenReturn(12);


        chatService.showChatMessage(author, message);

        verify(chatArea).setLineWrap(true);
        verify(chatArea).setWrapStyleWord(true);
    }
}