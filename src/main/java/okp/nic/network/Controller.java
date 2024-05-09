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

    @Getter
    private final Messenger messenger;
    private final Document document;
    private final TextEditor textEditor;

    @Getter
    private final String siteId;

    public Controller(String host, int port, String signalHost, String signalPort) {
        siteId = "ws://" + host + ":" + port;
        document = new Document();
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
            document.insertChar(siteId, position, value);
            messenger.broadcastInsert(value, position);
        } catch (Exception e) {
            log.info("Ошибка при вставке символа " + value + " на поизицию " + position);
        }
    }

    @Override
    public void onDelete(int position) {
        try {
            document.deleteChar(position);
            messenger.broadcastDelete(position);
        } catch (Exception e) {
            log.info("Ошибка при удалении символа на позиции " + position);

        }
    }

    @Override
    public void handleRemoteInsert(String from, char value, int position) {
        document.insertChar(from, position, value);
        insertCharToTextEditor(value, position);
    }

    @Override
    public void handleRemoteDelete(int position) {
        document.deleteChar(position);
        deleteCharFromTextEditor(position);
    }

    public void insertCharToTextEditor(char value, int index) {
        textEditor.getTextArea().insert(String.valueOf(value), index);
        int curPos = textEditor.getCursorPos();
        if (index <= curPos) {
            textEditor.getTextArea().setCaretPosition(curPos + 1);
        }
    }

    public void deleteCharFromTextEditor(int index) {
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

    public void importTextFromFile(String text) {
        new Thread(() -> {
            document.updateContent(siteId, text);
            textEditor.getTextArea().insert(text, 0);
        }).start();
        messenger.broadcastTextBlock(compress(text));
    }

    public void insertText(String from, byte[] compressedText) {
        String text = decompress(compressedText);
        clear();
        textEditor.pause();
        document.updateContent(from, text);
        textEditor.getTextArea().insert(text, 0);
        textEditor.unpause();
    }

    public byte[] getCompressedText() {
        return compress(document.getContent());
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
        return baos.toString();
    }

}