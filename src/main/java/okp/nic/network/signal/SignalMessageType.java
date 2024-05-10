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
    INITIAL_TEXT_REQUEST("SIGNAL:INITIAL_TEXT_REQ_TO:"),
    PASSWORD_REQUEST("SIGNAL:PASSWORD_REQ");

    private final String prefix;

    public String formatMessage(String content) {
        return prefix + content;
    }

    public String formatWelcomeMessage(Collection<String[]> clients) {
        if (clients.isEmpty()) {
            return getPrefix() + "NONE";
        }
        StringBuilder peersList = new StringBuilder();
        for (String[] client : clients) {
            peersList.append(client[0]).append("-").append(client[1]).append(", ");
        }
        return getPrefix() + peersList;
    }

}