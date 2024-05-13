package okp.nic.view.editor.log;

import lombok.AllArgsConstructor;

import static okp.nic.utils.Utils.getUtfString;

@AllArgsConstructor
public class LogServiceImpl implements LogService {

    private final LogPanel logPanel;

    @Override
    public void showLog(String message) {
        logPanel.getLogArea().setLineWrap(true);
        logPanel.getLogArea().setWrapStyleWord(true);
        logPanel.getLogArea().append(getUtfString(message) + "\n");
        logPanel.getLogArea().setCaretPosition(logPanel.getLogArea().getDocument().getLength());
    }

}
