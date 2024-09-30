/*
 * Copyright IBM Corp. 2024
 *
 * The following sample of source code ("Sample") is owned by International
 * Business Machines Corporation or one of its subsidiaries ("IBM") and is
 * copyrighted and licensed, not sold. You may use, copy, modify, and
 * distribute the Sample in any form without payment to IBM, for the purpose of
 * assisting you in the development of your applications.
 *
 * The Sample code is provided to you on an "AS IS" basis, without warranty of
 * any kind. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES, EITHER EXPRESS OR
 * IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Some jurisdictions do
 * not allow for the exclusion or limitation of implied warranties, so the above
 * limitations or exclusions may not apply to you. IBM shall not be liable for
 * any damages you suffer as a result of using, copying, modifying or
 * distributing the Sample, even if IBM has been advised of the possibility of
 * such damages.
 */
package com.ibm.integration.ikc;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.integration.model.ikc.Configuration;
import com.ibm.integration.model.ikc.UserTask;
import com.ibm.integration.model.ikc.Workflow;
import com.ibm.integration.util.JsonParser;

public class DataRetriever {
    private final String USER_TASKS_ENDPOINT = "/v3/workflow_user_tasks";
    private final String WORKFLOWS_ENDPOINT = "/v3/workflows";
    private final String WORKFLOW_CONFIGURATIONS_ENDPOINT = "/v3/workflow_configurations";
    private final Connection connection;

    public DataRetriever(Connection connection) {
        this.connection = connection;
    }

    public Workflow getWorkflowById(String workflowId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.getInstance().getIkcHost() + WORKFLOWS_ENDPOINT + "/" + workflowId))
                .header("Authorization", "Bearer " + connection.getBearerToken())
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = sendRequest(request);
        JsonNode rootNode = JsonParser.createJsonFromString(response.body());

        JsonNode metadata = rootNode.path("metadata");
        Workflow workflow = new Workflow();
        workflow.setWorkflowId(workflowId);
        workflow.setCreatedAt(metadata.path("created_at").asText());
        workflow.setCompletedAt(metadata.path("completed_at").asText());
        workflow.setConfigurationId(metadata.get("configuration_id").asText());
        workflow.setName(rootNode.at("/entity/artifacts/0/metadata/name").asText());
        return workflow;
    }

    public Configuration getConfigurationById(String workflowConfigurationId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.getInstance().getIkcHost() + WORKFLOW_CONFIGURATIONS_ENDPOINT + "/"
                        + workflowConfigurationId))
                .header("Authorization", "Bearer " + connection.getBearerToken())
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = sendRequest(request);
        JsonNode rootNode = JsonParser.createJsonFromString(response.body());

        Configuration workflowConfiguration = new Configuration();
        JsonNode metadata = rootNode.path("metadata");
        workflowConfiguration.setConfigurationId(metadata.path("configuration_id").asText());
        workflowConfiguration.setName(metadata.path("name").asText());
        return workflowConfiguration;
    }

    public ArrayList<UserTask> getActiveTasks(String artifactId, String artifactVersionId)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.getInstance().getIkcHost() + USER_TASKS_ENDPOINT + "?artifact_id=" + artifactId
                        + "&version_id=" + artifactVersionId + "&workflow_type_id=governance_artifacts"))
                .header("Authorization", "Bearer " + connection.getBearerToken())
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = sendRequest(request);
        JsonNode rootNode = JsonParser.createJsonFromString(response.body());

        int totalCount = rootNode.get("total_count").asInt();

        ArrayList<UserTask> tasks = new ArrayList<>();
        for (int i = 0; i < totalCount; i++) {
            if (rootNode.at("/resources/" + i + "/entity/candidate_users").isEmpty()
                    && rootNode.at("/resources/" + i + "/entity/candidate_groupss").isEmpty()) {
                // skip tasks without assignees aka candidates
                continue;
            }
            UserTask task = new UserTask();
            task.setTaskId(rootNode.at("/resources/" + i + "/metadata/task_id").asText());
            task.setCreatedAt(rootNode.at("/resources/" + i + "/metadata/created_at").asText());
            task.setWorkflowId(rootNode.at("/resources/" + i + "/metadata/workflow_id").asText());
            task.setName(rootNode.at("/resources/" + i + "/entity/task_definition_key").asText());
            tasks.add(task);
        }
        return tasks;
    }

    public ArrayList<UserTask> getCompletedTasks(String artifactId, String artifactVersionId)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(Config.getInstance().getIkcHost() + USER_TASKS_ENDPOINT + "?artifact_id=" + artifactId
                        + "&version_id=" + artifactVersionId + "&workflow_type_id=governance_artifacts&completed=true"))
                .header("Authorization", "Bearer " + connection.getBearerToken())
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = sendRequest(request);

        JsonNode rootNode = JsonParser.createJsonFromString(response.body());

        int totalCount = rootNode.get("total_count").asInt();

        ArrayList<UserTask> tasks = new ArrayList<>();
        for (int i = 0; i < totalCount; i++) {
            UserTask task = new UserTask();
            task.setTaskId(rootNode.at("/resources/" + i + "/metadata/task_id").asText());
            task.setCreatedAt(rootNode.at("/resources/" + i + "/metadata").get("created_at").asText());
            task.setCompletedAt(rootNode.at("/resources/" + i + "/entity").get("completed_at").asText());
            task.setWorkflowId(rootNode.at("/resources/" + i + "/metadata").get("workflow_id").asText());
            task.setName(rootNode.at("/resources/" + i + "/entity/task_definition_key").asText());
            tasks.add(task);
        }
        return tasks;
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = connection.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        Connection.validateResponse(response);
        return response;
    }
}
