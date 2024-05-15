package view;

import okp.nic.presenter.Presenter;
import okp.nic.view.editor.texteditor.TextEditorView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TextEditorTest {

    @Mock
    private Presenter presenter;

    @Mock
    private JTextArea textArea;

    private TextEditorView textEditorView;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        textEditorView = new TextEditorView(presenter);
        textEditorView.getTextArea().setText("Test text");
    }

    @Test
    void caretUpdate() {
        textArea.setText("abcdefgjkk");
        textEditorView.setTextArea(textArea);
        when(textArea.getSelectionStart()).thenReturn(3);  // Мокаем textArea.getSelectionStart()
        when(textArea.getSelectionEnd()).thenReturn(7);  // Мокаем textArea.getSelectionEnd()
        when(textArea.getSelectedText()).thenReturn("t tex");  // Мокаем textArea.getSelectedText()

        CaretEvent caretEvent = mock(CaretEvent.class);
        when(caretEvent.getDot()).thenReturn(3);
        when(caretEvent.getMark()).thenReturn(7);

        textEditorView.caretUpdate(caretEvent);

        assertEquals(3, textEditorView.getSelectStartPos());
        assertEquals(7, textEditorView.getSelectEndPos());
        assertEquals("t tex", textEditorView.getSelectedText());
    }

    @Test
    void insertUpdate() {
        DocumentEvent documentEvent = mock(DocumentEvent.class);

        textEditorView.insertUpdate(documentEvent);

        // Проверка взаимодействия с lineNumberPanel (не требуется в данном случае)
    }

    @Test
    void removeUpdate() {
        DocumentEvent documentEvent = mock(DocumentEvent.class);

        textEditorView.removeUpdate(documentEvent);

        // Проверка взаимодействия с lineNumberPanel (не требуется в данном случае)
    }

    @Test
    void changedUpdate() {
        DocumentEvent documentEvent = mock(DocumentEvent.class);

        textEditorView.changedUpdate(documentEvent);

        // Нет действий для проверки
    }

    @Test
    void keyPressed_Backspace_NoSelection() {
        KeyEvent keyEvent = new KeyEvent(textEditorView.getTextArea(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, ' ');
        textEditorView.setTextArea(textArea);
        when(textEditorView.getTextArea().getCaretPosition()).thenReturn(5);

        textEditorView.keyPressed(keyEvent);

        verify(presenter).onLocalDelete(5);
    }

    @Test
    void keyPressed_Backspace_WithSelection() {
        KeyEvent keyEvent = new KeyEvent(textEditorView.getTextArea(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_BACK_SPACE, ' ');
        textEditorView.setSelectStartPos(2);
        textEditorView.setSelectEndPos(6);

        textEditorView.keyPressed(keyEvent);

        verify(presenter).onLocalDeleteRange(2, 6);
        assertEquals("Tesext", textEditorView.getTextArea().getText());
    }

    @Test
    void keyPressed_ValidCharacter() {
        KeyEvent keyEvent = new KeyEvent(textEditorView.getTextArea(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_UNDEFINED, 'A');
        textEditorView.setTextArea(textArea); // Устанавливаем мок JTextArea в TextEditorView
        when(textArea.getCaretPosition()).thenReturn(5); // Мокаем вызов textArea.getCaretPosition()

        textEditorView.keyPressed(keyEvent);

        verify(presenter).onLocalInsert('A', 5);
    }

//    @Test
//    void keyPressed_ValidCharacter_WithSelection() {
//        KeyEvent keyEvent = new KeyEvent(textEditorView.getTextArea(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_UNDEFINED, 'B');
//        textEditorView.setSelectStartPos(2);
//        textEditorView.setSelectEndPos(6);
//        textEditorView.setTextArea(textArea);
//        when(textEditorView.getTextArea().getCaretPosition()).thenReturn(2);
//
//        textEditorView.keyPressed(keyEvent);
//
//        verify(presenter).onLocalDeleteRange(2, 6);
//        verify(presenter).onLocalInsert('B', 2);
//        assertEquals("TeBtext", textEditorView.getTextArea().getText());
//    }

    // Аналогично тестируем другие комбинации клавиш и их обработку

    @Test
    void keyReleased() {
        KeyEvent keyEvent = mock(KeyEvent.class);

        textEditorView.keyReleased(keyEvent);

        // Нет действий для проверки
    }

    @Test
    void keyTyped() {
        KeyEvent keyEvent = mock(KeyEvent.class);

        textEditorView.keyTyped(keyEvent);

        // Нет действий для проверки
    }

    @Test
    void clear() {
        textEditorView.clear();

        assertEquals("", textEditorView.getTextArea().getText());
        assertEquals(0, textEditorView.getTextArea().getCaretPosition());
    }

    @Test
    void insertChar() {
        textEditorView.insertChar('A', 3);

        assertEquals("TesAt text", textEditorView.getTextArea().getText());
    }

//    @Test
//    void deleteChar() {
//        when(textArea.getText()).thenReturn("Test text");  // Устанавливаем текст с помощью Mockito
//        textEditorView.setTextArea(textArea);
//        when(textArea.replaceRange("", anyInt(), anyInt())).thenReturn(textArea); // Мокаем replaceRange
//        when(textArea.getCaretPosition()).thenReturn(3);  // Мокаем getCaretPosition
//
//        textEditorView.deleteChar(4);
//
//        assertEquals("Tes text", textEditorView.getTextArea().getText());
//        assertEquals(3, textEditorView.getTextArea().getCaretPosition());
//    }

    @Test
    void insertTextBlock() {
        textEditorView.insertTextBlock("Inserted ", 4);

        assertEquals("TestInserted  text", textEditorView.getTextArea().getText());
    }

    @Test
    void deleteRange() {
        textEditorView.setTextArea(textArea);
        textEditorView.deleteRange(4, 8);
        verify(textArea).replaceRange("", 4, 8); // Проверяем вызов replaceRange()
    }

    @Test
    void setCaretPosition() {
        textEditorView.setCaretPosition(6);

        assertEquals(6, textEditorView.getTextArea().getCaretPosition());
    }

    @Test
    void getCaretPosition() {
        textEditorView.getTextArea().setCaretPosition(3);

        assertEquals(3, textEditorView.getCaretPosition());
    }

    @Test
    void getText() {
        assertEquals("Test text", textEditorView.getText());
    }

    @Test
    void pause() {
        textEditorView.pause();

        assertFalse(textEditorView.getTextArea().isEnabled());
        // Проверка отображения importDialog (не требуется в данном случае)
    }

    @Test
    void unpause() {
        textEditorView.pause();
        textEditorView.unpause();

        assertTrue(textEditorView.getTextArea().isEnabled());
    }

    @Test
    void componentResized() {
        ComponentEvent componentEvent = mock(ComponentEvent.class);

        textEditorView.componentResized(componentEvent);

        // Проверка изменения размеров панелей (не требуется в данном случае)
    }

    @Test
    void componentMoved() {
        ComponentEvent componentEvent = mock(ComponentEvent.class);

        textEditorView.componentMoved(componentEvent);

        // Нет действий для проверки
    }

    @Test
    void componentShown() {
        ComponentEvent componentEvent = mock(ComponentEvent.class);

        textEditorView.componentShown(componentEvent);

        // Нет действий для проверки
    }

    @Test
    void componentHidden() {
        ComponentEvent componentEvent = mock(ComponentEvent.class);

        textEditorView.componentHidden(componentEvent);

        // Нет действий для проверки
    }
}