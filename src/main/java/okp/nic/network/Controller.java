package okp.nic.network;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okp.nic.crdt.Document;
import okp.nic.editor.TextEditor;
import okp.nic.editor.TextEditorListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class Controller implements TextEditorListener, MessengerListener {

    private static final int BLOCK_SIZE = 1000;

    private final Document document;

    @Getter
    private final String siteId;
    private final TextEditor textEditor;

    @Getter
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
            document.insert(siteId, position, value);
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
    public void handleRemoteInsert(String from, char value, int position) {
        document.insert(from, position, value);
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

//    public void importTextFromFile(String text) {
//        int i = textEditor.getCursorPos();
//        for (char c : text.toCharArray()) {
//            document.insert(siteId, i, c);
//            textEditor.getTextArea().insert(String.valueOf(c), i);
//            i++;
//        }
//        messenger.broadcastText(text);
//    }

    public void importTextFromFile(String text) {
        int totalBlocks = (int) Math.ceil((double) text.length() / BLOCK_SIZE);
        int currentBlock = 0;
        textEditor.showProgress(0);
        for (int j = 0; j < text.length(); j += BLOCK_SIZE) {
            String block = text.substring(j, Math.min(j + BLOCK_SIZE, text.length()));
            document.insertBlock(siteId, textEditor.getCursorPos(), block);
            textEditor.getTextArea().insert(block, textEditor.getCursorPos());
            byte[] compressedBlock = compress(block);
            messenger.broadcastTextBlock(compressedBlock);
            currentBlock++;
            textEditor.showProgress((int) Math.round(((double) currentBlock / totalBlocks) * 100));
        }
        textEditor.hideProgress();
    }

    public void insertText(String from, byte[] compressedText) {
        String text = decompress(compressedText);
        int i = textEditor.getCursorPos();
        textEditor.showProgress(0);
        for (char c : text.toCharArray()) {
            handleRemoteInsert(from, c, i++);
        }
//        int totalBlocks = (int) Math.ceil((double) text.length() / BLOCK_SIZE);
//        int currentBlock = 0;
//        for (int j = 0; j < text.length(); j += BLOCK_SIZE) {
//            String block = text.substring(j, Math.min(j + BLOCK_SIZE, text.length()));
//            handleRemoteInsert(from, i, block);
//            i += block.length();
//            currentBlock++;
//            textEditor.showProgress((int) Math.round(((double) currentBlock / totalBlocks) * 100));
//        }
        textEditor.hideProgress();
    }

    public String getText() {
        return document.content();
    }

    public void insertText(String from, String text) {
        int i = textEditor.getCursorPos();
        for (char c : text.toCharArray()) {
            handleRemoteInsert(from, c, i++);
        }
    }

    private byte[] compress(String text) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(text.getBytes());
        } catch (IOException e) {
            log.error("Ошибка сжатия: " + e.getMessage());
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
            log.error("Ошибка распаковки: " + e.getMessage());
        }
        return new String(baos.toByteArray());
    }

}