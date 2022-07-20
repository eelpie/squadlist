package uk.co.squadlist.web.views.model;

import uk.co.squadlist.web.model.Member;

public class DisplayMember {

    private final Member member;
    private final boolean editable;

    public DisplayMember(Member member, boolean editable) {
        this.member = member;
        this.editable = editable;
    }

    public Member getMember() {
        return member;
    }

    public boolean isEditable() {
        return editable;
    }

}