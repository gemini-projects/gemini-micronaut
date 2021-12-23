# Product Schema Example

You can use this schema to test the Gemini Starters.

### Example - Gemini Mongodb starter

We are using the Gemini Docker starter just to load the product schema and create its REST APIs.

In the example we just use the MongoDB Atlas database.

```shell script
docker run -p 8080:8080 \
-e GEMINI_SCHEMAS=/schemas/product_schema.yaml \
-e GEMINI_MONGODB_URL="mongodb+srv://_mongo_user:_mongo_pwd_@__path__.mongodb.net/db?retryWrites=true&w=majority" \
-e GEMINI_MONGODB_DB=starter \
-v $(pwd)/product_schema.yaml:/schemas/product_schema.yaml:ro \
aat7/gemini-micronaut-mongodb-restapi
```