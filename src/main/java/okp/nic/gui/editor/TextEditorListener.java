package okp.nic.gui.editor;

public interface TextEditorListener {
    void clear();

    void onLocalInsert(char value, int index);

    void onLocalDelete(int index);

    void onLocalFileImport(String text);

    void onLocalInsertBlock(int pos, String text);

    void onLocalDeleteRange(int startPos, int endPos);

    String getCurrentDocument();

}
