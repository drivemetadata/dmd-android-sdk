package com.drivemetadata.utils;

import static com.drivemetadata.enums.LogLevel.DEBUG;
import static com.drivemetadata.enums.LogLevel.INFO;
import static com.drivemetadata.enums.LogLevel.VERBOSE;
import android.util.Log;
import com.drivemetadata.BuildConfig;
import com.drivemetadata.enums.LogLevel;

public final class DriveMetaDataLogger {

    private final String tag;

    public DriveMetaDataLogger(String tag) {
        this.tag = tag;
    }

    /** Log a verbose message. */
    public void verbose(String format, Object... extra) {
        if (shouldLog(VERBOSE)) {
            Log.v(tag, String.format(format, extra));
        }
    }

    /** Log an info message. */
    public void info(String format, Object... extra) {
        if (shouldLog(INFO)) {
            Log.i(tag, String.format(format, extra));
        }
    }

    /** Log a debug message. */
    public void debug(String format, Object... extra) {
        if (shouldLog(DEBUG)) {
            Log.d(tag, String.format(format, extra));
        }
    }

    /** Log an error message. */
    public void error(Throwable error, String format, Object... extra) {
        if (shouldLog(INFO)) {
            Log.e(tag, String.format(format, extra), error);
        }
    }

    private boolean shouldLog(LogLevel level) {

        if(BuildConfig.BUILD_TYPE.equals("debug")){
           return true;
        }
        return false;
    }
}
