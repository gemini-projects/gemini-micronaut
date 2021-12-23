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
A schema example can be found here [repository ufficiale](https://github.com/gemini-projects/gemini-micronaut/tree/main/starters/examples/product-sample).

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
Now we can start Gemini, exposing our schema APIs.

```shell script
docker run -p 8080:8080 \
-e GEMINI_SCHEMAS=/schemas/product_schema.yaml \
-e GEMINI_MONGODB_URL="mongodb+srv://_mongo_user:_mongo_pwd_@__path__.mongodb.net/db?retryWrites=true&w=majority" \
-e GEMINI_MONGODB_DB=starter \
-v $(pwd)/product_schema.yaml:/schemas/product_schema.yaml:ro \
aat7/gemini-micronaut-mongodb-restapi
```

## Step 3 - Use the APIs
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

// TODO EXAMPLE: retrieve paginated and filtered data 

### PUT e DELETE
If you want to modify or delete and entity record you can use the PUT or the DELETE method.
