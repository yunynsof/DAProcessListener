
package com.tigo.josm.gateway.services.order.simpleorderrequest.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import com.tigo.josm.gateway.services.order.orderresponse.v1.OrderResponse;


/**
 * <p>Clase Java para purchaseProductResponse complex type.
 * 
 * <p>El siguiente fragmento de esquema especifica el contenido que se espera que haya en esta clase.
 * 
 * <pre>
 * &lt;complexType name="purchaseProductResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="simpleOrderResponse" type="{http://tigo.com/josm/gateway/services/order/OrderResponse/V1}OrderResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "purchaseProductResponse", propOrder = {
    "simpleOrderResponse"
})
public class PurchaseProductResponse {

    protected OrderResponse simpleOrderResponse;

    /**
     * Obtiene el valor de la propiedad simpleOrderResponse.
     * 
     * @return
     *     possible object is
     *     {@link OrderResponse }
     *     
     */
    public OrderResponse getSimpleOrderResponse() {
        return simpleOrderResponse;
    }

    /**
     * Define el valor de la propiedad simpleOrderResponse.
     * 
     * @param value
     *     allowed object is
     *     {@link OrderResponse }
     *     
     */
    public void setSimpleOrderResponse(OrderResponse value) {
        this.simpleOrderResponse = value;
    }

}
