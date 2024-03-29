# Squadlist

Availability system website.

Provided for reference proposes only.


## Localisation

The availability system can be localised for different languages and governing bodies.
Users have previously provided French and Dutch translations.

To provide an alternative translation, copy the file [messages.properties](src/main/resources/messages.properties) and translate the text fragments.
Contact us (via the link in the Admin screen) and we can install your translation.

To implement a different governing bodies points and ranking system, provide an alternative implementation of the GoverningBody interface.


## Running locally

This is a Java/ Kotlin Spring Boot application. It can be run locally using the Maven plugin.

```
mvn spring-boot:run
```

## Building the Squadlist API Swagger client

This UI app talks to the Squadlist API (which is [documented in OpenAPI format](https://api.squadlist.app/openapi.json)) using a client jar generated from the OpenAPI definition.
When a new end point is added to the API we need to regenerate the client jar before we can call that end point.

```
java -jar modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate -i https://api.squadlist.app/openapi.json -l java --group-id uk.co.squadlist.client.swagger --api-package uk.co.squadlist.client.swagger.api --model-package uk.co.squadlist.model.swagger --library=okhttp-gson -o /tmp/squadlist-api-swagger-client -DhideGenerationTimestamp=true,dateLibrary=joda --artifact-version=2022072902 --artifact-id=squadlist-api-swagger-client
```

## Building container images

We use Google Cloud Build to produce container images.
To run this build locally:

```
gcloud components install cloud-build-local
cloud-build-local --config=cloudbuild.yaml --dryrun=false --push=false .
```

