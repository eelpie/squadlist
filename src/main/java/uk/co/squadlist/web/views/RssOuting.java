package uk.co.squadlist.web.views;

import uk.co.eelpieconsulting.common.geo.model.LatLong;
import uk.co.eelpieconsulting.common.views.rss.RssFeedable;
import uk.co.squadlist.model.swagger.Outing;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RssOuting implements RssFeedable {
	
	private final Outing outing;
	private final String url;
		
	public RssOuting(Outing outing, String url) {
		this.outing = outing;
		this.url = url;
	}

	@Override
	public String getAuthor() {
		return null;
	}

	@Override
	public List<String> getCategories() {
		return new ArrayList<>();
	}

	@Override
	public Date getDate() {
		return outing.getDate().toDate();
	}

	@Override
	public String getDescription() {
		return outing.getNotes();
	}

	@Override
	public String getHeadline() {
		return outing.getSquad().getName() + " - " + outing.getDate();	// TODO format
	}

	@Override
	public String getImageUrl() {
		return null;
	}

	@Override
	public LatLong getLatLong() {
		return null;
	}

	@Override
	public String getFeatureName() {
		return null;
	}

	@Override
	public String getWebUrl() {
		return url;
	}

}
