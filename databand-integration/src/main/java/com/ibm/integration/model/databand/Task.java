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
import java.util.Iterator;
import java.util.List;

public class Task extends Component {

    public static final String JSON_TEMPLATE
            = "{\"eventTime\":\"$EVENTTIME$\",\"eventType\":\"$EVENTTYPE$\",\"job\":{\"name\":\"$NAME$\",\"namespace\":\"com.ibm.ikc.integration.databand\"},\"producer\":\"custom_api\",\"inputs\":[$INPUTS$],\"run\":{\"facets\":{\"nominalTime\":{\"_producer\":\"https://some.producer.com/version/1.0\",\"_schemaURL\":\"https://github.com/OpenLineage/OpenLineage/blob/main/spec/facets/SQLJobFacet.json\",\"nominalStartTime\":\"$STARTTIME$\"},\"log\":{\"_producer\":\"https://github.com/OpenLineage/OpenLineage/tree/1.11.3/client/python\",\"_schemaURL\":\"https://raw.githubusercontent.com/OpenLineage/OpenLineage/main/spec/OpenLineage.json#/definitions/BaseFacet\",\"logBody\":\"$LOGBODY$\"},\"parent\":{$PARENT$},\"startTime\":{\"startTime\":\"$STARTTIME$\",\"_producer\":\"https://some.producer.com/version/1.0\",\"_schemaURL\":\"https://github.com/OpenLineage/OpenLineage/blob/main/spec/facets/SQLJobFacet.json\"}},\"runId\":\"$ID$\"},\"schemaURL\":\"https://openlineage.io/spec/1-0-5/OpenLineage.json#/definitions/RunEvent\"}";
    public static final String PARENT_JSON_TEMPLATE
            = "\"_producer\":\"https://github.com/OpenLineage/OpenLineage/tree/1.11.3/client/python\",\"_schemaURL\":\"https://raw.githubusercontent.com/OpenLineage/OpenLineage/main/spec/OpenLineage.json#/definitions/ParentRunFacet\",\"job\":{\"name\":\"$NAME$\",\"namespace\":\"com.ibm.ikc.integration.databand\"},\"run\":{\"runId\":\"$ID$\"}";
    private final Component parent;
    protected List<Task> inputs = new ArrayList<>();

    public Task(String name, String startTime, String eventTime, EventType eventType, String logBody, Component parent) {
        super(name, startTime, eventTime, eventType, logBody);
        this.parent = parent;
    }

    public void addInput(Task inputTask) {
        inputs.add(inputTask);
    }

    @Override
    public String toString() {
        String output = JSON_TEMPLATE.replace("$ID$", id).replace("$NAME$", name).replace("$STARTTIME$", startTime)
                .replace("$EVENTTIME$", eventTime).replace("$EVENTTYPE$", eventType.toString()).replace("$LOGBODY$", logBody);

        // populate parent element
        String parentString = "";
        if (parent != null) {
            parentString = PARENT_JSON_TEMPLATE.replace("$ID$", parent.getId()).replace("$NAME$", parent.getName());
        }
        output = output.replace("$PARENT$", parentString);

        // populate inputs
        StringBuilder inputsJson = new StringBuilder();
        Iterator<Task> iterator = inputs.iterator();
        while (iterator.hasNext()) {
            Task input = iterator.next();
            String inputJson = "{\"name\":\"$NAME$\",\"namespace\":\"com.ibm.ikc.integration.databand\"}";
            inputsJson.append(inputJson.replace("$NAME$", input.getName()));
            if (iterator.hasNext()) {
                inputsJson.append(",");
            }
        }
        output = output.replace("$INPUTS$", inputsJson.toString());
        return output;
    }
}
