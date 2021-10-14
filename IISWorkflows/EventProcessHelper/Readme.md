# Install instructions #

To install the updated initEventProcessHelper.bpmn, download initEventProcessHelper.bpmn and run the curl command listed below:

curl -k -s -X POST -H "Accept: application/json" -F "deployment=@initEventProcessHelper.bpmn" -u isadmin:password https://servername:9443/ibm/iis/activiti-rest/service/repository/deployments
