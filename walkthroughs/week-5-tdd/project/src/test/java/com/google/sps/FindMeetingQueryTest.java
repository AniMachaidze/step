// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** */
@RunWith(JUnit4.class)
public final class FindMeetingQueryTest {
  private static final Collection<Event> NO_EVENTS = Collections.emptySet();
  private static final Collection<String> NO_ATTENDEES = Collections.emptySet();
  private static final Collection<TimeRange> NO_TIME = Arrays.asList();

  // Some people that we can use in our tests.
  private static final String PERSON_A = "Person A";
  private static final String PERSON_B = "Person B";
  private static final String PERSON_C = "Person C";
  private static final String PERSON_D = "Person D";
  private static final String PERSON_F = "Person F";

  // All dates are the first day of the year 2020.
  private static final int TIME_0030AM = TimeRange.getTimeInMinutes(0, 30);
  private static final int TIME_0100AM = TimeRange.getTimeInMinutes(1, 0);
  private static final int TIME_0800AM = TimeRange.getTimeInMinutes(8, 0);
  private static final int TIME_0830AM = TimeRange.getTimeInMinutes(8, 30);
  private static final int TIME_0845AM = TimeRange.getTimeInMinutes(8, 45);
  private static final int TIME_0900AM = TimeRange.getTimeInMinutes(9, 0);
  private static final int TIME_0930AM = TimeRange.getTimeInMinutes(9, 30);
  private static final int TIME_1000AM = TimeRange.getTimeInMinutes(10, 0);
  private static final int TIME_1030AM = TimeRange.getTimeInMinutes(10, 30);
  private static final int TIME_1100AM = TimeRange.getTimeInMinutes(11, 00);
  private static final int TIME_1130AM = TimeRange.getTimeInMinutes(11, 30);
  private static final int TIME_1200AM = TimeRange.getTimeInMinutes(12, 00);
  private static final int TIME_1230AM = TimeRange.getTimeInMinutes(12, 30);
  private static final int TIME_1330AM = TimeRange.getTimeInMinutes(13, 30);

  private static final int DURATION_30_MINUTES = 30;
  private static final int DURATION_60_MINUTES = 60;
  private static final int DURATION_90_MINUTES = 90;
  private static final int DURATION_1_HOUR = 60;
  private static final int DURATION_2_HOUR = 120;

  private FindMeetingQuery query;

  @Before
  public void setUp() {
    query = new FindMeetingQuery();
  }

  @Test
  public void optionsForNoAttendees() {
    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_1_HOUR);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noOptionsForTooLongOfARequest() {
    // The duration should be longer than a day. This means there should be no options.
    int duration = TimeRange.WHOLE_DAY.duration() + 1;
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), duration);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = NO_TIME;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void eventSplitsRestriction() {
    // The event should split the day into two options (before and after the event).
    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void everyAttendeeIsConsidered() {
    // Have each person have different events. We should see two options
    // because each person has
    // split the restricted times.
    //
    // Events  :       |--A--|     |--B--|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_B)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void overlappingEvents() {
    // Have an event for each person, but have their events overlap.
    // We should only see two options.
    //
    // Events  :       |--A--|
    //                     |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_60_MINUTES),
                Arrays.asList(PERSON_B)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void nestedEvents() {
    // Have an event for each person, but have one person's event fully
    // contain another's event. We should see two options.
    //
    // Events  :       |----A----|
    //                   |--B--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_90_MINUTES),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_B)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void doubleBookedPeople() {
    // Have one person, but have them registered to attend two events at the same time.
    //
    // Events  :       |----A----|
    //                     |--A--|
    // Day     : |---------------------|
    // Options : |--1--|         |--2--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoom() {
    // Have one person, but make it so that there is just enough room at
    // one point in the day to have the meeting.
    //
    // Events  : |--A--|     |----A----|
    // Day     : |---------------------|
    // Options :       |-----|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
                Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void ignoresPeopleNotAttending() {
    // Add an event, but make the only attendee someone different from the
    // person looking to book
    // a meeting. This event should not affect the booking.
    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_A)));
    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noConflicts() {
    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    Collection<TimeRange> actual = query.query(NO_EVENTS, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void notEnoughRoom() {
    // Have one person, but make it so that there is not enough room at any
    // point in the day to
    // have the meeting.
    //
    // Events  : |--A-----| |-----A----|
    // Day     : |---------------------|
    // Options :

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
                Arrays.asList(PERSON_A)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_60_MINUTES);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = NO_TIME;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void optionalAttendeeCantAttend() {
    // The optional attendee should be ignored since they are busy
    // all day long.
    //
    // Events  :       |--A--|     |--B--|
    //           |--------------C--------------|
    // Day     : |-----------------------------|
    // Options : |--1--|     |--2--|     |--3--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_B)),
            new Event("Event 3", TimeRange.WHOLE_DAY, Arrays.asList(PERSON_C)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0830AM, TIME_0900AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void optionalAttendeeCanAttend() {
    // The optional attendee should be considered
    //
    // Events  :       |--A--|     |--B--|
    //                       |--C--|
    // Day     : |-----------------------------|
    // Options : |--1--|                 |--3--|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartDuration(TIME_0800AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0900AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 3C",
                TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_C)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B), DURATION_30_MINUTES);

    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoomForOptionalAttendee() {
    // The optional attendee should be ignored since considering their schedule
    // would result in a time slot smaller than the requested time.
    //
    // Events  : |--A--|     |----A----|
    //                 |--B--|
    // Day     : |---------------------|
    // Options :       |-----|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0900AM, TimeRange.END_OF_DAY, true),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TIME_0830AM, TIME_0845AM, false),
                Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);

    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartDuration(TIME_0830AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void onlyOptionalAttendees() {
    // No mandatory attendees, just two optional attendees with several gaps
    // in their schedules.
    //
    // Events  : |--A--|     |-B-|
    // Day     : |---------------------|
    // Options :       |-----|   |-----|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartDuration(TIME_0930AM, DURATION_30_MINUTES),
                Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartDuration(TIME_0830AM, DURATION_60_MINUTES),
            TimeRange.fromStartEnd(TIME_1000AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noTimeWithOnlyOptionals() {
    // No mandatory attendees, just two optional attendees with no
    // gaps in their schedules.
    // query should return that no time is available
    //
    // Events  : |--A--||----B---------|
    // Day     : |---------------------|
    // Options :

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0830AM, TimeRange.END_OF_DAY, true),
                Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(NO_ATTENDEES, DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = Arrays.asList(TimeRange.WHOLE_DAY);

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void justEnoughRoomForOptionalWithOverlapingEvents() {
    // No mandatory attendees, just two optional attendees with no
    // gaps in their schedules.
    // query should return that no time is available
    //
    // Events  : |--A--|      |----C-------|
    //	           |--B--|
    //               |--D--| |--D-|
    // Day     : |--------------------------|
    // Options :           |-|

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0930AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0830AM, TIME_1100AM, false),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TIME_1200AM, TimeRange.END_OF_DAY, false),
                Arrays.asList(PERSON_C)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_0900AM, TIME_1000AM, false),
                Arrays.asList(PERSON_D)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_1130AM, TIME_1230AM, false),
                Arrays.asList(PERSON_D)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B, PERSON_C), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_D);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartDuration(TIME_1100AM, DURATION_30_MINUTES));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void notEnoughtTimeForOptionalWithOverlapingEvents() {

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TIME_0030AM, TIME_0930AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_1000AM, TIME_1100AM, false),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(
                    TIME_1130AM, TimeRange.END_OF_DAY - DURATION_30_MINUTES, false),
                Arrays.asList(PERSON_C)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_0900AM, TIME_1200AM, false),
                Arrays.asList(PERSON_D)));

    MeetingRequest request =
        new MeetingRequest(Arrays.asList(PERSON_A, PERSON_B, PERSON_C), DURATION_60_MINUTES);
    request.addOptionalAttendee(PERSON_D);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected = NO_TIME;

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void onlyTimeWhenBothRequiredAndOptionalAreFree() {

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TIME_0930AM, TIME_1100AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_1200AM, TIME_1330AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TIME_0800AM, TIME_1200AM, false),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_1330AM, TimeRange.END_OF_DAY, false),
                Arrays.asList(PERSON_B)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_60_MINUTES);
    request.addOptionalAttendee(PERSON_B);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void mostOptioanlsCanAttendWithOverlapingEvents() {

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TIME_0930AM, TIME_1230AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0900AM, false),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TIME_0800AM, TIME_1100AM, false),
                Arrays.asList(PERSON_C)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_1200AM, TimeRange.END_OF_DAY, false),
                Arrays.asList(PERSON_D)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);
    request.addOptionalAttendee(PERSON_C);
    request.addOptionalAttendee(PERSON_D);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
            TimeRange.fromStartEnd(TIME_0900AM, TIME_0930AM, false),
            TimeRange.fromStartEnd(TIME_1230AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void notAllOptionalsCanAttend() {

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TIME_0900AM, TIME_1230AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0800AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0900AM, false),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_0800AM, TIME_1100AM, false),
                Arrays.asList(PERSON_C)),
            new Event(
                "Event 5",
                TimeRange.fromStartEnd(TIME_1200AM, TimeRange.END_OF_DAY, false),
                Arrays.asList(PERSON_D)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_30_MINUTES);
    request.addOptionalAttendee(PERSON_B);
    request.addOptionalAttendee(PERSON_C);
    request.addOptionalAttendee(PERSON_D);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TIME_1230AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void noneOfOptionalsFreeTimesWorksWithAttendees() {

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TIME_0830AM, TIME_1000AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_1100AM, TIME_1330AM, false),
                Arrays.asList(PERSON_A)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_1100AM, false),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_1330AM, TimeRange.END_OF_DAY, false),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 5",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TimeRange.END_OF_DAY, true),
                Arrays.asList(PERSON_C)),
            new Event(
                "Event 6",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_1130AM, false),
                Arrays.asList(PERSON_D)),
            new Event(
                "Event 7",
                TimeRange.fromStartEnd(TIME_1330AM, TimeRange.END_OF_DAY, false),
                Arrays.asList(PERSON_D)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A), DURATION_1_HOUR);
    request.addOptionalAttendee(PERSON_B);
    request.addOptionalAttendee(PERSON_C);
    request.addOptionalAttendee(PERSON_D);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0830AM, false),
            TimeRange.fromStartEnd(TIME_1000AM, TIME_1100AM, false),
            TimeRange.fromStartEnd(TIME_1330AM, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void optionalAndRequiredHaveSameEvent() {

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TIME_0100AM, TIME_1000AM, false),
                Arrays.asList(PERSON_A, PERSON_B)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0930AM, TimeRange.END_OF_DAY - DURATION_1_HOUR, false),
                Arrays.asList(PERSON_C)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TIME_0030AM, TIME_1130AM, false),
                Arrays.asList(PERSON_D)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(
                    TIME_1330AM, TimeRange.END_OF_DAY - DURATION_30_MINUTES, true),
                Arrays.asList(PERSON_D)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_A, PERSON_C), DURATION_1_HOUR);
    request.addOptionalAttendee(PERSON_B);
    request.addOptionalAttendee(PERSON_D);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            TimeRange.fromStartDuration(TimeRange.START_OF_DAY, DURATION_1_HOUR),
            TimeRange.fromStartEnd(
                TimeRange.END_OF_DAY - DURATION_1_HOUR, TimeRange.END_OF_DAY, true));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void worksForMoreOptionalAttendees() {

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_1000AM, false),
                Arrays.asList(PERSON_B)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0930AM, TIME_1130AM, false),
                Arrays.asList(PERSON_D)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TIME_1330AM, TimeRange.END_OF_DAY, false),
                Arrays.asList(PERSON_D)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_1030AM, TIME_1330AM, false),
                Arrays.asList(PERSON_A, PERSON_C)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_D), DURATION_1_HOUR);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);
    request.addOptionalAttendee(PERSON_C);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0930AM, false));

    Assert.assertEquals(expected, actual);
  }

  @Test
  public void optionsForDifferentGroupsOfOptionals() {

    Collection<Event> events =
        Arrays.asList(
            new Event(
                "Event 1",
                TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_1000AM, false),
                Arrays.asList(PERSON_A, PERSON_B)),
            new Event(
                "Event 2",
                TimeRange.fromStartEnd(TIME_0930AM, TIME_1130AM, false),
                Arrays.asList(PERSON_F)),
            new Event(
                "Event 3",
                TimeRange.fromStartEnd(TIME_1330AM, TimeRange.END_OF_DAY, false),
                Arrays.asList(PERSON_F)),
            new Event(
                "Event 4",
                TimeRange.fromStartEnd(TIME_1030AM, TIME_1330AM, false),
                Arrays.asList(PERSON_C, PERSON_D)));

    MeetingRequest request = new MeetingRequest(Arrays.asList(PERSON_F), DURATION_1_HOUR);
    request.addOptionalAttendee(PERSON_A);
    request.addOptionalAttendee(PERSON_B);
    request.addOptionalAttendee(PERSON_C);
    request.addOptionalAttendee(PERSON_D);

    Collection<TimeRange> actual = query.query(events, request);
    Collection<TimeRange> expected =
        Arrays.asList(
            // works for F and optionals: C and D
            TimeRange.fromStartEnd(TimeRange.START_OF_DAY, TIME_0930AM, false),
            // works for F and optionals: A and B
            TimeRange.fromStartEnd(TIME_1130AM, TIME_1330AM, false));

    Assert.assertEquals(expected, actual);
  }
}
