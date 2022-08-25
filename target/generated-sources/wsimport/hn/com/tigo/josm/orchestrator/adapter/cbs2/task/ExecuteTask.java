
package hn.com.tigo.josm.orchestrator.adapter.cbs2.task;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import hn.com.tigo.josm.adapter.requesttype.v1.TaskRequestType;


/**
 * <p>Clase Java para executeTask complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="executeTask">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="arg0" type="{http://adapter.josm.tigo.com.hn/RequestType/V1}TaskRequestType" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "executeTask", propOrder = {
    "arg0"
})
public class ExecuteTask {

    protected TaskRequestType arg0;

    /**
     * Obtiene el valor de la propiedad arg0.
     * 
     * @return
     *     possible object is
     *     {@link TaskRequestType }
     *     
     */
    public TaskRequestType getArg0() {
        return arg0;
    }

    /**
     * Define el valor de la propiedad arg0.
     * 
     * @param value
     *     allowed object is
     *     {@link TaskRequestType }
     *     
     */
    public void setArg0(TaskRequestType value) {
        this.arg0 = value;
    }

}
