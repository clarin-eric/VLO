/**
 * 
 */
package eu.clarin.cmdi.vlo.config;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
public class XmlFieldAdapter extends XmlAdapter<Fields, Map<String, String>> {

   /*
    * (non-Javadoc)
    * 
    * @see
    * javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
    */
   @Override
   public Fields marshal(Map<String, String> map) throws Exception {
      Fields fields = new Fields();
      if (map != null) {
         for (Map.Entry<String, String> entry : map.entrySet()) {
            fields.addEntry(new Field(entry.getKey(), entry.getValue()));
         }
      }
      return fields;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
    */
   @Override
   public Map<String, String> unmarshal(Fields fields) throws Exception {
      LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
      if (fields != null) {
         for (Field field : fields.entries()) {
            hashMap.put(field.getKey(), field.getValue());
         }
      }
      return hashMap;
   }

}
