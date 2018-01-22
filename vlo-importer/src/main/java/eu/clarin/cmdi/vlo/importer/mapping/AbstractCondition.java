package eu.clarin.cmdi.vlo.importer.mapping;

/**
 * @author @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public abstract class AbstractCondition {
	public abstract boolean match(String expression);
}
