package okp.nic.view.editor.chat;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatPanel chatPanel;

    @Override
    public void showChatMessage(String author, String message) {
        chatPanel.getChatArea().setLineWrap(true);
        chatPanel.getChatArea().setWrapStyleWord(true);
        chatPanel.getChatArea().append(author + ": " + message + "\n");
        chatPanel.getChatArea().setCaretPosition(chatPanel.getChatArea().getDocument().getLength());
    }

}
