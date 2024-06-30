package com.mars_sim.core.time;

import static org.junit.Assert.assertNotEquals;

import junit.framework.TestCase;

public class MarsTimeTest extends TestCase {

    public void testAddTime() {
        MarsTime start = new MarsTime(1,1, 1, 100D, 1);

        MarsTime later = start.addTime(1000D);
        assertEquals("New Sol of Month", start.getSolOfMonth() + 1, later.getSolOfMonth());
        assertNotEquals("Old and new time are different", start, later);
        assertEquals("Time difference", 1000D, later.getTimeDiff(start));
    }

    public void testAddTimeMonthEnd() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 100D, 1);

        MarsTime later = start.addTime(1000D);
        assertEquals("New Sol of Month", 1, later.getSolOfMonth());
        assertEquals("New Month", start.getMonth() +1, later.getMonth());
        assertNotEquals("Old and new Sol time are different", start, later);
        assertEquals("SOl Time difference", 1000D, later.getTimeDiff(start));
    }

    public void testAddTimeMonthEndMSols() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);

        MarsTime later = start.addTime(500D);
        assertEquals("New Sol of Month", 1, later.getSolOfMonth());
        assertEquals("New Month", start.getMonth() +1, later.getMonth());
        assertEquals("New milliSols", 100D, later.getMillisol());
    }

    public void testTimeEquals() {
        MarsTime start = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);
        MarsTime same = new MarsTime(1,1, MarsTime.SOLS_PER_MONTH_LONG, 600D, 1);

        assertEquals("MarsTime equals", start, same);
    }

    public void testDifferenceTime() {
        MarsTime start = new MarsTime(1,1, 1, 100D, 1);

        MarsTime later = new MarsTime(1,1, 1, 150D, 1);
        assertEquals("Difference of same sol", 50D, later.getTimeDiff(start));

        later = new MarsTime(1,1, 2, 150D, 1);
        assertEquals("Difference of different sol", 1050D, later.getTimeDiff(start));
    }

    public void testMarsDate() {
        MarsTime start = new MarsTime(1,1, 1, 100D, 1);
        MarsTime later = new MarsTime(1,1, 1, 150D, 1);

        assertEquals("Difference of same mars Date", start.getDate(), later.getDate());

        later = new MarsTime(1,1, 2, 150D, 1);
        assertNotEquals("Difference of different MarsDates", start.getDate(), later.getDate());
    }
}
