//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.11.25 at 03:09:27 PM GMT 
//

package placebooks.model.jaxb.GPX11;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 * A link to an external resource (Web page, digital photo, video clip, etc) with additional
 * information.
 * 
 * 
 * <p>
 * Java class for linkType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="linkType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="type" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="href" use="required" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "linkType", propOrder = { "text", "type" })
public class LinkType
{

	protected String text;
	protected String type;
	@XmlAttribute(required = true)
	@XmlSchemaType(name = "anyURI")
	protected String href;

	/**
	 * Gets the value of the href property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getHref()
	{
		return href;
	}

	/**
	 * Gets the value of the text property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * Gets the value of the type property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getType()
	{
		return type;
	}

	/**
	 * Sets the value of the href property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setHref(final String value)
	{
		href = value;
	}

	/**
	 * Sets the value of the text property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setText(final String value)
	{
		text = value;
	}

	/**
	 * Sets the value of the type property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setType(final String value)
	{
		type = value;
	}

}