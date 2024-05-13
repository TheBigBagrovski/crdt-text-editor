package okp.nic.network.messenger;

public interface PeerPublisher {
    void broadcastInsert(char value, int pos);

    void broadcastDelete(int pos);

    void broadcastDeleteRange(int startPos, int endPos);

    void broadcastTextUpdate(String text);

    void broadcastInsertBlock(int pos, String text);

    void broadcastChatMessage(String message);

}
