package uk.co.squadlist.web.views.model;

import com.google.common.base.Strings;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import uk.co.squadlist.model.swagger.Member;

import java.util.Date;

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

    public String getDisplayName() {
        if (!Strings.isNullOrEmpty(member.getKnownAs())) {
            return member.getKnownAs();
        } else {
            return member.getFirstName() + " " + member.getLastName();
        }
    }

}