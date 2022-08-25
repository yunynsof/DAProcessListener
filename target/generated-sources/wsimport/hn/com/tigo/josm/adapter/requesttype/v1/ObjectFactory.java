
package hn.com.tigo.josm.adapter.requesttype.v1;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the hn.com.tigo.josm.adapter.requesttype.v1 package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: hn.com.tigo.josm.adapter.requesttype.v1
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link TaskResponseType }
     * 
     */
    public TaskResponseType createTaskResponseType() {
        return new TaskResponseType();
    }

    /**
     * Create an instance of {@link TaskRequestType }
     * 
     */
    public TaskRequestType createTaskRequestType() {
        return new TaskRequestType();
    }

}
