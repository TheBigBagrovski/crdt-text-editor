package okp.nic.network;

public interface MessengerListener {
    void handleRemoteInsert(String siteId, char value, int position);

    void handleRemoteDelete(int position);

    void handleRemoteTextUpdate(String from, String text);

    void handleRemoteInsertBlock(String from, int pos, String text);

    void handleRemoteDeleteRange(int startPos, int endPos);

    void addPeerName(String peerAddress, String name);

    void removePeerName(String peerAddress, String name);

}
