package com.iunera.publictransport.afzsingestor.providers;

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

import com.iunera.publictransport.afzsingestor.providers.eventmappers.MGEventToRideEventMapper;
import com.iunera.publictransport.domain.trip.TripWaypoint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// import java.util.logging.*;
@Service
public class ResourceProcessor {
  // private static Logger logger = LoggerFactory.getLogger(ResourceProcessor.class);

  MGEventToRideEventMapper rideeventMapper =
      new MGEventToRideEventMapper("PUBLICTRANSPORTPROVIDER", ZoneId.of("Europe/Berlin"));

  public void process(Resource r) throws Exception {
    if (r.getFilename().toLowerCase().contains("zip")) {
      InputStream is = r.getInputStream();
      // generating a temporary local copy of the zip file
      final File tmpFile = stream2file(is, r.getFilename() + Instant.now().toString());

      // then using java.util.zip.ZipFile for extracting the InputStream for the
      // specific file within the zip archive
      final ZipFile zipFile = new ZipFile(tmpFile);
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      List<ZipEntry> zipentries = new ArrayList<>();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        zipentries.add(entry);
      }

      zipentries.sort(
          new Comparator<ZipEntry>() {

            @Override
            public int compare(ZipEntry o1, ZipEntry o2) {

              return o1.getName().compareTo(o2.getName());
            }
          });

      for (ZipEntry entry : zipentries) {
        if (entry.getName().toLowerCase().endsWith("txt")) {
          InputStream filecontents = zipFile.getInputStream(entry);
          readfile(filecontents, zipFile.getName() + "-" + entry.getName());
        }
      }

    } else {
      // read normal text file
    }
  }

  public void readfile(InputStream in, String partition) throws Exception {
    String line;
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    try {
      while ((line = reader.readLine()) != null) {
        TripWaypoint mapped = rideeventMapper.map(line);
        if (mapped == null) {
          // TODO: log the not mappable lines
        } else {
          mapped.meta_ingestionPartition = partition;
          template.send(outputtopic, mapped);
        }
      }
    } catch (IOException e) {
      // do something, probably not a text file
      e.printStackTrace();
    }
  }

  @Value("${fahrbar.countingdata.outputtopic}")
  private String outputtopic;

  @Autowired private KafkaTemplate<String, TripWaypoint> template;

  private static File stream2file(InputStream in, String name) throws IOException {
    final File tempFile = File.createTempFile("tmpfile", ".zip");
    tempFile.deleteOnExit();
    try {
      FileOutputStream out = new FileOutputStream(tempFile);
      IOUtils.copy(in, out);
    } catch (Exception e) {
    }
    return tempFile;
  }
}
