package eu.clarin.cmdi.vlo.pojo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mapping")
public class Mapping{
			
	@XmlElement(name = "normalizedValue")
	private NormalizedValue normalizedValue;
	
	@XmlElement(name = "variant")
	private List<Variant> variants = new ArrayList<Variant>();

	public NormalizedValue getNormalizedVlalue() {
		return normalizedValue;
	}

	public void setNormalizedVlalue(NormalizedValue normalizedVlalue) {
		this.normalizedValue = normalizedVlalue;
	}

	public List<Variant> getVariants() {
		return variants;
	}

	public void setVariants(List<Variant> variants) {
		this.variants = variants;
	}

	public String getValue() {
		return getNormalizedVlalue().getValue();
	}
	
	public void setValue(String value) {
		if(getNormalizedVlalue() == null)
			setNormalizedVlalue(new NormalizedValue());
		getNormalizedVlalue().setValue(value);
	}
	
	@Override
	public String toString() {
		return String.format("%s variants[%s]", normalizedValue, variants);
	}
	
	
}