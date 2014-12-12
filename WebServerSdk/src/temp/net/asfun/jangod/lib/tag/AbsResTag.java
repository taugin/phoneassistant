package net.asfun.jangod.lib.tag;

import net.asfun.jangod.interpret.InterpretException;
import net.asfun.jangod.interpret.JangodInterpreter;
import net.asfun.jangod.lib.Tag;
import net.asfun.jangod.tree.NodeList;
import net.asfun.jangod.util.HelperStringTokenizer;

import com.chukong.sdk.GlobalInit;

/**
 * {% resTag 'strName' %}
 * {% resTag var_strName %}
 * @author join
 */
public abstract class AbsResTag implements Tag {

    protected GlobalInit app = GlobalInit.getInstance();
    protected String pkgName = app.getBaseContext().getPackageName();

    @Override
    public String interpreter(NodeList carries, String helpers, JangodInterpreter interpreter)
            throws InterpretException {
        String[] helper = new HelperStringTokenizer(helpers).allTokens();
        if (helper.length != 1) {
            throw new InterpretException("Tag '" + getName() + "' expects 1 helper >>> "
                    + helper.length);
        }
        String strName = interpreter.resolveString(helper[0]);
        return getValue(strName);
    }

    public abstract String getValue(String name);

    protected int getIdentifier(String name, String defType) {
        return app.getBaseContext().getResources().getIdentifier(name, defType, pkgName);
    }

}
