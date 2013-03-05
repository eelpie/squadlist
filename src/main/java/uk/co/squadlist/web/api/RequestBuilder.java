package uk.co.squadlist.web.api;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;

import com.google.common.collect.Lists;

public class RequestBuilder {

	private final ApiUrlBuilder apiUrlBuilder;
	
	public RequestBuilder(ApiUrlBuilder apiUrlBuilder) {
		this.apiUrlBuilder = apiUrlBuilder;
	}
	
	public HttpPost buildCreateInstanceRequest(String id, String name) throws UnsupportedEncodingException {
		final HttpPost post = new HttpPost(apiUrlBuilder.getInstancesUrl());
		
		final List<NameValuePair> nameValuePairs = Lists.newArrayList();
		nameValuePairs.add(new BasicNameValuePair("id", id));
		nameValuePairs.add(new BasicNameValuePair("name", name));
		post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		return post;
	}
	
}
