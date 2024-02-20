CREATE TABLE dailydowntime (
    date DATE,
    assetName TEXT,
    totalIncidents INT,
    totalDowntimeSeconds INT,
    rating INT,
    PRIMARY KEY ((date), assetName)
);

CREATE TABLE incident (
  startDate timestamp,
  assetName text,
  endDate timestamp,
  severity int,
  processed boolean,
  PRIMARY KEY ((assetName), startDate)
) WITH CLUSTERING ORDER BY (startDate DESC)
AND INDEX processed_index (processed);