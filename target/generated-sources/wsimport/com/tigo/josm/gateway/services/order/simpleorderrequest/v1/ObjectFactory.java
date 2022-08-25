
package com.tigo.josm.gateway.services.order.simpleorderrequest.v1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.tigo.josm.gateway.services.order.simpleorderrequest.v1 package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _LoanProductResponse_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "loanProductResponse");
    private final static QName _LoanProduct_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "loanProduct");
    private final static QName _TransferProductResponse_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "transferProductResponse");
    private final static QName _SellProduct_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "sellProduct");
    private final static QName _DeactivateProductResponse_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "deactivateProductResponse");
    private final static QName _PurchaseProduct_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "purchaseProduct");
    private final static QName _TransferProduct_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "transferProduct");
    private final static QName _DeactivateProduct_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "deactivateProduct");
    private final static QName _ActivateProduct_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "activateProduct");
    private final static QName _ActivateProductResponse_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "activateProductResponse");
    private final static QName _PurchaseProductResponse_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "purchaseProductResponse");
    private final static QName _SellProductResponse_QNAME = new QName("http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", "sellProductResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.tigo.josm.gateway.services.order.simpleorderrequest.v1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeactivateProductResponse }
     * 
     */
    public DeactivateProductResponse createDeactivateProductResponse() {
        return new DeactivateProductResponse();
    }

    /**
     * Create an instance of {@link PurchaseProduct }
     * 
     */
    public PurchaseProduct createPurchaseProduct() {
        return new PurchaseProduct();
    }

    /**
     * Create an instance of {@link TransferProduct }
     * 
     */
    public TransferProduct createTransferProduct() {
        return new TransferProduct();
    }

    /**
     * Create an instance of {@link DeactivateProduct }
     * 
     */
    public DeactivateProduct createDeactivateProduct() {
        return new DeactivateProduct();
    }

    /**
     * Create an instance of {@link LoanProductResponse }
     * 
     */
    public LoanProductResponse createLoanProductResponse() {
        return new LoanProductResponse();
    }

    /**
     * Create an instance of {@link LoanProduct }
     * 
     */
    public LoanProduct createLoanProduct() {
        return new LoanProduct();
    }

    /**
     * Create an instance of {@link TransferProductResponse }
     * 
     */
    public TransferProductResponse createTransferProductResponse() {
        return new TransferProductResponse();
    }

    /**
     * Create an instance of {@link SellProduct }
     * 
     */
    public SellProduct createSellProduct() {
        return new SellProduct();
    }

    /**
     * Create an instance of {@link PurchaseProductResponse }
     * 
     */
    public PurchaseProductResponse createPurchaseProductResponse() {
        return new PurchaseProductResponse();
    }

    /**
     * Create an instance of {@link SellProductResponse }
     * 
     */
    public SellProductResponse createSellProductResponse() {
        return new SellProductResponse();
    }

    /**
     * Create an instance of {@link ActivateProduct }
     * 
     */
    public ActivateProduct createActivateProduct() {
        return new ActivateProduct();
    }

    /**
     * Create an instance of {@link ActivateProductResponse }
     * 
     */
    public ActivateProductResponse createActivateProductResponse() {
        return new ActivateProductResponse();
    }

    /**
     * Create an instance of {@link SimpleOrderRequest }
     * 
     */
    public SimpleOrderRequest createSimpleOrderRequest() {
        return new SimpleOrderRequest();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoanProductResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "loanProductResponse")
    public JAXBElement<LoanProductResponse> createLoanProductResponse(LoanProductResponse value) {
        return new JAXBElement<LoanProductResponse>(_LoanProductResponse_QNAME, LoanProductResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link LoanProduct }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "loanProduct")
    public JAXBElement<LoanProduct> createLoanProduct(LoanProduct value) {
        return new JAXBElement<LoanProduct>(_LoanProduct_QNAME, LoanProduct.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TransferProductResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "transferProductResponse")
    public JAXBElement<TransferProductResponse> createTransferProductResponse(TransferProductResponse value) {
        return new JAXBElement<TransferProductResponse>(_TransferProductResponse_QNAME, TransferProductResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SellProduct }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "sellProduct")
    public JAXBElement<SellProduct> createSellProduct(SellProduct value) {
        return new JAXBElement<SellProduct>(_SellProduct_QNAME, SellProduct.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeactivateProductResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "deactivateProductResponse")
    public JAXBElement<DeactivateProductResponse> createDeactivateProductResponse(DeactivateProductResponse value) {
        return new JAXBElement<DeactivateProductResponse>(_DeactivateProductResponse_QNAME, DeactivateProductResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PurchaseProduct }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "purchaseProduct")
    public JAXBElement<PurchaseProduct> createPurchaseProduct(PurchaseProduct value) {
        return new JAXBElement<PurchaseProduct>(_PurchaseProduct_QNAME, PurchaseProduct.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link TransferProduct }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "transferProduct")
    public JAXBElement<TransferProduct> createTransferProduct(TransferProduct value) {
        return new JAXBElement<TransferProduct>(_TransferProduct_QNAME, TransferProduct.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeactivateProduct }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "deactivateProduct")
    public JAXBElement<DeactivateProduct> createDeactivateProduct(DeactivateProduct value) {
        return new JAXBElement<DeactivateProduct>(_DeactivateProduct_QNAME, DeactivateProduct.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivateProduct }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "activateProduct")
    public JAXBElement<ActivateProduct> createActivateProduct(ActivateProduct value) {
        return new JAXBElement<ActivateProduct>(_ActivateProduct_QNAME, ActivateProduct.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivateProductResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "activateProductResponse")
    public JAXBElement<ActivateProductResponse> createActivateProductResponse(ActivateProductResponse value) {
        return new JAXBElement<ActivateProductResponse>(_ActivateProductResponse_QNAME, ActivateProductResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PurchaseProductResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "purchaseProductResponse")
    public JAXBElement<PurchaseProductResponse> createPurchaseProductResponse(PurchaseProductResponse value) {
        return new JAXBElement<PurchaseProductResponse>(_PurchaseProductResponse_QNAME, PurchaseProductResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SellProductResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.com/josm/gateway/services/order/SimpleOrderRequest/V1", name = "sellProductResponse")
    public JAXBElement<SellProductResponse> createSellProductResponse(SellProductResponse value) {
        return new JAXBElement<SellProductResponse>(_SellProductResponse_QNAME, SellProductResponse.class, null, value);
    }

}
