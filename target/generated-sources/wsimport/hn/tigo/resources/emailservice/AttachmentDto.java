
package hn.tigo.resources.emailservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para attachmentDto complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="attachmentDto">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="attachContent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="attachName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="mimeType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="encodingType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "attachmentDto", propOrder = {
    "attachContent",
    "attachName",
    "mimeType",
    "encodingType"
})
public class AttachmentDto {

    @XmlElement(required = true)
    protected String attachContent;
    @XmlElement(required = true)
    protected String attachName;
    @XmlElement(required = true)
    protected String mimeType;
    protected String encodingType;

    /**
     * Obtiene el valor de la propiedad attachContent.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachContent() {
        return attachContent;
    }

    /**
     * Define el valor de la propiedad attachContent.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachContent(String value) {
        this.attachContent = value;
    }

    /**
     * Obtiene el valor de la propiedad attachName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAttachName() {
        return attachName;
    }

    /**
     * Define el valor de la propiedad attachName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAttachName(String value) {
        this.attachName = value;
    }

    /**
     * Obtiene el valor de la propiedad mimeType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Define el valor de la propiedad mimeType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMimeType(String value) {
        this.mimeType = value;
    }

    /**
     * Obtiene el valor de la propiedad encodingType.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncodingType() {
        return encodingType;
    }

    /**
     * Define el valor de la propiedad encodingType.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncodingType(String value) {
        this.encodingType = value;
    }

}
