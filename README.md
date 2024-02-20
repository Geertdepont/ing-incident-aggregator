# ING assignment
## Incident aggregator
- This project receives a csv as input from a form-data.
http://localhost:8080/api/upload
- Every day at 04:00 AM the input from the CSV is being aggregated
- The aggregated results can be fetched per day with the following URL:
http://localhost:8080/api/dashboard?date=YYYY-MM-DD
- This project can be tested with the tests from the test folder

## TODO 
Since I only had two days to finish this project, here are the todo's:
- Use spring batch to handle large data. The job can be performed concurrently as well.
- Since it would take me too long to set up Kafka, I created a simple upload endpoint.
This should be perceived as the input stream. 
- Rate limiter 
- Add more testcases
- Use caching (If you know the csv will be downloaded today, you can store it in the cache)
- Add logging
- Overlapping incident times (If two incidents of the same asset would overlap, these are now calculated twice. So in theory you can have a downtime bigger than 100% of the day)
- Add authentication (if necessary)
- Properly validate input CSV (if necessary)
- Clean up database after aggregating? (This is not done now)
- When a new CSV is uploaded and has similar dates as the current dates and the same asset names. Then the respective downtime will be overriden. 
<b> Since I did not know if a new CSV has overlapping dates, I did not implement this for the MVP.  </b> To fix this issue the corresponding downtime just needs to be retrieved and updated. 
- I did not see any overlapping dates in the csv, so I also did not implement this for the MVP.


## After discussion
- Date in csv's is always in the following format:
  "4/1/2019 8:10" 
- GET http://localhost:8080/api/dashboard?date=2019-04-01 <br/>
  will return the aggregated result (as an output.csv) of the incidents of 2019-04-1 <br/>
  in the following format:

  Asset Name, Total Incidents, Total Downtime, Rating <br/>
  CRM, 6, 18.2639%, 160
  (Total Downtime being a percentage of the day)

## Setup
1. Setup a cassandra database in docker:
```shell
docker run -p 9042:9042 --rm --name cassandra -d cassandra:VERSION
```

2. Create two keyspaces in cassandra:
- sling_test
- sling_dev
```cmd
CREATE KEYSPACE sling_test WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
```

```cmd
CREATE KEYSPACE sling_dev WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };
```

3. Build the project: `./mvnw clean package`
4. Run the application: `java -jar target/ing-incident-aggregator.jar`
5. Run testcases : `./mvnw test`

