package okp.nic.editor;

public interface TextEditorListener {
    void onInsert(char value, int index);

    void onDelete(int index);
}
