package uk.co.squadlist.web.api;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.co.squadlist.web.model.Member;

import com.google.common.collect.Lists;

@Component
public class RequestBuilder {

	private final ApiUrlBuilder apiUrlBuilder;

	@Autowired
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
	
	public HttpPost buildUpdateMemberRequest(String instance, Member member) throws UnsupportedEncodingException {
		final HttpPost post = new HttpPost(apiUrlBuilder.getMemberDetailsUrl(instance, member.getId()));
		post.setEntity(new UrlEncodedFormEntity(member.toNameValuePairs()));
		return post;
	}

}
