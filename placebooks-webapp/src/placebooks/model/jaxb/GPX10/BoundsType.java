//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.11.25 at 03:09:26 PM GMT 
//

package placebooks.model.jaxb.GPX10;

import java.math.BigDecimal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for boundsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="boundsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="minlat" use="required" type="{http://www.topografix.com/GPX/1/0}latitudeType" />
 *       &lt;attribute name="minlon" use="required" type="{http://www.topografix.com/GPX/1/0}longitudeType" />
 *       &lt;attribute name="maxlat" use="required" type="{http://www.topografix.com/GPX/1/0}latitudeType" />
 *       &lt;attribute name="maxlon" use="required" type="{http://www.topografix.com/GPX/1/0}longitudeType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "boundsType")
public class BoundsType
{

	@XmlAttribute(required = true)
	protected BigDecimal minlat;
	@XmlAttribute(required = true)
	protected BigDecimal minlon;
	@XmlAttribute(required = true)
	protected BigDecimal maxlat;
	@XmlAttribute(required = true)
	protected BigDecimal maxlon;

	/**
	 * Gets the value of the maxlat property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getMaxlat()
	{
		return maxlat;
	}

	/**
	 * Gets the value of the maxlon property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getMaxlon()
	{
		return maxlon;
	}

	/**
	 * Gets the value of the minlat property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getMinlat()
	{
		return minlat;
	}

	/**
	 * Gets the value of the minlon property.
	 * 
	 * @return possible object is {@link BigDecimal }
	 * 
	 */
	public BigDecimal getMinlon()
	{
		return minlon;
	}

	/**
	 * Sets the value of the maxlat property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setMaxlat(final BigDecimal value)
	{
		maxlat = value;
	}

	/**
	 * Sets the value of the maxlon property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setMaxlon(final BigDecimal value)
	{
		maxlon = value;
	}

	/**
	 * Sets the value of the minlat property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setMinlat(final BigDecimal value)
	{
		minlat = value;
	}

	/**
	 * Sets the value of the minlon property.
	 * 
	 * @param value
	 *            allowed object is {@link BigDecimal }
	 * 
	 */
	public void setMinlon(final BigDecimal value)
	{
		minlon = value;
	}

}