
package hn.tigo.resources.emailservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the hn.tigo.resources.emailservice package. 
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

    private final static QName _SendMessageResponse_QNAME = new QName("http://tigo.hn/resources/emailService", "sendMessageResponse");
    private final static QName _SendMessage_QNAME = new QName("http://tigo.hn/resources/emailService", "sendMessage");
    private final static QName _SendMessageCC_QNAME = new QName("", "CC");
    private final static QName _SendMessageAttachments_QNAME = new QName("", "attachments");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: hn.tigo.resources.emailservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SendMessage }
     * 
     */
    public SendMessage createSendMessage() {
        return new SendMessage();
    }

    /**
     * Create an instance of {@link SendMessageResponse }
     * 
     */
    public SendMessageResponse createSendMessageResponse() {
        return new SendMessageResponse();
    }

    /**
     * Create an instance of {@link ToDto }
     * 
     */
    public ToDto createToDto() {
        return new ToDto();
    }

    /**
     * Create an instance of {@link AttachmentDto }
     * 
     */
    public AttachmentDto createAttachmentDto() {
        return new AttachmentDto();
    }

    /**
     * Create an instance of {@link AttachmentsDTO }
     * 
     */
    public AttachmentsDTO createAttachmentsDTO() {
        return new AttachmentsDTO();
    }

    /**
     * Create an instance of {@link SentDTO }
     * 
     */
    public SentDTO createSentDTO() {
        return new SentDTO();
    }

    /**
     * Create an instance of {@link GeneralResponse }
     * 
     */
    public GeneralResponse createGeneralResponse() {
        return new GeneralResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendMessageResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.hn/resources/emailService", name = "sendMessageResponse")
    public JAXBElement<SendMessageResponse> createSendMessageResponse(SendMessageResponse value) {
        return new JAXBElement<SendMessageResponse>(_SendMessageResponse_QNAME, SendMessageResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SendMessage }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://tigo.hn/resources/emailService", name = "sendMessage")
    public JAXBElement<SendMessage> createSendMessage(SendMessage value) {
        return new JAXBElement<SendMessage>(_SendMessage_QNAME, SendMessage.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "CC", scope = SendMessage.class)
    public JAXBElement<String> createSendMessageCC(String value) {
        return new JAXBElement<String>(_SendMessageCC_QNAME, String.class, SendMessage.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AttachmentsDTO }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "attachments", scope = SendMessage.class)
    public JAXBElement<AttachmentsDTO> createSendMessageAttachments(AttachmentsDTO value) {
        return new JAXBElement<AttachmentsDTO>(_SendMessageAttachments_QNAME, AttachmentsDTO.class, SendMessage.class, value);
    }

}
