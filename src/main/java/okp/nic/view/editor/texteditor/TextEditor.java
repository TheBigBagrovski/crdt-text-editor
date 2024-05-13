package okp.nic.view.editor.texteditor;

import okp.nic.view.editor.chat.ChatService;
import okp.nic.view.editor.log.LogService;
import okp.nic.view.editor.peers.PeerService;

public interface TextEditor {
    void clear();

    void insertChar(char value, int pos);

    void deleteChar(int pos);

    void insertTextBlock(String text, int pos);

    void deleteRange(int startPos, int endPos);

    void setCaretPosition(int pos);

    int getCaretPosition();

    String getText();

    void pause();

    void unpause();

    ChatService getChatService();

    PeerService getPeerService();

    LogService getLogService();

}
