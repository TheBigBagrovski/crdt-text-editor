package okp.nic.network;

public interface MessengerListener {
    void handleRemoteInsert(String siteId, char value, int position);

    void handleRemoteDelete(int position);

}
