package com.chukong.sdk.common;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.chukong.sdk.Constants;

/** 
 * Internal thread used to execute scripts as root. 
 */  
public class ScriptRunner extends Thread {
    private final String script;
    private final StringBuilder res;
    public int exitcode = -1;
    private Process exec;

    /**
     * Creates a new script runner.
     * 
     * @param script
     *            script to run
     * @param res
     *            response output
     */
    public ScriptRunner(String script, StringBuilder res) {
        this.script = script;
        this.res = res;
    }

    @Override
    public void run() {
        try {
            // Create the "su" request to run the command
            // note that this will create a shell that we must interact to
            // (using stdin/stdout)
            exec = Runtime.getRuntime().exec("su");
            final OutputStreamWriter out = new OutputStreamWriter(
                    exec.getOutputStream());
            // Write the script to be executed
            Log.d(Log.TAG, "script = " + script);
            out.write(script);
            // Ensure that the last character is an "enter"
            if (!script.endsWith("\n"))
                out.write("\n");
            out.flush();
            // Terminate the "su" process
            String successFlag = "echo " + Constants.IPTABLES_SUCCESS + "\n";
            out.write(successFlag);
            out.flush();
            out.write("exit\n");
            out.flush();
            final char buf[] = new char[1024];
            // Consume the "stdout"
            InputStreamReader r = new InputStreamReader(exec.getInputStream());
            int read = 0;
            while ((read = r.read(buf)) != -1) {
                if (res != null)
                    res.append(buf, 0, read);
            }
            // Consume the "stderr"
            r = new InputStreamReader(exec.getErrorStream());
            read = 0;
            while ((read = r.read(buf)) != -1) {
                if (res != null)
                    res.append(buf, 0, read);
            }
            // get the process exit code
            if (exec != null)
                this.exitcode = exec.waitFor();
            Log.d(Log.TAG, "exitcode = " + exitcode);
        } catch (InterruptedException ex) {
            if (res != null)
                res.append("\nOperation timed-out");
        } catch (Exception ex) {
            if (res != null)
                res.append("\n" + ex);
        } finally {
            destroy();
            //Log.d(Log.TAG, "finally res = " + (res != null ? res.toString() : ""));
        }
    }

    /**
     * Destroy this script runner
     */
    public synchronized void destroy() {
        if (exec != null)
            exec.destroy();
        exec = null;
    }
}
