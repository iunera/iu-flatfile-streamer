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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MGEventToRideEventMapper {
  public static final long serialVersionUID = -418287106672410913L;

  public String provider;
  public ZoneId timeZone = ZoneId.of("Europe/Berlin");
  private static final Logger LOG = LoggerFactory.getLogger(MGEventToRideEventMapper.class);

  public MGEventToRideEventMapper(String provider, ZoneId timeZone) {
    this.timeZone = timeZone;
    this.provider = provider;
  }

  public TripWaypoint map(String value) throws Exception {
    DateTimeFormatter datetimeformatterDay = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    String line[] = value.split(";");

    try {
      // Very important also map the ingestionPartition (partition) and the version/serial version
      // event!!
      switch (line[0]) {
        case "V":
          return (new TripWaypoint(
              EventType.MESSAGE,
              TripWaypoint.eventSubtypeMessageDeviceIDMESSAGE,
              this.provider,
              null,
              null,
              LocalDate.parse(line[1], datetimeformatterDay),
              toDate(line[2], line[1]),
              line[4].replace("\"", ""),
              line[3],
              null,
              null,
              null,
              null));

        case "GP":
          // I think the substring is wrong for GPS coordinates
          return (new TripWaypoint(
              EventType.WAYPOINT,
              TripWaypoint.eventSubtypeGPSUpdateWAYPOINT,
              this.provider,
              Double.parseDouble(line[3].substring(1)),
              Double.parseDouble(line[4].substring(1)),
              LocalDate.parse(line[1], datetimeformatterDay),
              toDate(line[2], line[1]),
              null,
              null,
              null,
              null,
              null,
              null));

        case "PC":
          Map<String, EntryExitActivity> doorEntryExitActivityPc = new HashMap<>();
          doorEntryExitActivityPc.put(line[5], new EntryExitActivity());
          if (line[3].equals("?") || line[4].equals("?")) {
            return (new TripWaypoint(
                EventType.WAYPOINT,
                TripWaypoint.eventSubtypeDoorCloseSTOPEVENT,
                this.provider,
                null,
                null,
                LocalDate.parse(line[1], datetimeformatterDay),
                toDate(line[2], line[1]),
                null,
                null,
                null,
                null,
                doorEntryExitActivityPc,
                null));
          } else
            return (new TripWaypoint(
                EventType.WAYPOINT,
                TripWaypoint.eventSubtypeDoorCloseSTOPEVENT,
                this.provider,
                Double.parseDouble(line[3].substring(1)),
                Double.parseDouble(line[4].substring(1)),
                LocalDate.parse(line[1], datetimeformatterDay),
                toDate(line[2], line[1]),
                null,
                null,
                null,
                null,
                doorEntryExitActivityPc,
                null));

        case "PO":
          Map<String, EntryExitActivity> doorEntryExitActivityPo = new HashMap<>();
          doorEntryExitActivityPo.put(line[5], new EntryExitActivity());
          if (line[3].equals("?") || line[4].equals("?")) {
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypeDoorOpenSTOPEVENT,
                this.provider,
                null,
                null,
                LocalDate.parse(line[1], datetimeformatterDay),
                toDate(line[2], line[1]),
                "",
                null,
                null,
                null,
                doorEntryExitActivityPo,
                null));
          } else
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypeDoorOpenSTOPEVENT,
                this.provider,
                Double.parseDouble(line[3].substring(1)),
                Double.parseDouble(line[4].substring(1)),
                LocalDate.parse(line[1], datetimeformatterDay),
                toDate(line[2], line[1]),
                null,
                null,
                null,
                null,
                doorEntryExitActivityPo,
                null));

        case "PP":
          Map<String, EntryExitActivity> doorEntryExitActivityPp = new HashMap<>();
          doorEntryExitActivityPp.put(
              line[5],
              new EntryExitActivity(Double.parseDouble(line[6]), Double.parseDouble(line[7])));
          if (line[3].equals("?") || line[4].equals("?")) {
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypePeopleCount,
                this.provider,
                null,
                null,
                LocalDate.parse(line[1], datetimeformatterDay),
                toDate(line[2], line[1]),
                null,
                null,
                null,
                null,
                doorEntryExitActivityPp,
                null));
          } else
            return (new TripWaypoint(
                EventType.PLANNEDSTOP,
                TripWaypoint.eventSubtypePeopleCount,
                this.provider,
                Double.parseDouble(line[3].substring(1)),
                Double.parseDouble(line[4].substring(1)),
                LocalDate.parse(line[1], datetimeformatterDay),
                toDate(line[2], line[1]),
                null,
                null,
                null,
                null,
                doorEntryExitActivityPp,
                null));
        case "#": // comment skip the line
          break;
        case "W": // comment skip the line
          break;
        case "IE": // comment skip the line
          break;
        case "IS": // comment skip the line
          break;
        case "PV": // comment skip the line
          break;
        case "PS": // comment skip the line
          break;
        case "GS": // comment skip the line
          break;
        case "WS": // comment skip the line
          break;
        case "PDC": // comment skip the line
          break;
        default:
          LOG.warn(value);
          break;
      }
    } catch (Exception e) {
      LOG.error("line or file not parseable", e);
    }

    return null;
  }

  // see why we need to define the DateTimeFormatter multiple times here:
  // https://github.com/JodaOrg/joda-time/issues/358
  private Instant toDate(String time, String day) {
    DateTimeFormatter datetimeformatterDay = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    DateTimeFormatter datetimeformattertime = DateTimeFormatter.ofPattern("HH:mm:ss");

    return LocalDate.parse(day, datetimeformatterDay)
        .atStartOfDay()
        .plusSeconds(LocalTime.parse(time, datetimeformattertime).toSecondOfDay())
        .atZone(this.timeZone)
        .toInstant();
  }
}
