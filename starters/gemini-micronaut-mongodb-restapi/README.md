# Gemini Micronaut MongoDB RESTAPI

This is a starter Gemini Micronaut project that use the basic features of Gemini and the
MongoDb driver.

## Usage Example

The easiest way to play with the starter, without coding anything is by using the docker
image.

However you can use this starter also as your entry point for the Gemini Framework
by using the lowcode features of Gemini combined with your code and custom controllers (it 
is a Micronaut Project).

## Step 1 - Define the schema (Domain Driven Design)
A simple schema example can be found [here](https://github.com/gemini-projects/gemini-micronaut/tree/main/starters/examples/product-sample).
It is something like:

```yaml
type: ENTITY
entity:
  name: CATEGORY
  lk: [id]
  fields:
    - name: id
      type: STRING
      required: true
    - name: description
      type: STRING

---

type: ENTITY
entity:
  name: PRODUCT
  lk: [id]
  fields:
    - name: id
      type: STRING
      required: true
    - name: name
      type: STRING
    - name: description
      type: STRING
    - name: available
      type: BOOL
    - name: status
      type: ENUM
      enums: [DRAFT, PENDING, PRIVATE, PUBLISH]
    - name: regular_price
      type: DOUBLE
    - name: sale_price
      type: DOUBLE
    - name: categories
      type: ARRAY
      array:
        type: ENTITY_REF
        entityRef:
          entity: CATEGORY
```

## Step 2 - Run the Docker Image
After defining the data schema, we can start Gemini, exposing all the REST APIs.

```shell script
docker run -p 8080:8080 \
-e GEMINI_SCHEMAS=/schemas/product_schema.yaml \
-e GEMINI_MONGODB_URL="mongodb+srv://_mongo_user:_mongo_pwd_@__path__.mongodb.net/db?retryWrites=true&w=majority" \
-e GEMINI_MONGODB_DB=starter \
-v $(pwd)/product_schema.yaml:/schemas/product_schema.yaml:ro \
aat7/gemini-micronaut-mongodb-restapi
```

* GEMINI_MONGODB_URL is the path of the MongoDB cluster (you can use MongoDb Atlas for an easy test)
* GEMINI_MONGODB_DB is the databse name inside the Mongo cluster
* GEMINI_SCHEMAS tells to Gemini where to pick the schema, and we use our mounted volume wit the schema file

## Step 3 - Use some basic REST APIs
Now we can use the common CRUD APIs calling the API pattern `/data/{entityName}`

### POST - Add some data
```shell
curl --request POST "http://localhost:8080/data/category" \
--header "Content-Type: application/json" \
-d '{"data": {"id": "tech-1",
        "description": "Technology"
    }
  }'
```

The output is something like that, where Gemini provides some useful metadata.

```json
{
  "status":"success",
  "data": {
    "description":"Technology",
    "id":"tech-1"
  },
  "meta": {
    "lastUpdateTimeUnix":1640185713731,
    "lastUpdateTimeISO":"2021-12-22T15:08:33.731Z",
  }
}
```

We can also add more data in a single step.
```shell
curl --request POST "http://localhost:8080/data/category" \
--header "Content-Type: application/json" \
-d '{"data": [{
                  "id": "smartphone-1",
                  "description": "Smartphone"
              },
              {
                  "id": "clothing-1",
                  "description": "Clothing"
              },
              {
                  "id": "beauty-1",
                  "description": "beauty"
              }]
  }'
```

### GET - Data

Calling the entity root with a GET `/data/{entityName}` we can get all its data. 
```shell
curl "http://localhost:8080/data/category"
```

Or just a single record proving its id `/data/{entityName}/{recordId}`
```shell
curl "http://localhost:8080/data/category/smartphone-1"
```

### PUT e DELETE
If you want to modify or delete and entity record you can use the PUT or the DELETE method.
Same url patter `/data/{entityName}/{recordId}` with different HTTP method.


## Step 4 - Count and Filter APIs

Let's add some other products. For example some Iphone versions.

```shell
curl --request POST "http://localhost:8080/data/product" \
--header "Content-Type: application/json" \
-d '{"data": [{
                  "id": "iphone-13-128-blue",
                  "name": "Iphone 13 128 GB Blue",
                  "description": "Apple Iphone 13 - Memory: 128 GB - Color: Blue",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 799,
                  "sale_price": 699,
                  "categories": ["tech-1", "smartphone-1"]
              },
              {
                  "id": "iphone-13-128-starlight",
                  "name": "Iphone 13 128 GB Starlight",
                  "description": "Apple Iphone 13 - Memory: 128 GB - Color: Starlight",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 799,
                  "sale_price": 699,
                  "categories": ["tech-1", "smartphone-1"]
              },
              {
                  "id": "iphone-13-128-midnight",
                  "name": "Iphone 13 128 GB Midnight",
                  "description": "Apple Iphone 13 - Memory: 128 GB - Color: Midnight",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 799,
                  "sale_price": 699,
                  "categories": ["tech-1", "smartphone-1"]
              },
                            {
                  "id": "iphone-13-128-pink",
                  "name": "Iphone 13 128 GB Pink",
                  "description": "Apple Iphone 13 - Memory: 128 GB - Color: Pink",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 799,
                  "sale_price": 699,
                  "categories": ["tech-1", "smartphone-1"]
              },
                            {
                  "id": "iphone-13-128-red",
                  "name": "Iphone 13 128 GB Red",
                  "description": "Apple Iphone 13 - Memory: 128 GB - Color: Product RED",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 799,
                  "sale_price": 699,
                  "categories": ["tech-1", "smartphone-1"]
              },
              {
                  "id": "iphone-13-256-blue",
                  "name": "Iphone 13 256 GB Blue",
                  "description": "Apple Iphone 13 - Memory: 256 GB - Color: Blue",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 899,
                  "sale_price": 799,
                  "categories": ["tech-1", "smartphone-1"]
              },
              {
                  "id": "iphone-13-256-starlight",
                  "name": "Iphone 13 256 GB Starlight",
                  "description": "Apple Iphone 13 - Memory: 256 GB - Color: Starlight",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 899,
                  "sale_price": 799,
                  "categories": ["tech-1", "smartphone-1"]
              },
              {
                  "id": "iphone-13-256-midnight",
                  "name": "Iphone 13 256 GB Midnight",
                  "description": "Apple Iphone 13 - Memory: 256 GB - Color: Midnight",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 899,
                  "sale_price": 799,
                  "categories": ["tech-1", "smartphone-1"]
              },
                            {
                  "id": "iphone-13-256-pink",
                  "name": "Iphone 13 256 GB Pink",
                  "description": "Apple Iphone 13 - Memory: 256 GB - Color: Pink",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 899,
                  "sale_price": 799,
                  "categories": ["tech-1", "smartphone-1"]
              },
                            {
                  "id": "iphone-13-256-red",
                  "name": "Iphone 13 256 GB Red",
                  "description": "Apple Iphone 13 - Memory: 256 GB - Color: Product RED",
                  "available": true,
                  "status": "PUBLISH",
                  "regular_price": 899,
                  "sale_price": 799,
                  "categories": ["tech-1", "smartphone-1"]
              }]
  }'
```

```json
{
    "status": "success",
    "data": [
        {
            "regular_price": 799.0,
            "name": "Iphone 13 128 GB Blue",
            "available": true,
            "description": "Apple Iphone 13 - Memory: 128 GB - Color: Blue",
            "id": "iphone-13-128-blue",
            "categories": [
                "tech-1",
                "smartphone-1"
            ],
            "sale_price": 699.0,
            "status": "PUBLISH"
        },
        // ....
        // ....
        // ....
        {
            "regular_price": 899.0,
            "name": "Iphone 13 256 GB Red",
            "available": true,
            "description": "Apple Iphone 13 - Memory: 256 GB - Color: Product RED",
            "id": "iphone-13-256-red",
            "categories": [
                "tech-1",
                "smartphone-1"
            ],
            "sale_price": 799.0,
            "status": "PUBLISH"
        }
    ],
    "meta": {
        "lastUpdateTimeUnix": 1640703311085,
        "lastUpdateTimeISO": "2021-12-28T14:55:11.085Z",
        "elapsedTime": "479ms"
    }
}
```

### Count records

To count record simply use a GET with the url pattern `/entity/{entityName}/recordCounts`. For example:
 
```shell
curl "http://localhost:8080/entity/product/recordCounts"
```

```json
{
    "status": "success",
    "data": {
        "count": 10
    },
    "meta": {
        "elapsedTime": "48ms"
    }
}
```

### Filters

Let's see now some interesting and very common stuff. Let's introduce the entity filter API. Of course we can filter
according to the field data type and the filter is also supported for the count API.

#### Strings
For example the `name` of the entity `Product` is a string. The monogDB driver is able to filter by `CONTAINS` and `EQUALS`.

```shell
# Count - Iphone 13 128 GB Blue - NB: the space is %20 in curl url
curl "http://localhost:8080/entity/product/recordCounts?name=Iphone%2013%20128%20GB%20Blue"
{"status":"success","data":{"count":1},"meta":{"elapsedTime":"38ms"}}

# Get Record - Iphone 13 128 GB Blue
curl "http://localhost:8080/data/product?name=Iphone%2013%20128%20GB%20Blue"
{"status":"success","data":[{"regular_price":799.0,"name":"Iphone 13 128 GB Blue","available":true,"description":"Apple Iphone 13 - Memory: 128 GB - Color: Blue","id":"iphone-13-128-blue","categories":["tech-1","smartphone-1"],"sale_price":699.0,"status":"PUBLISH"}],"meta":{"elapsedTime":"49ms"}}
```

To use the `CONTAINS` the syntax become `/data/{entityName}?fieldName[CONTAINS]=value`. For example we can count
all the records that contains *256* in the `name` field and retrieve all the records that contains  *Blue*

```shell
# Count - Name CONTAINS 256 - result is 5
curl "http://localhost:8080/entity/product/recordCounts?name[CONTAINS]=256"
{"status":"success","data":{"count":5},"meta":{"elapsedTime":"40ms"}}

# Get Records - Name CONTAINS 256 - result is 2 records
curl "http://localhost:8080/data/product?name[CONTAINS]=Blue"
{"status":"success","data":[{"regular_price":799.0,"name":"Iphone 13 128 GB Blue","available":true,"description":"Apple Iphone 13 - Memory: 128 GB - Color: Blue","id":"iphone-13-128-blue","categories":["tech-1","smartphone-1"],"sale_price":699.0,"status":"PUBLISH"},{"regular_price":899.0,"name":"Iphone 13 256 GB Blue","available":true,"description":"Apple Iphone 13 - Memory: 256 GB - Color: Blue","id":"iphone-13-256-blue","categories":["tech-1","smartphone-1"],"sale_price":799.0,"status":"PUBLISH"}],"meta":{"elapsedTime":"41ms"}}
```

#### Numbers
The `regular_price` field is a *DOUBLE*. For simplicity let's see only some count example, but of course it works also for data retrieval. 

Since it is numeric type we have the equals of course but also numeric operators like `> < >= <=` namely
`GT LT GTE LTE`. We have 5 products with `regular_price` of 799 and 5 products with `regular_price` of 899.

```shell
# 10 products > 700
curl "http://localhost:8080/entity/product/recordCounts?regular_price[GT]=700"
{"status":"success","data":{"count":10},"meta":{"elapsedTime":"40ms"}}

# 5 products > 799 
curl "http://localhost:8080/entity/product/recordCounts?regular_price[GT]=799"
{"status":"success","data":{"count":5},"meta":{"elapsedTime":"40ms"}}

# 10 production >= 799 
curl "http://localhost:8080/entity/product/recordCounts?regular_price[GTE]=799"
{"status":"success","data":{"count":10},"meta":{"elapsedTime":"39ms"}}
```

Of course it is possible to have multiple filters, both on different and  the same field. So we can also filter 
by intervals or make advanced search with the implicit `AND` operator.

```shell
# 5 products with 750 < regular_price < 850
curl "http://localhost:8080/entity/product/recordCounts?regular_price[GT]=750&regular_price[LT]=850"
{"status":"success","data":{"count":5},"meta":{"elapsedTime":"44ms"}}

# 1 products with 750 < regular_price < 850 and name containing 
curl "http://localhost:8080/entity/product/recordCounts?regular_price[GT]=750&regular_price[LT]=850&name[CONTAINS]=Blue"
{"status":"success","data":{"count":1},"meta":{"elapsedTime":"39ms"}}
```

#### Booleans
The `available` fields is obviously a *BOOL*. Here some examples: 

```shell
# 10 products available
curl "http://localhost:8080/entity/product/recordCounts?available=true"
{"status":"success","data":{"count":10},"meta":{"elapsedTime":"39ms"}}

# 0 products not available
curl "http://localhost:8080/entity/product/recordCounts?available=false"
{"status":"success","data":{"count":0},"meta":{"elapsedTime":"42ms"}}
```
