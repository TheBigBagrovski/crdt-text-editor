package okp.nic.view.editor.chat;

import lombok.AllArgsConstructor;
import okp.nic.presenter.Presenter;

import static okp.nic.utils.Utils.getUtfString;

@AllArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final Presenter presenter;
    private final ChatPanel chatPanel;

    @Override
    public void sendChatMessage(String message) {
        presenter.sendChatMessage(message);
        showChatMessage(getUtfString("Вы"), message);
    }

    @Override
    public void showChatMessage(String author, String message) {
        chatPanel.getChatArea().setLineWrap(true);
        chatPanel.getChatArea().setWrapStyleWord(true);
        chatPanel.getChatArea().append(author + ": " + message + "\n");
        chatPanel.getChatArea().setCaretPosition(chatPanel.getChatArea().getDocument().getLength());
    }


}
