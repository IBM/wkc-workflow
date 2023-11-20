# Developing Workflow Templates

We recommend using the Flowable Modeler to develop Flowable process definitions, aka workflow templates. The sections below describe how to launch the Flowable Modeler, how to get the existing workflow templates of your CPD system into the Flowable Modeler as a starting point for modifications, and how to get your custom workflow templates deployed to your CPD system.

## Launch Flowable Modeler in a docker container

1. Go to https://hub.docker.com/r/flowable/all-in-one and follow the instructions to start the Flowable modeler in a local Docker image (first, run the docker pull... command, then docker run...)
  - `docker pull flowable/all-in-one`
  - `docker run -p 8080:8080 flowable/all-in-one`
2. Open the modeler at http://localhost:8080/flowable-modeler 

This, by default, lets the Flowable Modeler store any process definitions in an in-memory H2 database, which means that it forgets any previously saved process definitions when the docker container gets shut down.

### Persist Flowable Modeler's process definitions in a local folder

You can tell Flowable Modeler's docker container to persist its internal storage into a local folder outside the docker container, so that your edits survive a container restart.

1. Create a local folder for Flowable's H2 database: `mkdir /root/flowable`
2. Launch Flowable using local-folder-persisted H2 database:

```
docker run -p 8080:8080 -v /root/flowable:/flowable-db -e "spring.datasource.url=jdbc:h2:/flowable-db/db;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9091;DB_CLOSE_DELAY=-1" flowable/all-in-one
```

## Import existing workflow templates into Flowable Modeler

* In CPD, navigate to Administration / Workflows
* On the "Workflow types" tab, click "Governance artifact management"
* Switch to the "Workflow template files" (or "Resources") tab
* Click export files
* Unzip the downloaded file. If the downloaded zip file contains other zip files, then unzip those as well, until you see .bpmn files.
* Switch to the Flowable Modeler
* Click "Import process" and drag a .bpmn or .bpmn20.xml file into the window.

## Edit the workflow template
* In Flowable Modeler, click on a process definition thumbnail, and then on the "Visual Editor" button.
* Edit the template
* Click the floppy disk button to save your changes, then the close button at the far right.

## Deploy custom workflow template to CPD

* In Flowable Modeler, on the processes tab, click on the process definition thumbnail to see its details (don't click on the Visual Editor icon)
* Click the "download" button with the down arrow icon to save the edited file.
* Switch to CPD, navigate to Administration / Workflows
* On the "Workflow types" tab, click the tile of your workflow type
* Switch to the "Workflow template files" (or "Resources") tab
* Click the "Import files" button, and follow the instructions there.

If your workflow type does not yet exist, then please follow the steps in https://www.ibm.com/docs/en/cloud-paks/cp-data/4.7.x?topic=workflows-importing-custom-process-definitions
