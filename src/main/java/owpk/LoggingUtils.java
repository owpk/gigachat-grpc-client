package owpk;

import org.slf4j.Logger;

public class LoggingUtils {

    public static String cliCommandLog(Class<?> cl) {
        return String.format("executing command: %s", cl.getSimpleName());
    }

    public static void cliCommandLog(Class<?> cl, Logger logger) {
        logger.info(cliCommandLog(cl));
    }
}
