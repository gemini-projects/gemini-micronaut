
```shell script
docker run -p 8080:8080 \
-e GEMINI_SCHEMAS=/schemas/product_schema.yaml \
-e GEMINI_MONGODB_URL="mongodb+srv://root:e4OEM5mXAGAHRMfHN7DP@cluster0.9thyu.mongodb.net/myFirstDatabase?retryWrites\=true&w\=majority" \
-e GEMINI_MONGODB_DB=starter \
-v $(pwd)/product_schema.yaml:/schemas/product_schema.yaml:ro \
gemini-micronaut-mongodb-restapi
```

```shell script
curl --request POST "http://localhost:8080/data/category" \
--header "Content-Type: application/json" \
-d '{"data": {"id": "tech-1",
        "description": "Technology"
    }
  }'
```

```shell script
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

```shell script
curl "http://localhost:8080/data/category"
```

```shell script
curl --request POST "http://localhost:8080/data/product" \
--header "Content-Type: application/json" \
-d '{"data": {
      "id": "iphone-13-red-128",
      "name": "Iphone 13",
      "description:": "Iphone 13 - Red - 128GB",
      "available": true,
      "status": "DRAFT",
      "regular_price": 899,
      "sale_price": 670,
      "categories": ["tech-1", "smartphone-1"]
      }
    }'
```