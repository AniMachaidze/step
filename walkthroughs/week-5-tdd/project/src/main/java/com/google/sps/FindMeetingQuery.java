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

import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public final class FindMeetingQuery {

	private static final int END_OF_DAY = 1440;

	public Collection<TimeRange> query(Collection<Event> events,
		MeetingRequest request) {
		int duration = (int) request.getDuration();
		Collection<String> attendees = request.getAttendees();
		Collection<String> optionalAttendees = request.getOptionalAttendees();

		Collection<TimeRange> availableTimes = new ArrayList<>();
		Collection<TimeRange> availableTimesWithOptionals = new ArrayList<>();

		availableTimes = getAvaliableTimes(attendees, events, duration);

		if (!optionalAttendees.isEmpty()) {
			availableTimesWithOptionals = getAvaliableTimes(optionalAttendees,
				events, duration);
		}

		if (optionalAttendees.isEmpty()) {
			return availableTimes;
		}

		if (attendees.isEmpty()) {
			return availableTimesWithOptionals;
		}

		availableTimesWithOptionals = intersection(availableTimesWithOptionals,
			availableTimes);

		if (!availableTimesWithOptionals.isEmpty()) {
			return availableTimesWithOptionals;
		}

		return availableTimes;
	}

	/**
	 * Finds avaliable times for the given atteendee list, input 
	 * collection and event duration
	 */
	private Collection<TimeRange> getAvaliableTimes(Collection<String> attendees,
		Collection<Event> events, int duration) {
		Collection<TimeRange> availableTimes = new ArrayList<>();

		int start = 0;
		int end = start + duration;
		int availablePeriodStart = -1, availablePeriodEnd = -1;

		while (end<= END_OF_DAY) {
			TimeRange currentTimeRange = TimeRange.fromStartDuration(start, duration);
			boolean works = true;

			for (Event event: events) {
				if (!works) break;

				TimeRange eventTimeRange = event.getWhen();
				if (currentTimeRange.overlaps(eventTimeRange)) {
					Collection<String> eventAttendees = event.getAttendees();
					if (!Collections.disjoint(attendees, eventAttendees)) {
						works = false;
						continue;
					}
				}
			}

			if (works) {
				if (availablePeriodEnd == -1) {
					availablePeriodStart = start;
				}

				availablePeriodEnd = end;

				if (end == END_OF_DAY) {
					TimeRange availableTime = TimeRange
						.fromStartEnd(availablePeriodStart, availablePeriodEnd, false);
					availableTimes.add(availableTime);
				}
			} else {
				if (availablePeriodEnd != -1) {
					TimeRange availableTime = TimeRange
						.fromStartEnd(availablePeriodStart, availablePeriodEnd, false);
					availableTimes.add(availableTime);
				}

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
	private List<TimeRange> intersection(Collection<TimeRange> list1,
		Collection<TimeRange> list2) {
		List<TimeRange> intersection = new ArrayList<>();

		for (TimeRange el1: list1) {
			for (TimeRange el2: list2) {
				if (el1.contains(el2)) {
					intersection.add(el2);
				} else if (el2.contains(el1)) {
					intersection.add(el1);
				}
			}
		}

		return intersection;
	}
}
