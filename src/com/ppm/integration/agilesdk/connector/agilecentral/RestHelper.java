
package com.ppm.integration.agilesdk.connector.agilecentral;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.ClientRuntimeException;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;

public class RestHelper {

    private Resource resource;

    private String endpoint;

    private final Logger logger = Logger.getLogger(this.getClass());

    public RestHelper(String endpoint, Config config) {
        this.endpoint = endpoint;
        RestClient restClient =
                config.getClientConfig() == null ? new RestClient() : new RestClient(config.getClientConfig());
        this.resource =
                restClient.resource("").header("Proxy-Connection", "Keep-Alive")
                        .header("Authorization", config.getBasicAuthorization());
    }

    public JSONObject get(String uri) {
        this.resource.uri(checkURI(uri));
        return get();
    }

    public JSONArray getAll(String uri) {
        return getAll(uri, 20);
    }

    public JSONArray getAll(String uri, int pageSize) {
        return query(uri, "", true, "StartDate", 1, pageSize);
    }

    public JSONArray query(String uri, String query, boolean fetch, String order, int start, int pageSize) {
        int resultCount = 0;
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = doQuery(uri, query, fetch, order, start, pageSize);
        int totalResultCount = jsonObject.getJSONObject("QueryResult").getInt("TotalResultCount");
        JSONArray results = jsonObject.getJSONObject("QueryResult").getJSONArray("Results");
        resultCount += results.size();
        while (!results.isEmpty()) {
            jsonArray.addAll(results);
            if (results.size() < pageSize || resultCount == totalResultCount) {
                break;
            }
            jsonObject = doQuery(uri, query, fetch, order, start = start + pageSize, pageSize);
            results = jsonObject.getJSONObject("QueryResult").getJSONArray("Results");
            resultCount += results.size();
        }
        return jsonArray;
    }

    private JSONObject doQuery(String uri, String query, boolean fetch, String order, int start, int pageSize) {
        this.resource.uri(checkURI(uri)).getUriBuilder().replaceQueryParam("query", query)
                .replaceQueryParam("fetch", fetch).replaceQueryParam("order", order).replaceQueryParam("start", start)
                .replaceQueryParam("pagesize", pageSize);
        return this.get();
    }

    private JSONObject get() {

        try {
            ClientResponse resp = this.resource.get();
            if (resp.getStatusCode() != 200) {
                throw new ClientException(resp.getStatusCode() + "", resp.getMessage());
            }
        } catch (ClientRuntimeException e) {
            logger.error("", e);
            new ConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e);
        } catch (ClientException e) {
            logger.error("", e);
            new ConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e);
        } catch (RuntimeException e) {
            logger.error("", e);
            new ConnectivityExceptionHandler().uncaughtException(Thread.currentThread(), e);
        }

        String json = this.resource.get(String.class);
        return JSONObject.fromObject(json);
    }

    private String checkURI(String s) {
        return (s.contains("https://") || s.contains("http://")) ? s : this.endpoint + s;
    }

}
