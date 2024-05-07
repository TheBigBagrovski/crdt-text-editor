package okp.nic.network;

public interface MessengerListener {
    void handleRemoteInsert(char value, int position);

    void handleRemoteDelete(int position);

}
