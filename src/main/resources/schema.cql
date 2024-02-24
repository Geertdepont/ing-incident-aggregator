CREATE TABLE dailydowntime (
    date DATE,
    assetName TEXT,
    totalIncidents INT,
    totalDowntimeSeconds INT,
    rating INT,
    PRIMARY KEY ((assetName), date)
) WITH CLUSTERING ORDER BY (date DESC)
AND INDEX date_index (date);

CREATE TABLE incident (
  startDate timestamp,
  assetName text,
  endDate timestamp,
  severity int,
  processed boolean,
  PRIMARY KEY ((assetName), startDate)
) WITH CLUSTERING ORDER BY (startDate DESC)
AND INDEX processed_index (processed);