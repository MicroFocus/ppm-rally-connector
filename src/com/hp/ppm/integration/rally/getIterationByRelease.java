package com.hp.ppm.integration.rally;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.Resource;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;

import com.hp.ppm.integration.rally.model.Release;

public class getIterationByRelease {
	public static RestHelper helper;
	public static void main(String[] args) throws ParseException{
//		ClientConfig config = new ClientConfig();
//		BasicAuthSecurityHandler basicAuthHandler = new BasicAuthSecurityHandler("dan.gao3@hpe.com","Hanyan@223");
//		config.handlers(basicAuthHandler);
//		
//		// create the rest client instance
//	    RestClient client = new RestClient(config);
//
//	    String releaseURI = "https://rally1.rallydev.com/slm/webservice/v2.0/release";
//	    Resource resource = client.resource(releaseURI);    
//	    
////	    JSONObject jsonObject = JSONObject.fromObject(resource.get(String.class));
////	    System.out.println(new Release(jsonObject.getJSONObject("Release")).getScheduleStart());
//	    
//	    JSONObject js = JSONObject.fromObject(resource.getClass());
//	    System.out.println(js.getJSONObject("QueryResult").getJSONObject("Results"));
		String a = null;
		System.out.println(a == null);

	}

}
