/**
 * 
 */
package eu.clarin.cmdi.vlo.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class Fields {
   @XmlElement(name = "field")
   private List<Field> entries = new ArrayList<Field>();

   List<Field> entries() {
      return Collections.unmodifiableList(entries);
   }

   void addEntry(Field entry) {
      entries.add(entry);
   }
}
