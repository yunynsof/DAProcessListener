
package com.tigo.josm.gateway.services.order.simpleorderrequest.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Clase Java para sellProduct complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="sellProduct">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="simpleOrderRequest" type="{http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1}SimpleOrderRequest" minOccurs="0" form="qualified"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sellProduct", propOrder = {
    "simpleOrderRequest"
})
public class SellProduct {

    @XmlElement(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1")
    protected SimpleOrderRequest simpleOrderRequest;

    /**
     * Obtiene el valor de la propiedad simpleOrderRequest.
     * 
     * @return
     *     possible object is
     *     {@link SimpleOrderRequest }
     *     
     */
    public SimpleOrderRequest getSimpleOrderRequest() {
        return simpleOrderRequest;
    }

    /**
     * Define el valor de la propiedad simpleOrderRequest.
     * 
     * @param value
     *     allowed object is
     *     {@link SimpleOrderRequest }
     *     
     */
    public void setSimpleOrderRequest(SimpleOrderRequest value) {
        this.simpleOrderRequest = value;
    }

}
