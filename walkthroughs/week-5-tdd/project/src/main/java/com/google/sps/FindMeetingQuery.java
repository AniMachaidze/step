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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FindMeetingQuery {

  /* Finds available times for the meeting, such that every required attendee 
  *  can attend or time periods when everyone can attend (both required and optional)
  *  if there exists any.
  */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    int duration = (int) request.getDuration();
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    Collection<TimeRange> availableTimes = new ArrayList<>();
    Collection<TimeRange> availableTimesOptionals = new ArrayList<>();

    availableTimes = getAvailableTimes(attendees, events, duration);

    // If there is no optional attendees only consider required attendees
    if (optionalAttendees.isEmpty()) {
      return availableTimes;
    }

    // Calculate best times for optional attendees
    availableTimesOptionals =
      getAvailableTimes(optionalAttendees, events, duration);
  

    // If there is no required attendees, return time which works for optionals
    if (attendees.isEmpty()) {
      return availableTimesOptionals;
    }

    // Find times that works both for required and optional attendees
    Collection<TimeRange> availableTimesBothAttendees = intersection(
      availableTimesOptionals,
      availableTimes, 
      duration
    );

    // Return time if any which works for both required and optional attendees
    if (!availableTimesBothAttendees.isEmpty()) {
      return availableTimesBothAttendees;
    }

    return availableTimes;
  }

  /**
   * Finds avaliable times for the given atteendee list, input
   * collection and event duration
   */
  private Collection<TimeRange> getAvailableTimes(Collection<String> attendees, Collection<Event> events, int duration) {
    // TODO: Make this method more optimal
    Collection<TimeRange> availableTimes = new ArrayList<>();

    // possible start and end times of the event
    int start = 0;
    int end = start + duration;

    // start and end time of the free period when event can occur
    int availablePeriodStart = -1;
    int availablePeriodEnd = -1;

    // Consider every possible time period and see if it works for attendees
    while (end <= TimeRange.END_OF_DAY) {
      TimeRange currentTimeRange = TimeRange.fromStartDuration(start, duration);
      boolean works = true;

      // Comparing possible time range to the events to see if there is any overlaps
      for (Event event : events) {
        TimeRange eventTimeRange = event.getWhen();
        if (currentTimeRange.overlaps(eventTimeRange)) {
          Collection<String> eventAttendees = event.getAttendees();
          if (!Collections.disjoint(attendees, eventAttendees)) {
            works = false;
          }
        }

        if (!works) break;
      }

      if (works) {
        // If last time period did not work, update start time
        if (availablePeriodEnd == -1) {
          availablePeriodStart = start;
        }
        // And extend end time
        availablePeriodEnd = end;

        // If this is last possible complete time period
        if (end == TimeRange.END_OF_DAY) {
          availableTimes.add(TimeRange.fromStartEnd(
            availablePeriodStart,
            availablePeriodEnd,
            true));
        }
      } else {

        // If last time period was valid
        if (availablePeriodEnd != -1) {
          availableTimes.add(TimeRange.fromStartEnd(
            availablePeriodStart,
            availablePeriodEnd,
            false));
        }

        // Mark that this time period did not work
        availablePeriodStart = -1;
        availablePeriodEnd = -1;
      }

      start++;
      end++;
    }

    return availableTimes;
  }

  /**
   * Finds intersection between elements of two lists of
   * TimeRange and returns as a list
   */
  private List<TimeRange> intersection(Collection<TimeRange> list1, Collection<TimeRange> list2, int duration) {
    List<TimeRange> intersection = new ArrayList<>();

    for (TimeRange el1 : list1) {
      for (TimeRange el2 : list2) {
        int overlapStart = Math.max(el1.start(), el2.start());
        int overlapEnd = Math.min(el1.end(), el2.end());
        if (overlapEnd - overlapStart >= duration) {
          intersection.add(TimeRange.fromStartEnd(overlapStart, overlapEnd, false));
        }
      }
    }

    return intersection;
  }
}
