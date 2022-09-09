package hn.com.tigo.da.listener;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * DAProcessDaemon.
 *
 * @author Yuny Rene Rodriguez Perez {@literal<mailto: yrodriguez@hightech-corp.com />}
 * @version  1.0.0
 * @since 08-30-2022 11:21:02 AM 2022
 */
@Singleton
@Startup
public class DAProcessDaemon {

	/** Attribute that determine a Constant of LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(DAProcessDaemon.class);

	/** Attribute that determine thread. */
	private Thread thread = null;

	/** Attribute that determine thread2. */
	private Thread thread2 = null;

	/** Attribute that determine thread3. */
	private Thread thread3 = null;

	/** Attribute that determine thread4. */
	private Thread thread4 = null;
	
	/** Attribute that determine thread5. */
	private Thread thread5 = null;

	/** Attribute that determine runnable. */
	GenerateFileBankThread runnable = null;

	/** Attribute that determine runnable2. */
	ApplyPayBankThread runnable2 = null;

	/** Attribute that determine runnable3. */
	RevertPayBankThread runnable3 = null;

	/** Attribute that determine runnable4. */
	DeleteMPBankThread runnable4 = null;
	
	/** Attribute that determine runnable5. */
	DeleteForReprocessThread runnable5 = null;

	/**
	 * Initialize.
	 */
	@PostConstruct
	public void initialize() {
		try {
			runnable = new GenerateFileBankThread();
			thread = new Thread(runnable);
			thread.setName("GenerateFileBankThread_executor");
			thread.start();
			LOGGER.info("GenerateFileBankThread " + thread.hashCode() + " has been started");

			runnable2 = new ApplyPayBankThread();
			thread2 = new Thread(runnable2);
			thread2.setName("ApplyPayBankThread_executor");
			thread2.start();
			LOGGER.info("ApplyPayBankThread " + thread2.hashCode() + " has been started");

			runnable3 = new RevertPayBankThread();
			thread3 = new Thread(runnable3);
			thread3.setName("RevertPayBankThread_executor");
			thread3.start();
			LOGGER.info("RevertPayBankThread " + thread3.hashCode() + " has been started");

			runnable4 = new DeleteMPBankThread();
			thread4 = new Thread(runnable4);
			thread4.setName("DeleteMPBankThread_executor");
			thread4.start();
			LOGGER.info("DeleteMPBankThread " + thread4.hashCode() + " has been started");
			
			runnable5 = new DeleteForReprocessThread();
			thread5 = new Thread(runnable5);
			thread5.setName("DeleteForReprocessThread_executor");
			thread5.start();
			LOGGER.info("DeleteForReprocessThread " + thread5.hashCode() + " has been started");

		} catch (TransformerFactoryConfigurationError e) {
			LOGGER.info("Error al instanciar los Hilos: " + e.getMessage());
		}
	}

	/**
	 * Terminate.
	 */
	@PreDestroy
	public void terminate() {
		try {
			this.runnable.shutdown();
			this.thread.interrupt();

			this.runnable2.shutdown();
			this.thread2.interrupt();

			this.runnable3.shutdown();
			this.thread3.interrupt();

			this.runnable4.shutdown();
			this.thread4.interrupt();
			
			this.runnable5.shutdown();
			this.thread5.interrupt();

		} catch (Exception e) {
			LOGGER.info("Error al cerrar los Hilos: " + e.getMessage());
		}
	}

	/**
	 * Gets the generate file bank thread.
	 *
	 * @return the generate file bank thread
	 */
	public Thread getGenerateFileBankThread() {
		LOGGER.info("GenerateFileBankThread " + thread.hashCode());
		return thread;
	}

	/**
	 * Gets the apply pay bank thread.
	 *
	 * @return the apply pay bank thread
	 */
	public Thread getApplyPayBankThread() {
		LOGGER.info("ApplyPayBankThread " + thread2.hashCode());
		return thread2;
	}

	/**
	 * Gets the revert pay bank thread.
	 *
	 * @return the revert pay bank thread
	 */
	public Thread getRevertPayBankThread() {
		LOGGER.info("RevertPayBankThread " + thread3.hashCode());
		return thread3;
	}

	/**
	 * Gets the delete MP bank thread.
	 *
	 * @return the delete MP bank thread
	 */
	public Thread getDeleteMPBankThread() {
		LOGGER.info("DeleteMPBankThread " + thread4.hashCode());
		return thread4;
	}
	
	/**
	 * Gets the delete for reprocess thread.
	 *
	 * @return the delete for reprocess thread
	 */
	public Thread getDeleteForReprocessThread() {
		LOGGER.info("DeleteForReprocessThread " + thread5.hashCode());
		return thread5;
	}

}
