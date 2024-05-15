package okp.nic.presenter;

import lombok.Getter;
import lombok.Setter;
import okp.nic.logger.Logger;
import okp.nic.model.Document;
import okp.nic.network.messenger.Messenger;
import okp.nic.view.editor.texteditor.TextEditor;
import okp.nic.view.editor.texteditor.TextEditorView;

import javax.swing.JFileChooser;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import static okp.nic.utils.Utils.getUtfString;

@Getter
@Setter
public class PresenterImpl implements Presenter, MessengerListener {

    private Messenger messenger;
    private Document document;
    private TextEditor textEditor;

    private final String siteId;

    public PresenterImpl(String host, int port) {
        siteId = "ws://" + host + ":" + port;
    }

    public void start(Messenger messenger, String name) {
        this.messenger = messenger;
        document = new Document();
        textEditor = new TextEditorView(this);
        addPeerName(siteId, name);
    }

    @Override
    public void clear() {
        document.clearDocument();
        textEditor.clear();
    }

    @Override
    public void onLocalInsert(char value, int pos) {
        try {
            document.insertChar(siteId, pos, value);
            messenger.broadcastInsert(value, pos);
        } catch (Exception e) {
            Logger.error("Ошибка при вставке символа " + value + " на поизицию " + pos);
        }
    }

    @Override
    public void onLocalDelete(int pos) {
        try {
            document.deleteChar(pos);
            messenger.broadcastDelete(pos);
        } catch (Exception e) {
            Logger.error("Ошибка при удалении символа на позиции " + pos);

        }
    }

    @Override
    public void onLocalFileImport(String text) {
        new Thread(() -> {
            clear();
            document.insertTextBlock(siteId, 0, text);
            textEditor.insertTextBlock(text, 0);
            textEditor.unpause();
        }).start();
        messenger.broadcastTextUpdate(text);
    }

    @Override
    public void onLocalInsertBlock(int pos, String text) {
        document.insertTextBlock(siteId, pos, text);
        messenger.broadcastInsertBlock(pos, text);
    }

    @Override
    public void onLocalDeleteRange(int startPos, int endPos) {
        document.deleteRange(startPos, endPos);
        messenger.broadcastDeleteRange(startPos, endPos);
    }

    @Override
    public void sendChatMessage(String message) {
        textEditor.getChatService().showChatMessage(getUtfString("Вы"), message);
        messenger.broadcastChatMessage(message);
    }

    @Override
    public String getCurrentDocument() {
        return document.getContent();
    }

    @Override
    public void saveFile() {
//        JFileChooser fileChooser = new JFileChooser();
//        int returnValue = fileChooser.showSaveDialog(null);
//        if (returnValue == JFileChooser.APPROVE_OPTION) {
//            File selectedFile = fileChooser.getSelectedFile();
//            try (FileWriter writer = new FileWriter(selectedFile)) {
//                writer.write(textEditor.getText());
//            } catch (IOException ex) {
//                Logger.error("Ошибка при сохранении файла: " + ex.getMessage());
//            }
//        }
    }

    @Override
    public void loadFile() {
        JFileChooser fileChooser = new JFileChooser();
//        int returnValue = fileChooser.showOpenDialog(null);
        int returnValue = 0;
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            textEditor.pause();
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                StringBuilder fileContent = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    fileContent.append(line).append("\n");
                }
                onLocalFileImport(fileContent.toString());
            } catch (IOException ex) {
                Logger.error("Ошибка при загрузке файла: " + ex.getMessage());
            }
        }
    }

    @Override
    public void handleRemoteInsert(String from, char value, int pos) {
        document.insertChar(from, pos, value);
        textEditor.insertChar(value, pos);
    }

    @Override
    public void handleRemoteDelete(int pos) {
        document.deleteChar(pos);
        textEditor.deleteChar(pos);
    }

    @Override
    public void handleRemoteTextUpdate(String from, String text) {
        clear();
        textEditor.pause();
        document.insertTextBlock(from, 0, text);
        textEditor.insertTextBlock(text, 0);
        textEditor.setCaretPosition(0);
        textEditor.unpause();
    }

    @Override
    public void handleRemoteInsertBlock(String from, int pos, String text) {
        document.insertTextBlock(from, pos, text);
        textEditor.insertTextBlock(text, pos);
        textEditor.setCaretPosition(textEditor.getCaretPosition() + text.length());
    }

    @Override
    public void handleRemoteDeleteRange(int startPos, int endPos) {
        document.deleteRange(startPos, endPos);
        textEditor.deleteRange(startPos, endPos);
        textEditor.setCaretPosition(startPos);
    }

    @Override
    public void addPeerName(String peerAddress, String name) {
        textEditor.getPeerService().addPeerName(name + " [" + peerAddress.substring(5) + "]");
    }

    @Override
    public void removePeerName(String peerAddress, String name) {
        textEditor.getPeerService().removePeerName(name + " [" + peerAddress.substring(5) + "]");
    }

    @Override
    public void handleRemoteChatMessage(String from, String message) {
        textEditor.getChatService().showChatMessage(from, message);
    }

}
