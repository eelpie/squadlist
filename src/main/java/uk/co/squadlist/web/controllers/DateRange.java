package uk.co.squadlist.web.controllers;

import org.joda.time.DateTime;

public class DateRange {

    private final DateTime start;
    private final DateTime end;
    private final boolean current;

    public DateRange(DateTime start, DateTime end, boolean current) {
        this.start = start;
        this.end = end;
        this.current = current;
    }

    public DateTime getStart() {
        return start;
    }

    public DateTime getEnd() {
        return end;
    }

    public boolean isCurrent() {
        return current;
    }

}
