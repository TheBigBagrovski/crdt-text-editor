package okp.nic.network.peer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PeerMessageType {
    UPDATE_TEXT("UPDATE_TEXT:"),
    OPERATION("OP:"),
    PASSWORD("PASSWORD:"),
    CHAT_MESSAGE("CHAT:");

    private final String prefix;

    public String formatMessage(String content) {
        return prefix + content;
    }

    public String formatOperationMessage(String type, String content) {
        return prefix + type + content;
    }

    public String formatTextUpdateMessage(String from, String text) {
        return prefix + ":<" + from + ">:" + text;
    }

}
