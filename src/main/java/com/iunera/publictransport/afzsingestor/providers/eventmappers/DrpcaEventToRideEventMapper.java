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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DrpcaEventToRideEventMapper {

  private static final long serialVersionUID = -2495898445211103539L;

  private String provider;
  ZoneId timeZone = ZoneId.of("Europe/Berlin");

  static DateTimeFormatter datetimeformattertime = DateTimeFormatter.ofPattern(" HH:mm:ss");
  static DateTimeFormatter datetimeformatterDay = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public DrpcaEventToRideEventMapper(String provider, ZoneId timeZone) {
    this.timeZone = timeZone;
    this.provider = provider;
  }

  public TripWaypoint map(String value) throws Exception {
    String line[] = value.split(";");
    try {

      switch (line[0]) {
        case "gps":
          if (line[4].equals("") || line[5].equals("")) {
            return (new TripWaypoint(
                EventType.WAYPOINT,
                TripWaypoint.eventSubtypeMessageDeviceIDMESSAGE,
                this.provider,
                null,
                null,
                toLocalDate(Long.parseLong(line[1] + "000")),
                Instant.ofEpochMilli(Long.parseLong(line[1] + "000")),
                line[3],
                line[3],
                "licensePlate",
                "",
                null,
                ""));
          }
          return (new TripWaypoint(
              EventType.WAYPOINT,
              TripWaypoint.eventSubtypeMessageDeviceIDMESSAGE,
              this.provider,
              Double.parseDouble(line[4]),
              Double.parseDouble(line[5]),
              toLocalDate(Long.parseLong(line[1] + "000")),
              Instant.ofEpochMilli(Long.parseLong(line[1] + "000")),
              line[3],
              line[3],
              "licensePlate",
              "",
              null,
              ""));

        case "data":
          Map<String, EntryExitActivity> doorEntryExitActivityPc = new HashMap<>();
          doorEntryExitActivityPc.put(
              line[10],
              new EntryExitActivity(Double.parseDouble(line[11]), Double.parseDouble(line[12])));
          if (line[15].equals("") || line[16].equals("")) {
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypePeopleCount,
                this.provider,
                null,
                null,
                toLocalDate(Long.parseLong(line[14] + "000")),
                Instant.ofEpochMilli(Long.parseLong(line[14] + "000")),
                line[2],
                line[2],
                "licensePlate",
                line[3],
                doorEntryExitActivityPc,
                line[5]));
          } else {
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypePeopleCount,
                this.provider,
                Double.parseDouble(line[15]),
                Double.parseDouble(line[16]),
                toLocalDate(Long.parseLong(line[14] + "000")),
                Instant.ofEpochMilli(Long.parseLong(line[14] + "000")),
                line[2],
                line[2],
                "licensePlate",
                line[3],
                doorEntryExitActivityPc,
                line[5]));
          }
        default:
          break;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private LocalDate toLocalDate(long milliSeconds) {
    return Instant.ofEpochMilli(milliSeconds).atZone(timeZone).toLocalDate();
  }
}
