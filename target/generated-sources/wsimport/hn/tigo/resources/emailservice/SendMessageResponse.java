
package hn.tigo.resources.emailservice;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para sendMessageResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="sendMessageResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="generalResponseMsg" type="{http://tigo.hn/resources/emailService}generalResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sendMessageResponse", propOrder = {
    "generalResponseMsg"
})
public class SendMessageResponse {

    protected GeneralResponse generalResponseMsg;

    /**
     * Obtiene el valor de la propiedad generalResponseMsg.
     * 
     * @return
     *     possible object is
     *     {@link GeneralResponse }
     *     
     */
    public GeneralResponse getGeneralResponseMsg() {
        return generalResponseMsg;
    }

    /**
     * Define el valor de la propiedad generalResponseMsg.
     * 
     * @param value
     *     allowed object is
     *     {@link GeneralResponse }
     *     
     */
    public void setGeneralResponseMsg(GeneralResponse value) {
        this.generalResponseMsg = value;
    }

}
