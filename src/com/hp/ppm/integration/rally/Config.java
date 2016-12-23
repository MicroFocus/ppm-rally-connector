package com.hp.ppm.integration.rally;

import org.apache.commons.codec.binary.Base64;
import org.apache.wink.client.ClientConfig;

public class Config {

	private ClientConfig clientConfig;
	private String basicAuthorization;

	public void setProxy(String proxyHost, String proxyPort) {
		clientConfig = new ClientConfig();
		if (proxyHost != null && !proxyHost.isEmpty()
				&& proxyHost != null && !proxyHost.isEmpty()) {
			clientConfig.proxyHost(proxyHost);
			clientConfig.proxyPort(Integer.parseInt(proxyPort));
		}
	}

	public void setBasicAuthorization(String userName, String password) {
		basicAuthorization = "Basic "
				+ new String(Base64.encodeBase64((userName + ":" + password)
						.getBytes()));
	}

	public ClientConfig getClientConfig() {
		return clientConfig;
	}

	public String getBasicAuthorization() {
		return basicAuthorization;
	}
}
