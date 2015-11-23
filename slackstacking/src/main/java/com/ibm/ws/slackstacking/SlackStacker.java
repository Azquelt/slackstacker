package com.ibm.ws.slackstacking;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.transport.common.gzip.GZIPFeature;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class SlackStacker {

	public static void main(String[] args) {
		Client client = ClientBuilder.newBuilder().register(JacksonJsonProvider.class).register(GZIPFeature.class).build();
		
		//WebTarget target = client.target("http://api.stackexchange.com/2.2");
		WebTarget target = client.target("http://localhost:8080/2.2");
		WebTarget questionTarget = target.path("questions").queryParam("order", "desc").queryParam("site", "stackoverflow");
		
		Invocation.Builder builder = questionTarget.request();
		builder.accept(MediaType.APPLICATION_JSON);
		builder.acceptEncoding("UTF-8");
		
		Response response = builder.get();
		
		if (response.getStatus() == 200) {
			QuestionResponse questions = response.readEntity(QuestionResponse.class);
			for (Question question : questions.getItems()) {
				System.out.println(question.title);
				System.out.println(question.link);
				System.out.println();
			}
		} else {
			System.out.println("Response: " + response.getStatus());
			String string = response.readEntity(String.class);
			System.out.println(string);
		}
	}
	
}
