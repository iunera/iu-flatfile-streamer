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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoagCsvEventToRideEventMapper {

  private static final long serialVersionUID = 3788658302046923662L;

  private String provider;
  ZoneId timeZone = ZoneId.of("Europe/Berlin");

  static DateTimeFormatter datetimeformattertime = DateTimeFormatter.ofPattern("HH:mm:ss");
  static DateTimeFormatter datetimeformatterDay = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public StoagCsvEventToRideEventMapper(String provider, ZoneId timeZone) {
    this.timeZone = timeZone;
    this.provider = provider;
  }

  public TripWaypoint map(String value) throws Exception {
    String line[] = value.split("\t", -1);
    String techVehicleNo = "";
    try {
      switch (line[0]) {
          // Technical vehicle no
        case "001":
          // just capturing vehicle number and no other details
          // the latitude/longitude is same and out of range eg:2147483645 after
          // calculation:596.5232347222222
          techVehicleNo = line[16];
          break;
          // line info
        case "011":
          if (line[9].equals("") || line[10].equals("")) {
            return (new TripWaypoint(
                EventType.MESSAGE,
                TripWaypoint.eventSubtypeMessageDeviceIDMESSAGE,
                this.provider,
                null,
                null,
                LocalDate.parse(line[2], datetimeformatterDay),
                toDate(line[3], line[2]),
                techVehicleNo,
                techVehicleNo,
                null,
                line[18],
                null,
                null));
          } else {
            return (new TripWaypoint(
                EventType.MESSAGE,
                TripWaypoint.eventSubtypeMessageDeviceIDMESSAGE,
                this.provider,
                ((Double.parseDouble(line[10]) / 1000) / 3600),
                ((Double.parseDouble(line[9]) / 1000) / 3600),
                LocalDate.parse(line[2], datetimeformatterDay),
                toDate(line[3], line[2]),
                techVehicleNo,
                techVehicleNo,
                null,
                line[18],
                null,
                null));
          }
          // stop zone info
        case "031":
          if (line[9].equals("") || line[10].equals("")) {
            return (new TripWaypoint(
                EventType.WAYPOINT,
                TripWaypoint.eventSubtypeStopZone,
                this.provider,
                null,
                null,
                LocalDate.parse(line[2], datetimeformatterDay),
                toDate(line[3], line[2]),
                techVehicleNo,
                techVehicleNo,
                null,
                null,
                null,
                line[20]));
          } else {
            return (new TripWaypoint(
                EventType.WAYPOINT,
                TripWaypoint.eventSubtypeStopZone,
                this.provider,
                ((Double.parseDouble(line[10]) / 1000) / 3600),
                ((Double.parseDouble(line[9]) / 1000) / 3600),
                LocalDate.parse(line[2], datetimeformatterDay),
                toDate(line[3], line[2]),
                techVehicleNo,
                techVehicleNo,
                null,
                null,
                null,
                line[20]));
          }
          // door
        case "041":
          if (line[9].equals("") || line[10].equals("")) {
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypeDoorCloseSTOPEVENT,
                this.provider,
                null,
                null,
                LocalDate.parse(line[2], datetimeformatterDay),
                toDate(line[3], line[2]),
                techVehicleNo,
                techVehicleNo,
                null,
                null,
                null,
                null));
          } else {
            if (line[9].length() <= 8 && line[10].length() <= 9) {
              return (new TripWaypoint(
                  EventType.PLANNEDSTOP,
                  TripWaypoint.eventSubtypeDoorCloseSTOPEVENT,
                  this.provider,
                  ((Double.parseDouble(line[10]) / 1000) / 3600),
                  ((Double.parseDouble(line[9]) / 1000) / 3600),
                  LocalDate.parse(line[2], datetimeformatterDay),
                  toDate(line[3], line[2]),
                  techVehicleNo,
                  techVehicleNo,
                  null,
                  null,
                  null,
                  null));
            } else break;
          }
          // passenger change
        case "042":
          // passenger change over
          String ins = "0000";
          String outs = "0000";
          String regex = "[0-9]+";
          Matcher m = Pattern.compile(regex).matcher(line[17]);
          if (m.matches()) {
            ins = line[17].substring(0, 2);
            outs = line[17].substring(2);
          }

          Map<String, EntryExitActivity> doorEntryExitActivityPc = new HashMap<>();
          doorEntryExitActivityPc.put(
              line[16], new EntryExitActivity(Double.parseDouble(ins), Double.parseDouble(outs)));

          if (line[9].equals("") || line[10].equals("")) {
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypePeopleCount,
                this.provider,
                null,
                null,
                LocalDate.parse(line[2], datetimeformatterDay),
                toDate(line[3], line[2]),
                line[18],
                line[18],
                null,
                null,
                doorEntryExitActivityPc,
                null));
          } else {
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypePeopleCount,
                this.provider,
                ((Double.parseDouble(line[10]) / 1000) / 3600),
                ((Double.parseDouble(line[9]) / 1000) / 3600),
                LocalDate.parse(line[2], datetimeformatterDay),
                toDate(line[3], line[2]),
                line[18],
                line[18],
                null,
                null,
                doorEntryExitActivityPc,
                null));
          }

        default:
          break;
      }

    } catch (Exception e) {
      e.printStackTrace();
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
