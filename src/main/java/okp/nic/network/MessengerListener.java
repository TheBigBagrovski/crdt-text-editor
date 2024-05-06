package okp.nic.network;

import okp.nic.crdt.Char;

public interface MessengerListener {
    void handleRemoteInsert(char value, int position);

    void handleRemoteDelete(int position);

}
