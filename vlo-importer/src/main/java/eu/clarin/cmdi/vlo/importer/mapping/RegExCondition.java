package eu.clarin.cmdi.vlo.importer.mapping;

import java.util.regex.*;

/**
 * @author @author Wolfgang Walter SAUER (wowasa)
 *         &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class RegExCondition extends AbstractCondition {
    private Pattern pattern;

    public RegExCondition() {

    }

    public void setPattern(String regEx) {
        this.pattern = Pattern.compile(regEx);
    }

    public RegExCondition(String regEx) {
        this.pattern = Pattern.compile(regEx);
    }

    @Override
    public boolean matches(String expression) {
        // TODO Auto-generated method stub
        return pattern.matcher(expression).matches();
    }

}
