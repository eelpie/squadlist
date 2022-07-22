package uk.co.squadlist.web.views.model;

public class NavItem {

    private final String label;
    private final String url;
    private final Integer count;
    private final String countId;
    private final boolean selected;

    public NavItem(String label, String url, Integer count, String countId, boolean selected) {
        this.label = label;
        this.url = url;
        this.count = count;
        this.countId = countId;
        this.selected = selected;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public Integer getCount() {
        return count;
    }

    public String getCountId() {
        return countId;
    }

    public boolean isSelected() {
        return selected;
    }

}
