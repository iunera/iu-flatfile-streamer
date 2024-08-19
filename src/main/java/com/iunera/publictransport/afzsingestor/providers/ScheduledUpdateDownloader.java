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

import io.awspring.cloud.s3.S3PathMatchingResourcePatternResolver;
import io.awspring.cloud.s3.S3ProtocolResolver;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.WritableResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

@Service
public class ScheduledUpdateDownloader {

  @Value("s3://${fahrbar.countingdata.provider}/flatfile-streamer.state")
  private WritableResource statusofreadfilesresource;

  @Value("${fahrbar.countingdata.provider}")
  private String provider;

  @Value("${fahrbar.countingdata.filenamepattern}")
  private String filenamepattern;

  private Map<String, String> readfiles = new HashMap<>();

  @Autowired ResourceProcessor resourceProcessor;

  @Scheduled(fixedDelayString = "${resolveAndLoad.fixedDelay.in.milliseconds}")
  public void resolveAndLoad() throws Exception {
    // use a list to sort and prune
    Resource[] filesinfolder =
        this.resourcePatternResolver.getResources("s3://" + provider + "/" + filenamepattern);
    if (filesinfolder.length == 0) return;

    // filter the files for the ones which have been read already
    List<Resource> toprocessList = new ArrayList<>(filesinfolder.length);
    for (Resource r : filesinfolder) {
      if (!readfiles.containsKey(r.getFilename())) {
        toprocessList.add(r);
      }
    }
    toprocessList.sort(
        (o1, o2) -> {
          try {
            return Long.compare(o1.lastModified(), o2.lastModified());
          } catch (IOException e) {
            e.printStackTrace();
          }
          return 0;
        });

    // read the files
    for (Resource r : toprocessList) {
      resourceProcessor.process(r);
      recordProcessedFile(r);
    }
  }

  private ResourcePatternResolver resourcePatternResolver;

  @Autowired
  public void initResolver(S3Client s3Client) {
    DefaultResourceLoader loader = new DefaultResourceLoader();
    loader.addProtocolResolver(new S3ProtocolResolver());
    this.resourcePatternResolver =
        new S3PathMatchingResourcePatternResolver(
            s3Client, new PathMatchingResourcePatternResolver(loader));
  }

  /* Store that a file was processed */
  private void recordProcessedFile(Resource file) {
    readfiles.put(file.getFilename(), Instant.now().toString());
    try {
      persistState();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void persistState() throws IOException {
    OutputStream out = statusofreadfilesresource.getOutputStream();
    OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
    for (Map.Entry<String, String> str : readfiles.entrySet()) {
      osw.write(str.getKey() + ";" + str.getValue() + "\n");
    }
    osw.close();
  }

  @Autowired
  private void initState() throws IOException {

    readfiles.putAll(
        new BufferedReader(
                new InputStreamReader(
                    statusofreadfilesresource.getInputStream(), StandardCharsets.UTF_8))
            .lines()
            .collect(
                Collectors.toMap(
                    e -> e.split(";")[0],
                    e -> {
                      if (e.split(";").length > 1) return e.split(";")[1];
                      return "UNKNOWNPROCESSINGTIME";
                    })));
  }
}
