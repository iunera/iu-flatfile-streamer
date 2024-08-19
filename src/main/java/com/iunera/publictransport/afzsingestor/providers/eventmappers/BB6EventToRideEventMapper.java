package com.iunera.publictransport.afzsingestor.providers.eventmappers;

/*-
 * #%L
 * iu-flatfile-streamer
 * %%
 * Copyright (C) 2024 Tim Frey, Christian Schmitt
 * %%
 * Licensed under the OPEN COMPENSATION TOKEN LICENSE (the "License").
 *
 * You may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * <https://github.com/open-compensation-token-license/license/blob/main/LICENSE.md>
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @octl.sid: 1b6f7a5d-8dcf-44f1-b03a-77af04433496
 * #L%
 */

import com.iunera.publictransport.domain.trip.EntryExitActivity;
import com.iunera.publictransport.domain.trip.EventType;
import com.iunera.publictransport.domain.trip.TripWaypoint;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BB6EventToRideEventMapper {

  private static final long serialVersionUID = 16814791450411341L;

  private String provider;
  ZoneId timeZone = ZoneId.of("Europe/Berlin");

  static DateTimeFormatter datetimeformattertime = DateTimeFormatter.ofPattern("HH:mm:ss");
  static DateTimeFormatter datetimeformatterDay = DateTimeFormatter.ofPattern("dd.MM.yyyy");

  public BB6EventToRideEventMapper(String provider, ZoneId timeZone) {
    this.timeZone = timeZone;
    this.provider = provider;
  }

  public TripWaypoint map(String value) throws Exception {

    String file[] = value.split(";");
    String line[] = file[1].split(",");
    switch (line[0]) {
      case "1":
        return (new TripWaypoint(
            EventType.MESSAGE,
            TripWaypoint.eventSubtypeMessageDeviceIDMESSAGE,
            this.provider,
            null,
            null,
            LocalDate.parse(line[1], datetimeformatterDay),
            // here we need to take actual departure time than planned departure
            toDate(line[2], line[1]),
            line[3],
            line[3],
            line[5],
            line[4],
            null,
            "stop"));

      case "4":
        if (line.length > 5) {
          Map<String, EntryExitActivity> doorEntryExitActivity4 = new HashMap<>();
          doorEntryExitActivity4.put(
              line[5],
              new EntryExitActivity(Double.parseDouble(line[3]), Double.parseDouble(line[4])));
          return (new TripWaypoint(
              EventType.PLANNEDSTOP,
              TripWaypoint.eventSubtypePeopleCount,
              "Ilmenau",
              null,
              null,
              null,
              // here only time is available not date
              null,
              "",
              "",
              "licensePlate",
              "",
              doorEntryExitActivity4,
              line[2]));
        }

      default:
        break;
    }

    return null;
  }

  private Instant toDate(String time, String day) {
    return LocalDate.parse(day, datetimeformatterDay)
        .atStartOfDay()
        .plusSeconds(LocalTime.parse(time, datetimeformattertime).toSecondOfDay())
        .atZone(timeZone)
        .toInstant();
  }
}
