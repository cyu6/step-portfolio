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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Container class for {@code query} function. */
public final class FindMeetingQuery {
    
  /** Returns a list of possible time ranges for the meeting request given a list of events. */
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Meeting duration cannot be longer than a day.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    if (events.isEmpty() || request.getAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }

    // If there are no mandatory attendees, check optional attendees and treat them as mandatory.
    Collection<String> attendees = request.getAttendees();
    if (attendees.isEmpty()) {
      attendees = request.getOptionalAttendees();
    }

    List<TimeRange> eventTimes = events.stream().filter(
        event -> !Collections.disjoint(event.getAttendees(), request.getAttendees()))
        .map(Event::getWhen).collect(Collectors.toList());
    if (eventTimes.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    Collections.sort(eventTimes, TimeRange.ORDER_BY_START);

    List<TimeRange> possibleTimes = findAvailabilityForMandatoryAttendees(eventTimes, duration);

    // If there were no mandatory attendees or there are no optional attendees, return now.
    if (attendees.equals(request.getOptionalAttendees()) || 
        request.getOptionalAttendees().isEmpty()) {
      return possibleTimes;
    }

    // Collect time ranges of optional attendees' events.
    List<TimeRange> optionalTimes = events.stream().filter(
        event -> !Collections.disjoint(event.getAttendees(), request.getOptionalAttendees()))
        .map(Event::getWhen).collect(Collectors.toList());
    if (optionalTimes.isEmpty()) {
      return possibleTimes;
    }
    Collections.sort(optionalTimes, TimeRange.ORDER_BY_START);

    List<TimeRange> possibleWithOptional = compareWithOptionalAvailability(optionalTimes, possibleTimes);

    // Optional attendees cannot make any of the possible time slots (for mandatory attendees).
    if (possibleWithOptional.isEmpty()) {
      return possibleTimes;
    }

    return possibleWithOptional;
  }

  /** Create a meeting timeslot if there is enough time between firstTime and secondTime. */
  private static Optional<TimeRange> createDuration(long duration, int firstTime, int secondTime, 
    boolean inclusive) {
    if ((secondTime - firstTime) >= duration) {
      return Optional.of(TimeRange.fromStartEnd(firstTime, secondTime, inclusive));
    }
    return Optional.empty();
  }

  /** Returns a list of time slots that work for mandatory attendees. */
  private List<TimeRange> findAvailabilityForMandatoryAttendees(
    List<TimeRange> eventTimes, long duration) {
    List<TimeRange> possibleTimes = new ArrayList<>();

    createDuration(duration, TimeRange.START_OF_DAY, eventTimes.get(0).start(), /* inclusive= */ false)
    .ifPresent(possibleTimes::add);
    for (int i = 0; i < eventTimes.size() - 1; i++) {
      TimeRange current = eventTimes.get(i);
      TimeRange next = eventTimes.get(i + 1);
      if (!current.overlaps(next)) {
        createDuration(duration, current.end(), next.start(), /* inclusive= */ false)
        .ifPresent(possibleTimes::add);
      } 
      else {
        // If the second event is completely contained, move on to the next event.
        if (current.contains(next)) {
          eventTimes.remove(next);
          i--;
        }
      }
    }
    createDuration(duration, eventTimes.get(eventTimes.size() - 1).end(), 
    TimeRange.END_OF_DAY, /* inclusive= */ true).ifPresent(possibleTimes::add);
    
    return possibleTimes;
  }

  /** Returns a list of time slots that work for both optional and mandatory attendees. */
  private static List<TimeRange> compareWithOptionalAvailability(Collection<TimeRange> optionalTimes, 
    Collection<TimeRange> possibleTimes) {
    List<TimeRange> possibleWithOptional = new ArrayList<TimeRange>(possibleTimes);
    for (TimeRange optionalTime : optionalTimes) {
      for (TimeRange possibleTime : possibleTimes) {
        // Time range cannot conflict with the rest of the possible times, so skip over them.
        if (optionalTime.end() < possibleTime.start()) {
          break;
        }
        // Remove time range if the optional event overlaps with a possible time range. 
        if (optionalTime.overlaps(possibleTime)) {
          possibleWithOptional.remove(possibleTime);
        }
      }
    }
    return possibleWithOptional;
  }
}