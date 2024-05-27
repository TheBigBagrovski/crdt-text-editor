package okp.nic.presenter;

import okp.nic.network.messenger.Messenger;

public interface Presenter {
    void start(Messenger messenger, String name);

    void clear();

    void onLocalInsert(char value, int index);

    void onLocalDelete(int index);

    void onLocalFileImport(String text);

    void onLocalInsertBlock(int pos, String text);

    void onLocalDeleteRange(int startPos, int endPos);

    String getCurrentDocument();

    void sendChatMessage(String message);

    void saveFile();

    void loadFile();

}

