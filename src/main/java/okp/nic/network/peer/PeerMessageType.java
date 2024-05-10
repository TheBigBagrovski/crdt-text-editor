package okp.nic.network.peer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PeerMessageType {
    TEXT_BLOCK("TEXT_BLOCK:"),
    UPDATE_TEXT("UPDATE_TEXT:"),
    OPERATION("OP:"),
    PASSWORD("PASSWORD:");

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

    public String formatTextBlockMessage(int pos, String text) {
        return prefix + pos + ":" + text;
    }

}
