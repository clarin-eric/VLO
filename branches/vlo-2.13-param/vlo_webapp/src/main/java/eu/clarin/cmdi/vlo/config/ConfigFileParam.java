
package eu.clarin.cmdi.vlo.config;

/**
 * Parameter configuration<br><br>
 *
 * Add parameters to an application by extending this class. The read method
 * assigns values to the new members by reading an XML definition from a file.
 *
 * Configuration is the process of reading the definitions and assigning values.
 * A configuration, the set of members extending this class, is the result of
 * this process.<br><br>
 *
 * Next to reading parameter values from a file, by means of the write method,
 * a ConfigFileParam class object can also reflect the assigned values back to
 * an XML file.<br><br>
 *
 * Note: {@literal maven} can also control parameters. This type of control
 * applies to the source level, and in that sense not visible to the parameter
 * mechanism defined here.<br><br>
 *
 * Checklist.<br><br>
 *
 * An extending class needs to implement the getFileName method.
 *
 * @author keeloo
 */
public abstract class ConfigFileParam {

    /**
     * Empty constructor
     */
    public ConfigFileParam() {
    }

    /**
     * Constraint on a deriving class<br><br>
     *
     * Ask a deriving class to implement a method that returns the name of the
     * file the persister object can read from or write to.<br><br>
     *
     * @return the name of an XML file
     */
    public abstract String getFileName();

    /**
     * Configure by reading from an XML file
     *
     * @param config the object whose annotated members will be assigned a value
     * in accordance with the definition read from the XML file.
     *
     * @return the object with values assigned to annotated members
     */
    public static synchronized ConfigFileParam read(ConfigFileParam config) {

        ConfigFilePersister persister;
        // config itself might not reference a file name
        persister = new ConfigFilePersister(config, config.getFileName ());

        // assign the members their values
        config = (ConfigFileParam) persister.ConfigFromFile();
        return config;
    }
            
    /**
     * Write a assigned values to an XML file.
     *
     * @param config the object whose annotated members and values will be
     * written to a file in the form of an XML definition.
     */
     public static void write(ConfigFileParam config) {
        
        ConfigFilePersister persister;
        // config itself might not reference a file name
        persister = new ConfigFilePersister(config, config.getFileName ());

        // create the definition
        persister.ConfigToFile();
     }     
}
