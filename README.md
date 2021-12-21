# Gemini Micronaut

Gemini Micronaut is the backend part of the Gemini Framework. It's built on top of Micronaut providing
an easy way to create powerful ***REST APIs*** using a ***Domain Driven Design*** approach.

## How it works
The starting point of Gemini is the *Gemini Schema*, by using a *Domain Driven Design* approach developers can
define the data fields and type for the REST APIs and Gemini does the rest.

### How to start
Gemini Framework is in the early stages of development. The easiest way to start is by running one of the Gemini
starters or by using the starters docker images.

- [MongoDB Starter](./starters/gemini-micronaut-mongodb-restapi)


### Packages

*Gemini Micronaut* is made of different packages that you can combine together accordingly to your needs.

For example you can use the out of the box data drivers or write your own code to store data where and how you want.

- ***core*** is the main part of the framework, that implments the REST APIs common interface and loads the DDD schema
- ***auth*** to add authentication to the APIs
- ***gcp-firebase*** driver to store API data by using firebase
- ***mongodb*** driver to store API data by using MongoDB
- ***adminspa*** simple package that provide an easy schema to build a full Admin APP backend (menus, settings, users, and so on..)

## License

*Gemini Micronaut* is [MIT licensed](./LICENSE).
