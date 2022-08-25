
package hn.com.tigo.josm.orchestrator.adapter.cbs2.task;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the hn.com.tigo.josm.orchestrator.adapter.cbs2.task package. 
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

    private final static QName _ExecuteTaskResponse_QNAME = new QName("http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/", "executeTaskResponse");
    private final static QName _ExecuteTask_QNAME = new QName("http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/", "executeTask");
    private final static QName _AdapterException_QNAME = new QName("http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/", "AdapterException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: hn.com.tigo.josm.orchestrator.adapter.cbs2.task
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExecuteTaskResponse }
     * 
     */
    public ExecuteTaskResponse createExecuteTaskResponse() {
        return new ExecuteTaskResponse();
    }

    /**
     * Create an instance of {@link ExecuteTask }
     * 
     */
    public ExecuteTask createExecuteTask() {
        return new ExecuteTask();
    }

    /**
     * Create an instance of {@link AdapterException }
     * 
     */
    public AdapterException createAdapterException() {
        return new AdapterException();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteTaskResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/", name = "executeTaskResponse")
    public JAXBElement<ExecuteTaskResponse> createExecuteTaskResponse(ExecuteTaskResponse value) {
        return new JAXBElement<ExecuteTaskResponse>(_ExecuteTaskResponse_QNAME, ExecuteTaskResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExecuteTask }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/", name = "executeTask")
    public JAXBElement<ExecuteTask> createExecuteTask(ExecuteTask value) {
        return new JAXBElement<ExecuteTask>(_ExecuteTask_QNAME, ExecuteTask.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AdapterException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/", name = "AdapterException")
    public JAXBElement<AdapterException> createAdapterException(AdapterException value) {
        return new JAXBElement<AdapterException>(_AdapterException_QNAME, AdapterException.class, null, value);
    }

}
