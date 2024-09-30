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
package com.ibm.integration.model.databand;

import java.util.ArrayList;

public class Run extends Component {
    private static final String JSON_TEMPLATE
            = "{\"eventTime\":\"$EVENTTIME$\",\"eventType\":\"$EVENTTYPE$\",\"job\":{\"name\":\"$NAME$\",\"namespace\":\"com.ibm.ikc.integration.databand\"},\"producer\":\"custom_api\",\"run\":{\"facets\":{\"nominalTime\":{\"_producer\":\"https://some.producer.com/version/1.0\",\"_schemaURL\":\"https://github.com/OpenLineage/OpenLineage/blob/main/spec/facets/SQLJobFacet.json\",\"nominalStartTime\":\"$STARTTIME$\"},\"log\":{\"_producer\":\"https://github.com/OpenLineage/OpenLineage/tree/1.11.3/client/python\",\"_schemaURL\":\"https://raw.githubusercontent.com/OpenLineage/OpenLineage/main/spec/OpenLineage.json#/definitions/BaseFacet\",\"logBody\":\"$LOGBODY$\"},\"startTime\":{\"startTime\":\"$STARTTIME$\",\"_producer\":\"https://some.producer.com/version/1.0\",\"_schemaURL\":\"https://github.com/OpenLineage/OpenLineage/blob/main/spec/facets/SQLJobFacet.json\"},\"tags\":{\"projectName\":\"$PROJECTNAME$\",\"runName\":\"$RUNNAME$\"}},\"runId\":\"$ID$\"},\"schemaURL\":\"https://openlineage.io/spec/1-0-5/OpenLineage.json#/definitions/RunEvent\"}";
    private final String projectName;
    private final String runName;
    private final ArrayList<Task> allTasks = new ArrayList<>();

    public Run(String name, String projectName, String runName, String startTime, String eventTime, EventType eventType, String logBody) {
        super(name, startTime, eventTime, eventType, logBody);
        this.projectName = projectName;
        this.runName = runName;
    }

    public void addTask(Task task) {
        allTasks.add(task);
    }

    @Override
    public String toString() {
        StringBuilder jsonBuffer = new StringBuilder("[");

        // first json object in array is the pipeline
        final String output = JSON_TEMPLATE.replace("$ID$", id).replace("$NAME$", name).replace("$RUNNAME$", runName)
                .replace("$PROJECTNAME$", projectName).replace("$STARTTIME$", startTime).replace("$EVENTTIME$", eventTime)
                .replace("$EVENTTYPE$", eventType.toString()).replace("$LOGBODY$", logBody);
        jsonBuffer.append(output);

        // add json object for each tasks
        for (Task task : allTasks) {
            jsonBuffer.append(",");
            jsonBuffer.append(task.toString());
        }

        jsonBuffer.append("]");

        return jsonBuffer.toString();
    }
}
