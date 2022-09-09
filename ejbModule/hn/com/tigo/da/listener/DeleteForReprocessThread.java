package hn.com.tigo.da.listener;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import hn.com.tigo.core.da.dto.DABankProcessDTO;
import hn.com.tigo.core.da.manager.DAManager;
import hn.com.tigo.core.invoice.utils.States;
import hn.com.tigo.da.listener.util.DAListenerConstants;
import hn.com.tigo.josm.persistence.core.ServiceSessionEJB;
import hn.com.tigo.josm.persistence.core.ServiceSessionEJBLocal;
import hn.com.tigo.josm.persistence.exception.PersistenceException;

/**
 * DeleteForReprocessThread.
 *
 * @author Yuny Rene Rodriguez Perez {@literal<mailto: yrodriguez@hightech-corp.com />}
 * @version  1.0.0
 * @since 08-30-2022 11:21:13 AM 2022
 */
public class DeleteForReprocessThread extends Thread {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LogManager.getLogger(DeleteForReprocessThread.class);

	/** The executor service. */
	private ThreadPoolExecutor executorService;

	/** The working queue. */
	private BlockingQueue<Runnable> workingQueue;

	/** The state. */
	private States state;

	/** The config params. */
	private HashMap<String, String> configParams;

	/**
	 * Instantiates a new delete for reprocess thread.
	 */
	public DeleteForReprocessThread() {
		try {
			initialize();
		} catch (Exception e) {
			state = States.SHUTTINGDOWN;
			LOGGER.error("Unable to initialize : " + e.getMessage(), e);
		}
	}

	/**
	 * Initialize.
	 */
	public void initialize() {
		workingQueue = new ArrayBlockingQueue<Runnable>(100);
		LOGGER.info("workingQueue correctly");
		executorService = new ThreadPoolExecutor(1, 2, 1, TimeUnit.MILLISECONDS, workingQueue);
		state = States.STARTED;
		LOGGER.info("Iinitialize Finalized.");
	}

	/**
	 * Shutdown.
	 */
	public void shutdown() {
		state = States.SHUTTINGDOWN;
		executorService.shutdownNow();
	}

	/**
	 * Run.
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void run() {

		while (state == States.STARTED) {
			DAManager manager = null;
			String BPiD = "";

			try {
				ServiceSessionEJBLocal<DAManager> serviceSession = ServiceSessionEJB.getInstance();
				manager = (DAManager) serviceSession.getSessionDataSource(DAManager.class, DAListenerConstants.JNDI);
				configParams = manager.listAllParam();
				final List<DABankProcessDTO> listDeleteBP = manager
						.selectDABankProcess(DAListenerConstants.STATUS_DEL_REPRO);

				if (listDeleteBP.size() > 0) {
					for (int a = 0; a < listDeleteBP.size(); a++) {

						manager.deleteLogs(listDeleteBP.get(a).getId());
						manager.deleteGroupP(listDeleteBP.get(a).getId());
						manager.deleteGroupAcc(listDeleteBP.get(a).getId());
						manager.deleteBPPayDetail(listDeleteBP.get(a).getId());

						manager.updateStatusBankProcess(DAListenerConstants.STATUS_BPDP,
								DAListenerConstants.STATUS_DEL_REPRO, listDeleteBP.get(a).getId());
					}
				}

			} catch (PersistenceException error) {
				error.printStackTrace();
				NewRelicImpl.addNewRelicError(error.getMessage());

				LOGGER.error("DeleteForReprocessThread " + this.getClass().getName() + error.getMessage(), error);
				String uuid = UUID.randomUUID().toString();
				if (manager != null) {
					try {
						manager.updateStatusBankProcess(-4, 3, BPiD);

						manager.insertLogs(uuid, DAListenerConstants.TYPE_ERROR1,
								"Error en proceso DeleteForReprocessThread, se presenta el siguiente error: "
										+ error.getMessage(),
								BPiD, "");
					} catch (Exception e) {
						LOGGER.error("Error de proceso DeleteForReprocessThread: " + e.getMessage());
						e.printStackTrace();
					}
				}
			} finally {

				if (manager != null) {
					try {
						manager.close();
					} catch (Exception e) {
						LOGGER.error("DeleteForReprocessThread " + e.getMessage(), e);
					}
				}
				this.sleepThread(Integer.parseInt(configParams.get(DAListenerConstants.SLEEP_THREAD)));
			}
		}
		executorService.shutdown();

	}

	/**
	 * Sleep thread.
	 *
	 * @param milliSecounds the milli secounds
	 */
	private void sleepThread(final int milliSecounds) {
		try {
			Thread.sleep(milliSecounds);
		} catch (InterruptedException e) {
			LOGGER.error("DeleteForReprocessThread " + e.getMessage(), e);
		}
	}

}
