package com.chukong.sdk.common;

import java.io.IOException;

import android.content.Context;

public class CmdExecutor {
    /**
     * Runs a script as root (multiple commands separated by "\n") with a
     * default timeout of 5 seconds.
     * 
     * @param script
     *            the script to be executed
     * @param res
     *            the script output response (stdout + stderr)
     * @param timeout
     *            timeout in milliseconds (-1 for none)
     * @return the script exit code
     * @throws IOException
     *             on any error executing the script, or writing it to disk
     */
    public static int runScriptAsRoot(String script, StringBuilder res)
            throws IOException {
        return runScriptAsRoot(script, res, 15000);
    }

    /**
     * Runs a script as root (multiple commands separated by "/n").
     * 
     * @param script
     *            the script to be executed
     * @param res
     *            the script output response (stdout + stderr)
     * @param timeout
     *            timeout in milliseconds (-1 for none)
     * @return the script exit code
     */
    public static int runScriptAsRoot(String script, StringBuilder res,
            final long timeout) {
        final ScriptRunner runner = new ScriptRunner(script, res);
        runner.start();
        try {
            if (timeout > 0) {
                runner.join(timeout);
            } else {
                runner.join();
            }
            if (runner.isAlive()) {
                // Timed-out
                runner.interrupt();
                runner.destroy();
                runner.join(50);
            }
        } catch (InterruptedException ex) {
        }
        return runner.exitcode;
    }

    /**
     * Check if we have root access
     * 
     * @param ctx
     *            optional context to display alert messages
     * @return boolean true if we have root
     */
    public static boolean hasRootAccess(Context ctx) {
        // if (hasroot) return true;
        try {
            // Run an empty script just to check root access
            if (runScriptAsRoot("exit 0", null, 20000) == 0) {
                // hasroot = true;
                return true;
            }
        } catch (Exception e) {
        }
        /*
         * alert(ctx, "Could not acquire root access./n" +
         * "You need a rooted phone to run Droid Wall./n/n" +
         * "If this phone is already rooted, please make sure Droid Wall has enough permissions to execute the \"su\" command."
         * );
         */
        return false;
    }
}
