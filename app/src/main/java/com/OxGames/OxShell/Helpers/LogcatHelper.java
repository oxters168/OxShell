package com.OxGames.OxShell.Helpers;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

// source: https://stackoverflow.com/questions/45229996/how-do-i-get-the-app-log-from-a-real-android-device
public class LogcatHelper {
    private static final HashMap<String, LogcatHelper> helpers = new HashMap<>();
    private final String STORAGE_DIR_INTERNAL;
    private final String PATH_LOGCAT;
    private LogDumper mLogDumper = null;
    private final int pid;
    private final String pkgName;
    private int logStartCount = 0;

    public static LogcatHelper getInstance(Context context) {
        String pkgName = context.getPackageName();
        if (!helpers.containsKey(pkgName)) {
            //Log.d("LogcatHelper", "Creating new entry for " + pkgName);
            return new LogcatHelper(context);
        } else {
            //Log.d("LogcatHelper", "Found entry for " + pkgName);
            return helpers.get(pkgName);
        }
    }

    private LogcatHelper(Context context) {
        STORAGE_DIR_INTERNAL = AndroidHelpers.combinePaths(context.getExternalFilesDir(null).toString(), "Logs");
        PATH_LOGCAT = AndroidHelpers.combinePaths(STORAGE_DIR_INTERNAL, getFileName() + ".log");
        pkgName = context.getPackageName();
        pid = android.os.Process.myPid();
    }

    public void start() {
        logStartCount++;
        Log.i("LogcatHelper", "Starting log for " + pkgName + " => " + logStartCount);
        if (!helpers.containsKey(pkgName)) {
            helpers.put(pkgName, this);
            if (mLogDumper == null)
                mLogDumper = new LogDumper();
            mLogDumper.start();
        }
    }

    public void stop() {
        logStartCount--;
        Log.i("LogcatHelper", "Ending log for " + pkgName + " => " + logStartCount);
        if (logStartCount <= 0 && helpers.containsKey(pkgName)) {
            helpers.remove(pkgName);
            if (mLogDumper != null) {
                mLogDumper.stopLogs();
                mLogDumper = null;
            }
        }
    }

    private class LogDumper extends Thread {

        private Process logcatProc;
        private BufferedReader mReader = null;
        private boolean mRunning = true;
        String cmds = null;
        //private final String mPID;
        private FileOutputStream out = null;

        public LogDumper() {
            //mPID = pid;
            Log.i("LogcatHelper", "pid " + pid);
            try {
                File dir = new File(STORAGE_DIR_INTERNAL);
                if (!dir.exists())
                    dir.mkdirs();
//                File file = new File(PATH_LOGCAT);
//                if (!file.exists())
//                    file.createNewFile();
                out = new FileOutputStream(PATH_LOGCAT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\""; // print e level and ilevel info
            // cmds = "logcat  | grep \"(" + mPID + ")\"";// print all
            // cmds = "logcat -s way";// print filter info
            cmds = "logcat *:e *:i | grep \"(" + pid + ")\"";

        }

        public void stopLogs() {
            mRunning = false;
        }

        @Override
        public void run() {
            try {
                //Log.d("LogcatHelper", "Start run");
                logcatProc = Runtime.getRuntime().exec(cmds);
                mReader = new BufferedReader(new InputStreamReader(logcatProc.getInputStream()), 1024);
                String line;
                while (mRunning && (line = mReader.readLine()) != null) {
                    //Log.d("LogcatHelper", "Logging");
                    if (!mRunning) {
                        break;
                    }
                    if (line.length() == 0) {
                        continue;
                    }
                    if (out != null && line.contains(String.valueOf(pid))) {
                        out.write((getDateEN() + "  " + line + "\n").getBytes());
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (logcatProc != null) {
                    logcatProc.destroy();
                    logcatProc = null;
                }
                if (mReader != null) {
                    try {
                        mReader.close();
                        mReader = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    out = null;
                }

            }

        }

    }

    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;
    }

    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;
    }
}