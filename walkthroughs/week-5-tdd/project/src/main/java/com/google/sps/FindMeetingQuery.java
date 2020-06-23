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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class FindMeetingQuery {

  /* Finds available times for the meeting, such that every required attendee
   *  can attend and higest number of optionals possible is able to attend.
   */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    int duration = (int) request.getDuration();
    Collection<String> attendees = request.getAttendees();
    Collection<String> optionalAttendees = request.getOptionalAttendees();

    Collection<TimeRange> availableTimes = new ArrayList<>();
    List<Integer> availableTimesOptionals = new ArrayList<>();

    availableTimes = getAvailableTimes(attendees, events, duration);
    availableTimesOptionals = getAvailableTimesForOptionals(optionalAttendees, events, duration);

    Collection<TimeRange> overlaps =
        findOverlaps(availableTimes, availableTimesOptionals, duration, optionalAttendees.size());

    return overlaps;
  }

  /**
   * Finds avaliable times considering the required atteendee list, existing events and event
   * duration
   */
  private Collection<TimeRange> getAvailableTimes(
      Collection<String> attendees, Collection<Event> events, int duration) {
    List<TimeRange> busyTimes = new ArrayList<>();
    Collection<TimeRange> freeTimes;

    // finds time ranges that do not work for at least one required attendee
    for (Event event : events) {
      TimeRange eventTimeRange = event.getWhen();

      Collection<String> eventAttendees = event.getAttendees();
      if (!Collections.disjoint(attendees, eventAttendees)) {
        busyTimes.add(eventTimeRange);
      }
    }

    freeTimes = findFreeTimesFromBusy(busyTimes, duration);

    return freeTimes;
  }

  /**
   * Calculates for each minute in a day how many optional attendee are free. Returns list X of size
   * 1440, where X[i] is how many optional attendees are free on minute i
   */
  private List<Integer> getAvailableTimesForOptionals(
      Collection<String> optionalAttendees, Collection<Event> events, int duration) {
    Map<TimeRange, Set<String>> busyTimesForOptionals = new HashMap<>();

    // for each event, finds which optional attendees are busy
    for (Event event : events) {
      TimeRange eventTimeRange = event.getWhen();

      Collection<String> eventAttendees = event.getAttendees();
      Set<String> busyOptionals = new HashSet<>(optionalAttendees);
      busyOptionals.retainAll(eventAttendees);

      if (busyOptionals.size() > 0) {
        if (busyTimesForOptionals.containsKey(eventTimeRange)) {
          busyTimesForOptionals.get(eventTimeRange).addAll(busyOptionals);
        } else {
          busyTimesForOptionals.put(eventTimeRange, busyOptionals);
        }
      }
    }

    List<Set<String>> setOfBusyOptionalsEachMinute = new ArrayList<>();
    for (int i = 0; i <= TimeRange.END_OF_DAY; i++) {
      setOfBusyOptionalsEachMinute.add(new HashSet<>());
    }

    // find busy people set for every minute and merge sets if there are overlaping time ranges
    for (TimeRange timeRange : busyTimesForOptionals.keySet()) {
      for (int i = timeRange.start(); i < timeRange.end(); i++) {
        setOfBusyOptionalsEachMinute.get(i).addAll(busyTimesForOptionals.get(timeRange));
      }
    }

    List<Integer> numberOfFreeOptionals = new ArrayList<>();
    int totalOptionals = optionalAttendees.size();

    for (int i = 0; i <= TimeRange.END_OF_DAY; i++) {
      numberOfFreeOptionals.add(totalOptionals - setOfBusyOptionalsEachMinute.get(i).size());
    }

    return numberOfFreeOptionals;
  }

  /**
   * Calculates for each number of optional attendees, which free time ranges are overlaping with
   * available times for required attendees and returns overlaping free times with highest number
   * possible
   */
  private Collection<TimeRange> findOverlaps(
      Collection<TimeRange> availableTimes,
      List<Integer> numberOfOptionals,
      int duration,
      int maxOptionals) {

    List<List<TimeRange>> overlaps = new ArrayList<>();

    for (int i = 1; i <= maxOptionals + 1; i++) {
      overlaps.add(new ArrayList<TimeRange>());
    }

    // for each available time for required attendees find chuncks of times with same number of free
    // optionals and populates overlaps
    for (TimeRange availableTime : availableTimes) {
      int start = availableTime.start();
      int end = availableTime.end();

      if (end > TimeRange.END_OF_DAY) {
        end = TimeRange.END_OF_DAY;
      }
      int optionalsNum = numberOfOptionals.get(start);

      // for time range which has same number of free optionals
      int localStart = start;
      int localEnd = start;

      for (int i = start; i <= end; i++) {
        if (numberOfOptionals.get(i) != optionalsNum || i == end) {
          TimeRange time;

          if (i == TimeRange.END_OF_DAY) {
            time = TimeRange.fromStartEnd(localStart, localEnd, true);
          } else {
            time = TimeRange.fromStartEnd(localStart, localEnd, false);
          }

          if (localEnd - localStart >= duration) {
            overlaps.get(optionalsNum).add(time);
          }

          optionalsNum = numberOfOptionals.get(i);
          localStart = i;
        }

        localEnd++;
      }
    }

    for (int i = maxOptionals; i > 0; i--) {
      if (!overlaps.get(i).isEmpty()) {
        return overlaps.get(i);
      }
    }

    return availableTimes;
  }

  /* Find the free time period of at least event length using busy time ranges */
  private Collection<TimeRange> findFreeTimesFromBusy(List<TimeRange> busyTimes, int duration) {
    Collections.sort(busyTimes, TimeRange.ORDER_BY_START);

    int start = TimeRange.START_OF_DAY;
    List<TimeRange> availableTimes = new ArrayList<TimeRange>();

    for (TimeRange timeRange : busyTimes) {
      int timeRangeStart = timeRange.start();
      int timeRangeEnd = timeRange.end();

      // free time: |--------------|
      // busy time:    |------|
      if (timeRangeStart > start) {
        if (timeRangeStart - start >= duration) {
          availableTimes.add(TimeRange.fromStartEnd(start, timeRangeStart, false));
        }
        start = timeRangeEnd;
      } else
      // free time:    |---------|
      // busy time: |------|
      if (timeRangeEnd > start) {
        start = timeRangeEnd;
      }
    }

    if (TimeRange.END_OF_DAY - start >= duration) {
      availableTimes.add(TimeRange.fromStartEnd(start, TimeRange.END_OF_DAY, true));
    }

    return availableTimes;
  }
}
