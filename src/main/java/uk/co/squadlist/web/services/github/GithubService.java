package uk.co.squadlist.web.services.github;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.kohsuke.github.GHIssue;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

@Component
public class GithubService {
	
	private final static Logger log = Logger.getLogger(GithubService.class);
	
	private static final String GITHUB_REPO = "eelpie/squadlist";
	
	private GitHub github;
	
	private Cache<GHIssueState, List<GHIssue>> cache;
	
	@Autowired
	public GithubService(@Value("#{squadlist['github.username']}") String username, @Value("#{squadlist['github.token']}") String token) {
		this.cache = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.HOURS).build();
		try {
			log.info("Github config: " + username + "/" + token);
			if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(token)) {
				this.github = GitHub.connect(username, token);
			} else {
				log.warn("Github service non configured");
			}
			
		} catch (IOException e) {
			log.error(e);
		}
	}
	
	public List<GHIssue> getOpenIssues() {
		return getIssuesWhichAre(GHIssueState.OPEN);
	}

	public List<GHIssue> getClosedIssues() {
		return getIssuesWhichAre(GHIssueState.CLOSED);
	}
	
	private List<GHIssue> getIssuesWhichAre(final GHIssueState state) {
		List<GHIssue> cached = cache.getIfPresent(state);
		if (cached != null) {
			log.info("Returning cached for: " + state.toString());
			return cached;
		}
		
		if (github == null) {
			return Lists.newArrayList();
		}
    	try {
    		log.info("Fetching issues from github: " + state);
			List<GHIssue> issues = github.getRepository(GITHUB_REPO).getIssues(state);
			cache.put(state, issues);
			return issues;
			
		} catch (IOException e) {
			log.error(e);
		}
    	return Lists.newArrayList();
	}
	
}