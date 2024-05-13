package okp.nic.logger;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okp.nic.view.editor.log.LogService;

@AllArgsConstructor
@Slf4j
public class Logger {

    private static LogService logService;

    public static void setLogService(LogService logService) {
        Logger.logService = logService;
    }

    public static void info(String message) {
        infoToConsole(message);
        logToEditor("INFO: " + message);
    }

    public static void error(String message) {
        errorToConsole(message);
        logToEditor("ERROR: " + message);
    }

    private static void infoToConsole(String message) {
        log.info(message);
    }

    private static void errorToConsole(String message) {
        log.error(message);
    }

    private static void logToEditor(String message) {
            logService.showLog(message);
    }

}
