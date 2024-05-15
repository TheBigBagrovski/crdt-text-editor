package view;

import okp.nic.view.editor.log.LogPanel;
import okp.nic.view.editor.log.LogServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import javax.swing.text.Document;

import static org.mockito.Mockito.*;

class LogServiceTest {

    @Mock
    private LogPanel logPanel;

    @Mock
    private JTextArea logArea;

    private LogServiceImpl logService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(logPanel.getLogArea()).thenReturn(logArea);
        logService = new LogServiceImpl(logPanel);
    }

    @Test
    void showLog_AppendsMessageToLogArea() {
        String message = "Test message";
        when(logPanel.getLogArea().getDocument()).thenReturn(mock(Document.class));
        when(logPanel.getLogArea().getDocument().getLength()).thenReturn(12);

        logService.showLog(message);

        verify(logArea).append("Test message\n");
        verify(logArea).setCaretPosition(anyInt());
    }

    @Test
    void showLog_SetsLineWrapAndWrapStyleWord() {
        String message = "Test message";
        when(logPanel.getLogArea().getDocument()).thenReturn(mock(Document.class));
        when(logPanel.getLogArea().getDocument().getLength()).thenReturn(12);
        logService.showLog(message);

        verify(logArea).setLineWrap(true);
        verify(logArea).setWrapStyleWord(true);
    }
}