package okp.nic.network.messenger;

public interface SignalListener {
    void connectToSignalServer(String signalServerAddress, String name);

    void handlePasswordRequest();

    void handleRemotePeerConnected(String peerAddress, String peerName);

    void handleRemotePeerDisconnected(String disconnectedPeer);

    void handleRemoteCurrentStateRequest(String peerAddress);


}
