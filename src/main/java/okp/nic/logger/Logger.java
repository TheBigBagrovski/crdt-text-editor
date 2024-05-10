package okp.nic.logger;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okp.nic.gui.editor.TextEditor;

@AllArgsConstructor
@Slf4j
public class Logger {

    private final TextEditor textEditor;

    public void info(String message) {
        infoToConsole(message);
        logToEditor("INFO: " + message);
    }

    public void error(String message) {
        errorToConsole(message);
        logToEditor("ERROR: " + message);
    }

    public void infoToConsole(String message) {
        log.info(message);
    }

    public void errorToConsole(String message) {
        log.error(message);
    }

    private void logToEditor(String message) {
        textEditor.writeLog(message);
    }
}
