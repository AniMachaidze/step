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
import java.util.Comparator;

public final class FindMeetingQuery {

	public Collection < TimeRange > query(Collection < Event > events, MeetingRequest request) {
		int duration = (int) request.getDuration();
		Collection < String > attendees = request.getAttendees();
		Collection < String > optionalAttendees = request.getOptionalAttendees();

		Collection < TimeRange > availableTimes = new ArrayList < >();
		Collection < TimeRange > availableTimesOptionals = new ArrayList < >();

		availableTimes = getAvailableTimes(attendees, events, duration);

		if (optionalAttendees.isEmpty()) {
			// If there is no optional attendees only consider required attendees
			return availableTimes;
		} else {
			// Otherwise, calculate best times for optional attendees
			availableTimesOptionals = getAvailableTimes(optionalAttendees, events, duration);
		}

		// If there is no required atendees, return time which works for optionals
		if (attendees.isEmpty()) {
			return availableTimesOptionals;
		}

		// Find times that works both for required and optional attendees
		Collection < TimeRange > availableTimesBothAttendees = intersection(availableTimesOptionals, availableTimes);

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
	private Collection < TimeRange > getAvailableTimes(Collection < String > attendees, Collection < Event > events, int duration) {
		// TODO: Make this method more optimal
		Collection < TimeRange > availableTimes = new ArrayList < >();

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
			for (Event event: events) {
				TimeRange eventTimeRange = event.getWhen();
				if (currentTimeRange.overlaps(eventTimeRange)) {
					Collection < String > eventAttendees = event.getAttendees();
					if (!Collections.disjoint(attendees, eventAttendees)) {
						works = false;
					}
				}

				if (!works) break;
			}

			if (works) {
				if (availablePeriodEnd == -1) {
					availablePeriodStart = start;
				}

				availablePeriodEnd = end;

				if (end == TimeRange.END_OF_DAY) {
					TimeRange availableTime = TimeRange.fromStartEnd(availablePeriodStart, availablePeriodEnd, true);
					availableTimes.add(availableTime);
				}
			} else {
				if (availablePeriodEnd != -1) {
					TimeRange availableTime = TimeRange.fromStartEnd(availablePeriodStart, availablePeriodEnd, false);
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
	private List < TimeRange > intersection(Collection < TimeRange > list1, Collection < TimeRange > list2) {
		List < TimeRange > intersection = new ArrayList < >();

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
