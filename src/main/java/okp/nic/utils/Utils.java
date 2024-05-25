package okp.nic.utils;

import lombok.extern.slf4j.Slf4j;
import okp.nic.logger.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class Utils {

    // метод для инкапсуляции текста в UTF-8 для Swing
    public static String getUtfString(String str) {
        return new String(str.getBytes(), StandardCharsets.UTF_8);
    }

}
