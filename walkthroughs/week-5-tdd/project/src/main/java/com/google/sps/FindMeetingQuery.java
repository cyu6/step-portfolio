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

/**
 * Given a list of events, returns a list of possible time ranges for the meeting request.
 */
public final class FindMeetingQuery {
  public Collection<TimeRange> query(Collection<Event> events, MeetingRequest request) {
    // Meeting duration cannot be longer than a day.
    if (request.getDuration() > TimeRange.WHOLE_DAY.duration()) {
      return Arrays.asList();
    }
    if (events.isEmpty() || request.getAttendees().isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    
    List<TimeRange> eventTimes = new ArrayList<>();
    // Collect time ranges while filtering out non-attendees.
    for (Event event : events) {
      if (!Collections.disjoint(event.getAttendees(), request.getAttendees())) {
        // The event and meeting request have at least one attendee in common.
        eventTimes.add(event.getWhen());
      }
    }
    if (eventTimes.isEmpty()) {
      return Arrays.asList(TimeRange.WHOLE_DAY);
    }
    Collections.sort(eventTimes, TimeRange.ORDER_BY_START);

    List<TimeRange> possibleTimes = new ArrayList<>();

    // Check duration between start of day and start of first event.
    if ((eventTimes.get(0).start() - TimeRange.START_OF_DAY) >= request.getDuration()) {
      possibleTimes.add(TimeRange.fromStartEnd(
        TimeRange.START_OF_DAY, eventTimes.get(0).start(), false)
      );
    }

    for (int i = 0; i < eventTimes.size() - 1; i++) {
      TimeRange current = eventTimes.get(i);
      TimeRange next = eventTimes.get(i + 1);
      if (!current.overlaps(next)) {
        // Check duration between events and create a time range if there is enough time.
        if ((next.start() - current.end()) >= request.getDuration()) {
          possibleTimes.add(TimeRange.fromStartEnd(current.end(), next.start(), false));
        }
      } 
      else {
        // If the second event is completely contained, move on to the next event.
        if (current.contains(next)) {
          eventTimes.remove(next);
          i--;
        }
      }
    }

    // Check duration between end of last event and end of day.
    if ((TimeRange.END_OF_DAY - eventTimes.get(eventTimes.size() - 1).end()) >= 
          request.getDuration()) 
    {
      possibleTimes.add(TimeRange.fromStartEnd(
        eventTimes.get(eventTimes.size() - 1).end(), TimeRange.END_OF_DAY, true)
      );
    }

    return possibleTimes;
  }
}
