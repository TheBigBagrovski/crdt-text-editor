package okp.nic.network;

import lombok.Getter;
import lombok.Setter;
import okp.nic.crdt.Char;
import okp.nic.crdt.Document;
import okp.nic.editor.TextEditor;
import okp.nic.editor.TextEditorListener;

@Getter
@Setter
public class Controller implements TextEditorListener, MessengerListener {

    private String host;
    private int port;

    private Document document;
    private String siteId;
    private TextEditor textEditor = new TextEditor(400, 400);
    private Messenger messenger;

    private int localClock = 0;

    public Controller(String host, int port, String signalHost, String signalPort) {
        siteId = "ws://" + host + ":" + port;
        document = new Document(this);
        textEditor.setTextEditorListener(this);
        messenger = new Messenger(host, port, this, signalHost, signalPort);
    }

    public void start() {
        textEditor.show();
    }

    @Override
    public void onInsert(char value, int position) {
        try {
            document.insert(position, value);
//            Char c = generateChar(position, String.valueOf(value));
//            document.localInsert(c, position);
            messenger.broadcastInsert(value, position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDelete(int position) {
        try {
//            Char charToDelete = document.ithVisible(position - 1);
//            document.integrateDelete(charToDelete);
            document.delete(position);

            messenger.broadcastDelete(position);
        } catch (Exception e) {
            e.printStackTrace();
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
        System.out.println("попытка стереть символ с индексом " + index);
        textEditor.getTextArea().replaceRange("", index - 1, index);

//        int curPos = textEditor.getCursorPos();
//        if (index <= curPos) {
//            textEditor.getTextArea().setCaretPosition(0);
//        }
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


}