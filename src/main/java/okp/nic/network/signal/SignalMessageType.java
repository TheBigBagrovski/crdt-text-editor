package okp.nic.network.signal;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@Getter
@AllArgsConstructor
public enum SignalMessageType {
    WELCOME("SIGNAL:WELCOME.PEERS:"),
    PEER_CONNECTED("SIGNAL:CONNECTED:"),
    PEER_DISCONNECTED("SIGNAL:DISCONNECTED:"),
    INITIAL_TEXT_REQUEST("SIGNAL:INITIAL_TEXT_REQ_TO:");

    private final String prefix;

    public String formatMessage(String content) {
        return prefix + content;
    }

    public String formatWelcomeMessage(Collection<String> clients) {
        String peersList = clients.isEmpty() ? "NONE" : String.join(", ", clients);
        return getPrefix() + peersList;
    }

}