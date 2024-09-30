# IBM Knowledge Catalog workflow and IBM Databand integration demo

The code sample demonstrates how IKC workflows can be integrated with Databand to enable
observability features. The code reads workflow details from IKC and creates a Databand
pipeline view of the workflow instance and its user tasks. A workflow type in IKC gets
represented as a "project" in Databand, and a workflow configuration gets represented
as a "pipeline". This enables the definition of alerts in Databand for each workflow configuration.

An existing governance artifact management workflow is required.

Last verified with IBM Knowledge Catalog 5.0.3 and IBM Databand 1.0.104.3.

To run the demo, create a `config.properties` file in the root folder with this content:

```
# IKC host URL, e.g. https://cpd-wkc.apps.ikchost.com
IKC_HOST=

# IKC Basic auth token as Base64 string, e.g. YWRtaW46TmJyOHQ2QTdFZGN0
IKC_BASIC_AUTH_TOKEN=

# The ID of the governance artifact managed by the sample workflow 
ARTIFACT_ID=

# The version ID of the governance artifact
ARTIFACT_VERSION_ID=

# The Databand instance bulk event URL, e.g. https://databand-host.databand.ai/api/v1/tracking/open-lineage/881c3448-2f14-11ef-b811-2e4d00527841/events/bulk
DATABAND_URL=

# The Databand bearer token
DATABAND_TOKEN=
```
and provide the values.

Then run

`
mvn clean compile assembly:single
`

`
java -jar target/ikc-workflow-databand-1.0-SNAPSHOT-jar-with-dependencies.jar
`
