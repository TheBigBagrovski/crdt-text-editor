package okp.nic.network;

import okp.nic.crdt.Char;

public interface MessengerListener {
    void handleRemoteInsert(Char c);

    void handleRemoteDelete(Char c);

}
