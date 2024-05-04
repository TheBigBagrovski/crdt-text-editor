package okp.nic.network;

import lombok.Getter;
import lombok.Setter;
import okp.nic.crdt.CRDT;
import okp.nic.crdt.Char;
import okp.nic.editor.TextEditor;
import okp.nic.editor.TextEditorListener;
import okp.nic.vectorclock.Version;
import okp.nic.vectorclock.VersionVector;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Controller implements TextEditorListener, MessengerListener {

    private String host;
    private int port;

    private CRDT crdt;
    private String siteId;
    private TextEditor textEditor = new TextEditor(400, 400);
    private Messenger messenger;

    private VersionVector versionVector;
    private List<Operation> deletionBuffer = new ArrayList<>();

    public Controller(String host, int port, String signalHost, String signalPort) {
        siteId = "ws://" + host + ":" + port;
        versionVector = new VersionVector(siteId);
        crdt = new CRDT(siteId, this);
        textEditor.setTextEditorListener(this);
        messenger = new Messenger(host, port, this, signalHost, signalPort);
    }

    public void start() {
        textEditor.show();
    }

    @Override
    public void onInsert(char value, int index) {
        Char c = crdt.localInsert(value, index);
        messenger.broadcastInsert(c);
    }

    @Override
    public void onDelete(int index) {
        System.out.println("[onDelete] START");
        System.out.println("[onDelete] call localDelete");
        Char c = crdt.localDelete(index);
        System.out.println("[onDelete] call broadCastDelete");
        messenger.broadcastDelete(c);
        System.out.println("[onDelete] FINISH");
    }

    public void insertToTextEditor(char value, int index) {
        textEditor.getTextArea().insert(String.valueOf(value), index);
        int curPos = textEditor.getCursorPos();
        if (index <= curPos) {
            textEditor.getTextArea().setCaretPosition(curPos + 1);
        }
    }

    public void deleteToTextEditor(int index) {
        System.out.println("[deleteToTextEditor] START");
        System.out.println("[deleteToTextEditor] >> delete in editor at index = " + index);
        textEditor.getTextArea().replaceRange("", index, index + 1);
        int curPos = textEditor.getCursorPos();
        if (index <= curPos) {
            System.out.println("[deleteToTextEditor] >> cursorPos = " + curPos);
            System.out.println("[deleteToTextEditor] >> setCaretPosition to " + (curPos - 1));
            textEditor.getTextArea().setCaretPosition(0);
            System.out.println("[deleteToTextEditor] >> after update, cursorPos = " + textEditor.getCursorPos());
        }
        System.out.println("[deleteToTextEditor] FINISH");
    }

    @Override
    public void handleRemoteInsert(Char c) {
        Version operationVersion = new Version(c.getSiteId(), c.getCounter());
        if (versionVector.hasBeenApplied(operationVersion)) {
            return;
        }
        crdt.remoteInsert(c);
        versionVector.update(operationVersion);
        processDeletionBuffer();
    }

    @Override
    public void handleRemoteDelete(Char c) {
        System.out.println("[handleRemoteDelete] START");
        System.out.println("[handleRemoteDelete] >> preparing operation");
        System.out.println("[handleRemoteDelete] >> check whether operation was already applied");
        Operation operation = new Operation(c, "delete");
        System.out.println("[handleRemoteDelete] >> add op to deletionBuffer");
        deletionBuffer.add(operation);
        System.out.println("[handleRemoteDelete] >> call processDeletionBuffer");
        processDeletionBuffer();
        System.out.println("[handleRemoteDelete] FINISH");
    }

    public void processDeletionBuffer() {
        System.out.println("[processDeletionBuffer] START");
        int huyu = 0;
        while (huyu < deletionBuffer.size()) {
            System.out.println("[processDeletionBuffer] >> check index = " + huyu);
            Operation op = deletionBuffer.get(huyu);
            System.out.println("[processDeletionBuffer] >> value = " + op.getData().getValue() + ", counter = " + op.getData().getCounter());
            System.out.println("[processDeletionBuffer] >> siteId = " + op.getData().getSiteId());
            if (hasInsertionBeenApplied(op)) {
                Version operationVersion = new Version(op.getData().getSiteId(), op.getData().getCounter());
                System.out.println("[processDeletionBuffer] >> currentCount = " + versionVector.getVersionFromVector(operationVersion).getCounter());
                crdt.remoteDelete(op.getData());
                versionVector.update(operationVersion);
                deletionBuffer.remove(op);
            } else {
                System.out.println("[processDeletionBuffer] >>>> insertion hasn't been applied yet!");
                huyu++;
            }
        }
    }

    public boolean hasInsertionBeenApplied(Operation op) {
        Version charVersion = new Version(op.getData().getSiteId(), op.getData().getCounter());
        return versionVector.hasBeenApplied(charVersion);
    }

    public List<Char> getCurrentText() {
        return crdt.getStruct();
    }

    public VersionVector getCurrentVersionVector() {
        return crdt.getVersionVector();
    }

}


/*

 */