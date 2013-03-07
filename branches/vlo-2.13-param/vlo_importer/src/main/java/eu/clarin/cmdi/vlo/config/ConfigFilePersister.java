
package eu.clarin.cmdi.vlo.config;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import org.simpleframework.xml.core.Persister;

/**
 * Mapping of XML definitions to class members<br><br>
 *
 * A ConfigFilePersister class object can, by means of the Simple framework,
 * interpret definitions in an XML file and assign the values defined to members
 * in a class that have been annotated according to the Simple framework
 * specifications. Next to reading, a persister object can also turn annotated
 * members and their values into an XML definition and write it to a
 * file.<br><br>
 *
 * Annotating a class amounts to prepending annotations to members in it. When
 * the framework applies an XML file containing the element<br><br>
 *
 * {@literal <text>this is an example</text>}<br><br>
 *
 * to a class containing<br><br>
 *
 * {@literal @Element}<br> 
 * {@literal String text;}<br><br>
 *
 * the value of this member will be equal to<br><br>
 *
 * {@literal 'this is an example'}<br><br>
 *
 * Apart from elements, the Simple framework can also generate attributes and
 * lists. For more examples, please refer to the Simple web site.<br><br>
 * 
 * Checklist<br><br>
 * 
 * A ConfigFilePersister object can successfully process a message sent
 * to it successfully, only if it's logger member has been initialized. In other 
 * words: before using a persister object, the interface defined here should
 * be implemented and passed to the object.
 * 
 * @author keeloo
 */
public class ConfigFilePersister {

    /**
     * Interface to a logger object<br><br>
     *
     * Definition of what the ConfigFilePersister class expects from a method
     * that takes care of logging. All messages that will be send to a logger
     * object are rated as severe.
     */
    public interface Logger {

        /**
         * @param object message to be logged
         */
        public void log(Object object);
    }
    
    /**
     * The type of the annotated object
     */
    private Class configClass;
    
    /**
     * The annotated object
     */
    private Object configObject;
    
    /**
     * The absolute name of the XML file defining the members of the annotated 
     * class.
     */
    private String fileName;
    
    /**
     * Interface object taking care of logging<br><br>
     *
     * Before sending a ConfigFromFile or ConfigToFile message to a
     * ConfigFilePersister object, initialize the logger member by sending the
     * object a setLogger message first.
     */
    private static Logger logger;
    
    /**
     * Interface object initialization
     * 
     * @param someLogger 
     */
    public static void setLogger (Logger someLogger){
        logger = someLogger;
    }
    
    private Persister persister;

    /**
     * Constructor method
     *
     * @param object an object whose annotated members will be initialized from
     * or written to definitions in the XML file. On reading, an object may be
     * equal to null.<br>
     *
     * @param name the name of the XML file which an annotated object is read
     * from or written to.<br>
     *
     */
    public ConfigFilePersister(Object object, String name) {
        // associate the name of the file and the object with the persister
        fileName = name;
        configObject = object;
        
        // create the Simple framework object that ensures persistance
        persister = new Persister();

        // remember the class to which the object belongs 
        configClass = object.getClass();
    }

    /**
     * Read definitions from an XML file<br><br>
     *
     * Assign the values defined in the elements of the XML file to the
     * annotated members of the object.<br><br>
     *
     * If the file cannot be opened or the annotation of the class does not
     * conform to the specifications of the Simple framework, an exception will
     * be raised, and an error will be logged.<br><br>
     *
     * @return the object if the file conforms to the specification, null
     * otherwise
     */
    public Object ConfigFromFile() {
        
        Object object = null;

        // try to resolve the absolute name of configuration file to a stream
        InputStream sourceAsStream;
        sourceAsStream = ConfigFilePersister.class.getResourceAsStream(fileName);

        if (sourceAsStream == null) {
            
            // the resource cannot be found inside the package, try outside
            File sourceAsFile; 
            
            sourceAsFile = new File(fileName);
            try {
                object = persister.read(configClass, sourceAsFile, true);
            } catch (Exception e) {
                logger.log(e);
            }
            
        } else {
            // the resource can be found in eu.clarin.cmdi.vlo.config
            try {
                object = persister.read(configClass, sourceAsStream, true);
            } catch (Exception e) {
                logger.log(e);
            }
        }

        return object;
    }

    /**
     * Write definitions to an XML file<br><br>
     *
     * Write the values of the annotated members of the object passed to the
     * constructor to an XML file.<br><br>
     *
     * If the file cannot be created or the annotation of the class does not
     * conform to the specifications of the Simple framework, an exception will
     * be raised, and an error will be logged.
     */
    public void ConfigToFile() {

        File configTarget;
        configTarget = new File(fileName);

        try {
            persister.write(configObject, configTarget);
        } catch (Exception e) {
            logger.log(e);
        }
    }
}
