package com.ibm.ws.slackstacking;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class SlackStacker {

	public static void main(String[] args) {
		Client client = ClientBuilder.newBuilder().newClient();
		WebTarget target = client.target("https://api.stackexchange.com/2.2");
		//WebTarget questionTarget = target.path("questions").queryParam("order", "desc").queryParam("sort", "created").queryParam("site", "stackoverflow");
		
		Invocation.Builder builder = target.request();
		//builder.accept(MediaType.APPLICATION_JSON);
		
		QuestionResponse response = builder.get(QuestionResponse.class);
		for (Question question : response.items) {
			System.out.println(question.title);
			System.out.println(question.link);
			System.out.println();
		}
	}
	
}
