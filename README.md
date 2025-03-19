# iu-flatfile-streamer
Reads people counter files with different data formats (see eventmappers package) and transforms it into a data stream.
Reason to do that is that sometimes there are just the files and sometimes there can be real time streaming data. 
We unify the data format by transforming everything into a data stream to ensure homogeneous processing.
Please beware when you add your own data format to not join all formats on the data stream just after creating a stream, because often different formats lack different properties (e.g. one lacks line information the next one lacks delay information and similar) what leads that different post-processing/enrichment steps are required to have all streams populated with equal properties for further processing in a unified way (see the linematching and enrichment projects how missing properties can be filled.).

#HOW To USE
Start the job (BlockStorageIngestorApplication) with the different parameters:
- inputFolder = the folder where the Data Files are searched
- timeZone = the time zone where the transport provider operates - default: Europe/Berlin
- provider = the provider where the data is from to track the origin in case different providers share the same data. Default: "defaultProvider";
- kafkaBroker = Default: "localhost:9092";
- outputTopic = The output topic: "iu-fahrbar-prod-ingest-mgelectronics-flatfilecountdata-v1";
- maxWaitingTime = Waiting time for input file ordering in seconds: Default:"5";

# Customize/How to add another data format
In order to add another data format one can just use one of the event mapper examples and use it out of the box or use the event mappers as a template to create a data mapper for an own format.

Subsequently, one can use the Resource Processor as a template to create a configurable processor reading files form storage and using the suitable mapper.

# Required VM Arguments
--add-opens java.base/java.lang=ALL-UNNAMED
--add-opens java.base/java.util=ALL-UNNAMED
--add-opens java.base/java.io=ALL-UNNAMED
## Required program arguments
-Dexecution.runtime-mode=BATCH 


# General
* Deploys in the fahrbar-common namespace
* Mounts the buckets to
  * /buckets/XXX READONLY
  * /buckets/XXX READONLY
  * /buckets/druidimport READWRITE
* Checkpoint/Savepoint Dir
  * /checkpoints/flink/externalized-checkpoints
  * /checkpoints/flink/savepoints


# Building local

## Docker
```
docker build --no-cache -t iu-flatfile-streamer:local -f iu-occupancy-flatfile-streamer/Dockerfile .
```

# License

We choose fair [code, fair work, fair payment, open  collaboration](https://www.license-token.com)

## [Open Compensation Token License](https://github.com/open-compensation-token-license/license/blob/main/LICENSE.md)

```
Licensed under the OPEN COMPENSATION TOKEN LICENSE (the "License").

You may not use this file except in compliance with the License.

You may obtain a copy of the License at
<https://github.com/open-compensation-token-license/license/blob/main/LICENSE.md>

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expressed or implied.
See the License for the specific language governing permissions and
limitations under the License.

@octl.sid: 1b6f7a5d-8dcf-44f1-b03a-77af04433496
```
* Why did we [choose the OCTL as alternative to the GNU Lesser General Public License v3](https://www.license-token.com/wiki/unveiling-gnu-lesser-general-public-license-v3-summary)?
* Why we [do NOT apply Apache 2.0 License](https://www.license-token.com/wiki/the-downside-of-apache-license-and-why-i-never-would-use-it)?
