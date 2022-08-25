
package hn.com.tigo.josm.orchestrator.adapter.cbs2.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import hn.com.tigo.josm.adapter.requesttype.v1.TaskResponseType;


/**
 * <p>Clase Java para executeTaskResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="executeTaskResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://adapter.josm.tigo.com.hn/RequestType/V1}TaskResponseType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "executeTaskResponse", propOrder = {
    "_return"
})
public class ExecuteTaskResponse {

    @XmlElement(name = "return")
    protected TaskResponseType _return;

    /**
     * Obtiene el valor de la propiedad return.
     * 
     * @return
     *     possible object is
     *     {@link TaskResponseType }
     *     
     */
    public TaskResponseType getReturn() {
        return _return;
    }

    /**
     * Define el valor de la propiedad return.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskResponseType }
     *     
     */
    public void setReturn(TaskResponseType value) {
        this._return = value;
    }

}
