
package hn.com.tigo.josm.orchestrator.adapter.cbs2.task;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;
import hn.com.tigo.josm.adapter.requesttype.v1.TaskRequestType;
import hn.com.tigo.josm.adapter.requesttype.v1.TaskResponseType;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.8
 * Generated source version: 2.2
 * 
 */
@WebService(name = "CBSQueryInvoiceEnhancedTask", targetNamespace = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/")
@XmlSeeAlso({
    com.tigo.enterprise.resources.parameters.simple.v1.schema.ObjectFactory.class,
    hn.com.tigo.josm.adapter.requesttype.v1.ObjectFactory.class,
    hn.com.tigo.josm.orchestrator.adapter.cbs2.task.ObjectFactory.class
})
public interface CBSQueryInvoiceEnhancedTask {


    /**
     * 
     * @param arg0
     * @return
     *     returns hn.com.tigo.josm.adapter.requesttype.v1.TaskResponseType
     * @throws AdapterException_Exception
     */
    @WebMethod
    @WebResult(targetNamespace = "")
    @RequestWrapper(localName = "executeTask", targetNamespace = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/", className = "hn.com.tigo.josm.orchestrator.adapter.cbs2.task.ExecuteTask")
    @ResponseWrapper(localName = "executeTaskResponse", targetNamespace = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/", className = "hn.com.tigo.josm.orchestrator.adapter.cbs2.task.ExecuteTaskResponse")
    @Action(input = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/CBSQueryInvoiceEnhancedTask/executeTaskRequest", output = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/CBSQueryInvoiceEnhancedTask/executeTaskResponse", fault = {
        @FaultAction(className = AdapterException_Exception.class, value = "http://task.cbs2.adapter.orchestrator.josm.tigo.com.hn/CBSQueryInvoiceEnhancedTask/executeTask/Fault/AdapterException")
    })
    public TaskResponseType executeTask(
        @WebParam(name = "arg0", targetNamespace = "")
        TaskRequestType arg0)
        throws AdapterException_Exception
    ;

}
