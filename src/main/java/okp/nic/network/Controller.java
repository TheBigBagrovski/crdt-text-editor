package okp.nic.network;

import lombok.Getter;
import okp.nic.crdt.Document;
import okp.nic.gui.editor.TextEditor;
import okp.nic.gui.editor.TextEditorListener;
import okp.nic.logger.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    public void clear() {
        document.clearDocument();
        textEditor.clearTextArea();
    }

    @Override
    public void onInsert(char value, int position) {
        try {
            document.insertChar(siteId, position, value);
            messenger.broadcastInsert(value, position);
        } catch (Exception e) {
            logger.error("Ошибка при вставке символа " + value + " на поизицию " + position);
        }
    }

    @Override
    public void onDelete(int position) {
        try {
            document.deleteChar(position);
            messenger.broadcastDelete(position);
        } catch (Exception e) {
            logger.error("Ошибка при удалении символа на позиции " + position);

        }
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

    public void importTextFromFile(String text) {
        new Thread(() -> {
            clear();
            document.insertTextBlock(siteId, 0, text);
            textEditor.getTextArea().insert(text, 0);
            textEditor.unpause();
        }).start();
        messenger.broadcastTextUpdate(compress(text));
    }

    public void updateText(String from, byte[] compressedText) {
        String text = decompress(compressedText);
        clear();
        textEditor.pause();
        document.insertTextBlock(from, 0, text);
        textEditor.getTextArea().insert(text, 0);
        textEditor.unpause();
    }

    public void handlePasteTextBlock(int pos, String text) {
        document.insertTextBlock(siteId, pos, text);
        textEditor.getTextArea().insert(text, pos);
        messenger.broadcastTextBlock(compress(text), pos);
    }

    public void insertTextBlock(String from, int pos, byte[] compressedText) {
        String text = decompress(compressedText);
        document.insertTextBlock(from, pos, text);
        textEditor.getTextArea().insert(text, pos);
    }

    public void deleteRange(int startPos, int endPos) {
        document.deleteRange(startPos, endPos);
        textEditor.setCursorPos(startPos);
        textEditor.getTextArea().setCaretPosition(startPos);
        textEditor.getTextArea().replaceRange("", startPos, endPos);
    }

    public byte[] getCompressedText() {
        String text = document.getContent();
        return compress(text);
    }

    public void addPeerName(String peerAddress, String name) {
        textEditor.addPeerName(name + " [" + peerAddress + "]");
    }

    public void removePeerName(String peerAddress, String name) {
        textEditor.removePeerName(name + " [" + peerAddress + "]");

    }

    private byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(text.getBytes());
        } catch (IOException e) {
            logger.error("Ошибка при сжатии файла: " + e.getMessage());
        }
        return baos.toByteArray();
    }

    private String decompress(byte[] compressedText) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(compressedText))) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            logger.error("Ошибка при распаковке файла: " + e.getMessage());
        }
        return baos.toString();
    }

}