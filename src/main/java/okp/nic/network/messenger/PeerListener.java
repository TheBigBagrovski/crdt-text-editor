package okp.nic.network.messenger;

public interface PeerListener {
    void startServerPeer();

    void handleRemoteInsert(String from, int pos, char value);

    void handleRemoteDelete(int pos);

    void handleRemoteTextUpdate(String from, String text);

    void handleRemoteInsertBlock(String from, int pos, String text);

    void handleRemoteDeleteRange(int startPos, int endPos);

    void handleRemoteChatMessage(String from, String message);
}
