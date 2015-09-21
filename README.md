# Quotient Filter

A Scala project implementing a Quotient Filter for use as a probalistic data store.

## Usage

### Create new quotient filter

`GET /new/<quotient_filter_id>`

Returns back with the quotient_filter_id on success

### Insert Value

`GET /insert/<quotient_filter_id>/<value>`

value must be of form UUID1

Returns back the number of times that value has been inserted on success

### Query Value

`GET /insert/<quotient_filter_id>/<value>`

value must be of form UUID1

Returns back the number of times that value has been inserted on success

## Compile

`sbt compile`

## Tests

`sbt test`

## Run

`sbt run`
