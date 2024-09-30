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
package com.ibm.integration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.ibm.integration.ikc.Config;
import com.ibm.integration.ikc.Connection;
import com.ibm.integration.ikc.DataRetriever;
import com.ibm.integration.model.databand.Component;
import com.ibm.integration.model.databand.EventType;
import com.ibm.integration.model.databand.Run;
import com.ibm.integration.model.databand.Task;
import com.ibm.integration.model.ikc.Configuration;
import com.ibm.integration.model.ikc.UserTask;
import com.ibm.integration.model.ikc.Workflow;
import com.ibm.integration.util.Utils;

/**
 * Sample integration between IKC workflow components and Databand.
 * See the Config class for the required environment variables.
 */
public class IntegrationDemo {
    public static final String DB_PROJECT_NAME = "Governance artifact management";
    final private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    public static void main(String[] args) throws Exception {
        Config config = Config.getInstance();
        String artifactId = config.getArtifactId();
        String artifactVersionId = config.getArtifactVersionId();
        if (args.length >= 1) {
            if (args[0].contains("/")) {
                String[] ids = args[0].split("/");
                artifactId = ids[0];
                artifactVersionId = ids[1];
            } else if (args.length >= 2) {
                artifactId = args[0];
                artifactVersionId = args[1];
            }
        }

        Connection connection = new Connection();
        DataRetriever retriever = new DataRetriever(connection);

        System.out.println("Retrieving IKC data for artifact " + artifactId + "/" + artifactVersionId);
        ArrayList<UserTask> activeTasks = retriever.getActiveTasks(artifactId, artifactVersionId);
        System.out.println("Number of active tasks: " + activeTasks.size());
        ArrayList<UserTask> completedTasks = retriever.getCompletedTasks(artifactId, artifactVersionId);
        System.out.println("Number of completed tasks: " + completedTasks.size());
        ArrayList<UserTask> tasks = new ArrayList<>(completedTasks);
        tasks.addAll(activeTasks);

        if (tasks.isEmpty()) {
            System.out.println("No workflow found.");
            return;
        }
        Workflow workflow = retriever.getWorkflowById(tasks.get(0).getWorkflowId());
        System.out.println("Got workflow: " + workflow.getName());
        System.out.println("Workflow is running: " + Utils.isEmpty(workflow.getCompletedAt()));

        Configuration workflowConfiguration = retriever.getConfigurationById(workflow.getConfigurationId());
        System.out.println("Got workflow config: " + workflowConfiguration.getName());

        System.out.println("Generating run data...");

        // The workflow instance becomes the Databand run,
        // The workflow configuration becomes the pipeline,
        // The workflow type becomes the project.
        // Create the pipeline
        final String workflowName = "Workflow instance: " + workflow.getName() + "_" + workflow.getWorkflowId();
        final String workflowStartTime = workflow.getCreatedAt();
        final String workflowEventTime = workflow.isRunning() ? dateFormat.format(new Date())
                : workflow.getCompletedAt();
        final EventType workflowEventType = workflow.isRunning() ? EventType.RUNNING : EventType.COMPLETE;
        Run run = new Run(workflowConfiguration.getName(), DB_PROJECT_NAME, workflowName, workflowStartTime,
                workflowEventTime, workflowEventType, "");
        run.setId(workflow.getWorkflowId());

        for (UserTask task : tasks) {
            // The task same have to be unique to distinguish instances of the same task
            final String taskName = task.getName();
            final String taskStartTime = task.getCreatedAt();
            final String taskEventTime = task.isActive() ? workflowEventTime : task.getCompletedAt();
            final EventType taskEventType = task.isActive() ? EventType.RUNNING : EventType.COMPLETE;
            Task subTask = createTask(taskName, taskStartTime, taskEventTime, taskEventType, "", run, run, null);
            subTask.setId(task.getTaskId());
        }

        DatabandSender.sendRun(run);
    }

    private static Task createTask(String name, String startTime, String eventTime, EventType eventType, String log,
            Run run, Component parent,
            Task receiver) {
        Task newTask = new Task(name, startTime, eventTime, eventType, log, parent);
        run.addTask(newTask);
        if (receiver != null)
            receiver.addInput(newTask);
        return newTask;
    }
}