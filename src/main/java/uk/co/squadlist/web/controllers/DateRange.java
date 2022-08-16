package uk.co.squadlist.web.controllers;

import org.joda.time.LocalDate;

public class DateRange {

    private final LocalDate start;
    private final LocalDate end;

    private final String month;

    private final boolean current;

    public DateRange(LocalDate start, LocalDate end, String month, boolean current) {
        this.start = start;
        this.end = end;
        this.month = month;
        this.current = current;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public String getMonth() {
        return month;
    }

    public boolean isCurrent() {
        return current;
    }

    @Override
    public String toString() {
        return "DateRange{" +
                "start=" + start +
                ", end=" + end +
                ", month='" + month + '\'' +
                ", current=" + current +
                '}';
    }

}
