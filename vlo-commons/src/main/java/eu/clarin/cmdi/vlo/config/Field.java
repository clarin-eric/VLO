/**
 * 
 */
package eu.clarin.cmdi.vlo.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Field {

   @XmlAttribute
   private String key;
   @XmlValue
   private String value;

   public Field() {

   }

   /**
    * @param key
    * @param value
    */
   public Field(String key, String value) {
      super();
      this.key = key;
      this.value = value;
   }

   /**
    * @return the key
    */
   public String getKey() {
      return key;
   }

   /**
    * @param key
    *           the key to set
    */
   public void setKey(String key) {
      this.key = key;
   }

   /**
    * @return the value
    */
   public String getValue() {
      return value;
   }

   /**
    * @param value
    *           the value to set
    */
   public void setValue(String value) {
      this.value = value;
   }
}
