package okp.nic.network;

import lombok.Getter;
import okp.nic.crdt.Char;
import okp.nic.crdt.Document;
import okp.nic.gui.editor.TextEditor;
import okp.nic.gui.editor.TextEditorListener;
import okp.nic.logger.Logger;

public class Controller implements TextEditorListener, MessengerListener {

    @Getter
    private Messenger messenger;
    private Document document;
    private TextEditor textEditor;

    @Getter
    private Logger logger;

    @Getter
    private final String siteId;

    public Controller(String host, int port) {
        siteId = "ws://" + host + ":" + port;
    }

    public void start(Messenger messenger, String name) {
        this.messenger = messenger;
        document = new Document();
        textEditor = new TextEditor(this, logger);
        logger = new Logger(textEditor);
        addPeerName(siteId, name);
    }

    @Override
    public void clear() {
        document.clearDocument();
        textEditor.clearTextArea();
    }

    @Override
    public void onLocalInsert(char value, int position) {
        try {
            document.insertChar(siteId, position, value);
            messenger.broadcastInsert(value, position);
        } catch (Exception e) {
            logger.error("Ошибка при вставке символа " + value + " на поизицию " + position);
        }
    }

    @Override
    public void onLocalDelete(int position) {
        try {
            document.deleteChar(position);
            messenger.broadcastDelete(position);
        } catch (Exception e) {
            logger.error("Ошибка при удалении символа на позиции " + position);

        }
    }

    @Override
    public void onLocalFileImport(String text) {
        new Thread(() -> {
            clear();
            document.insertTextBlock(siteId, 0, text);
            textEditor.getTextArea().insert(text, 0);
            textEditor.unpause();
        }).start();
        messenger.broadcastTextUpdate(text);
    }

    @Override
    public void onLocalInsertBlock(int pos, String text) {
        document.insertTextBlock(siteId, pos, text);
        textEditor.getTextArea().insert(text, pos);
        messenger.broadcastInsertBlock(pos, text);
    }

    @Override
    public void onLocalDeleteRange(int startPos, int endPos) {
        document.deleteRange(startPos, endPos);
        textEditor.getTextArea().replaceRange("", startPos, endPos);
        textEditor.setCursorPos(startPos);
        messenger.broadcastDeleteRange(startPos, endPos);
    }

    public void onlyInDocLocalDeleteRange(int startPos, int endPos) {
        document.deleteRange(startPos, endPos);
//        textEditor.getTextArea().replaceRange("", startPos, endPos);
        textEditor.setCursorPos(startPos);
        messenger.broadcastDeleteRange(startPos, endPos);
    }

//    public void cut(int startPos, int endPos) {
//        document.deleteRange(startPos, endPos);
//        textEditor.getTextArea().replaceRange("", startPos, endPos);
//        textEditor.setCursorPos(startPos);
//        messenger.broadcastDeleteRange(startPos, endPos);
//    }

    @Override
    public String getCurrentDocument() {
        return document.getContent();
    }

    @Override
    public void handleRemoteInsert(String from, char value, int position) {
        document.insertChar(from, position, value);
        textEditor.insertCharToTextEditor(value, position);
    }

    @Override
    public void handleRemoteDelete(int position) {
        document.deleteChar(position);
        textEditor.deleteCharFromTextEditor(position);
    }

    @Override
    public void handleRemoteTextUpdate(String from, String text) {
        clear();
        textEditor.pause();
        document.insertTextBlock(from, 0, text);
        textEditor.getTextArea().insert(text, 0);
        textEditor.unpause();
    }

    @Override
    public void handleRemoteInsertBlock(String from, int pos, String text) {
        document.insertTextBlock(from, pos, text);
        textEditor.getTextArea().insert(text, pos);
    }

    @Override
    public void handleRemoteDeleteRange(int startPos, int endPos) {
        document.deleteRange(startPos, endPos);
        textEditor.getTextArea().replaceRange("", startPos, endPos);
        if (textEditor.getCursorPos() < endPos && textEditor.getCursorPos() >= startPos) {
            textEditor.getTextArea().setCaretPosition(startPos + 1);
            textEditor.setCursorPos(startPos + 1);
        }
    }

    @Override
    public void addPeerName(String peerAddress, String name) {
        textEditor.addPeerName(name + " [" + peerAddress + "]");
    }

    @Override
    public void removePeerName(String peerAddress, String name) {
        textEditor.removePeerName(name + " [" + peerAddress + "]");

    }

}