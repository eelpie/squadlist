package uk.co.squadlist.web.views.model;

import org.junit.Test;
import uk.co.squadlist.model.swagger.Member;

import static org.junit.Assert.assertNotNull;

public class DisplayMemberTest {

    @Test
    public void canPolyfillStringDateOfBirthToDate() {
        Member member = new Member().dateOfBirth("1991-06-09T00:00:00Z");
        DisplayMember displayMember = new DisplayMember(member, false);

        assertNotNull(displayMember.getDateOfBirth());
    }

}
