
package hn.com.tigo.da.listener;

import com.newrelic.api.agent.NewRelic;

public class NewRelicImpl {

	public static void addNewRelicMetric(String appId, float time) {
		try {
			NewRelic.recordMetric(appId, time);

		} catch (Exception ex) {

		}
	}

	public static void addNewRelicError(String message) {
		try {
			NewRelic.noticeError(message);

		} catch (Exception ex) {

		}
	}
}
