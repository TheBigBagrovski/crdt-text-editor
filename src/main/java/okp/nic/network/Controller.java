package okp.nic.network;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okp.nic.crdt.Document;
import okp.nic.editor.TextEditor;
import okp.nic.editor.TextEditorListener;

@Slf4j
public class Controller implements TextEditorListener, MessengerListener {

    private final Document document;

    @Getter
    private final String siteId;
    private TextEditor textEditor;
    private final Messenger messenger;

    @Getter
    private int localClock = 0;

    public Controller(String host, int port, String signalHost, String signalPort) {
        siteId = "ws://" + host + ":" + port;
        document = new Document(this);
        textEditor = new TextEditor(this);
        messenger = new Messenger(host, port, this, signalHost, signalPort);
    }

    public void clear() {
        document.clearDocument();
        textEditor.clearTextArea();
    }

    @Override
    public void onInsert(char value, int position) {
        try {
            document.insert(position, value);
            messenger.broadcastInsert(value, position);
        } catch (Exception e) {
            log.info("Ошибка при вставке символа " + value + " на поизицию " + position);
        }
    }

    @Override
    public void onDelete(int position) {
        try {
            document.delete(position);
            messenger.broadcastDelete(position);
        } catch (Exception e) {
            log.info("Ошибка при удалении символа на позиции " + position);

        }
    }

    public void insertToTextEditor(char value, int index) {
        textEditor.getTextArea().insert(String.valueOf(value), index);
        int curPos = textEditor.getCursorPos();
        if (index <= curPos) {
            textEditor.getTextArea().setCaretPosition(curPos + 1);
        }
    }

    public void deleteToTextEditor(int index) {
        if (index == 0) {
            return;
        }
        int curPos = textEditor.getCursorPos();
        if (index <= curPos) {
            textEditor.setCursorPos(curPos - 1);
            textEditor.getTextArea().setCaretPosition(curPos - 1);
        }
        textEditor.getTextArea().replaceRange("", index - 1, index);

    }

    @Override
    public void handleRemoteInsert(char value, int position) {
        document.insert(position, value);
        insertToTextEditor(value, position);
    }

    @Override
    public void handleRemoteDelete(int position) {
        document.delete(position);
        deleteToTextEditor(position);
    }

    public void incrementLocalClock() {
        localClock++;
    }

    public void importTextFromFile(char[] text) {
        clear();
        messenger.broadcastClear();
        int i = 0;
        for (char c : text) {
            onInsert(c, i);
            textEditor.getTextArea().insert(String.valueOf(c), i);
//            insertToTextEditor(c, i);
            i++;
        }
    }

    public String getText() {
        System.out.println(document.content());
        return document.content();
    }

}