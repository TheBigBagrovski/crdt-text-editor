package okp.nic.network.peer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PeerMessageType {
    COMPRESSED_TEXT("COMPRESSED_TEXT:"),
    OPERATION("OP:"),
    CURRENT_STATE("CURRENT_STATE:"),
    PASSWORD("PASSWORD:");

    private final String prefix;

    public String formatMessage(String content) {
        return prefix + content;
    }

    public String formatOperationMessage(String type, String content) {
        return prefix + type + content;
    }

}
