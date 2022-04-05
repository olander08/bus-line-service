# Bus line service

The number of stops a bus line has is calculated by using Trafiklab's API 
[SL Stops and lines v2.0](https://www.trafiklab.se/api/trafiklab-apis/sl/stops-and-lines-2/)
and by retrieving all instances of JourneyPatternPointOnLine-objects.

JourneyPatternPointOnLine objects may represent the mapping between a bus line and a bus stop,
and to determine if the object is related to a bus line StopPoint-objects are retrieved from
the API, where information regarding what type of stop it is, where StopAreaTypeCode=BUSSTERM 
designates bus stops.

### Setup

Set `trafiklab.stops-and-lines-2.api-key` to your Trafiklab API key used for authorizing requests to
SL Stops and lines v2.0 API.

### Notes

Redundant data storage in in mem persistance layer (Stops)

ExistsFromDate ignored

More graceful error handling

Log appender

Refresh data periodically

Caching

Monitoring