
package hn.com.tigo.da.listener;

import com.newrelic.api.agent.NewRelic;

/**
 * NewRelicImpl.
 *
 * @author Yuny Rene Rodriguez Perez {@literal<mailto: yrodriguez@hightech-corp.com />}
 * @version  1.0.0
 * @since 08-30-2022 11:21:30 AM 2022
 */
public class NewRelicImpl {

	/**
	 * Adds the new relic metric.
	 *
	 * @param appId the app id
	 * @param time the time
	 */
	public static void addNewRelicMetric(String appId, float time) {
		try {
			NewRelic.recordMetric(appId, time);

		} catch (Exception ex) {

		}
	}

	/**
	 * Adds the new relic error.
	 *
	 * @param message the message
	 */
	public static void addNewRelicError(String message) {
		try {
			NewRelic.noticeError(message);

		} catch (Exception ex) {

		}
	}
}
