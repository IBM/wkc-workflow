# Developing Workflow Templates

We recommend using the Flowable Modeler to develop Flowable process definitions (workflow templates). This guide explains how to develop and modify workflow templates using the Flowable Modeler. It covers how to launch the Flowable Modeler, import existing workflow templates from your Cloud Pak for Data (CPD) system, make edits, and deploy custom workflows back to CPD.

## 1. Launch Flowable Modeler

Flowable automatically creates a default user that you can use to log in to the application.

- user: admin
- password: test

### Option 1: Using Docker Container

By default, the Flowable Modeler stores process definitions in an in-memory H2 database. This means that any saved process definitions will be lost when the Docker container shuts down.

1. Pull the Flowable image:
  
      ```bash
      docker pull flowable/all-in-one
      ```

2. Run the container:  

      ```bash
      docker run -p 8080:8080 flowable/all-in-one
      ```

3. Open Flowable Modeler in your browser:
   [http://localhost:8080/flowable-modeler](http://localhost:8080/flowable-modeler)

To ensure your process definitions are retained across restarts, you can configure the Flowable container to persist its database in a local folder.

1. Create a folder for storing the H2 database:

    ```bash
    mkdir /root/flowable
    ```

2. Launch Flowable with persistent storage:

    ```bash
    docker run -p 8080:8080 -v /root/flowable:/flowable-db -e "spring.datasource.url=jdbc:h2:/flowable-db/db;AUTO_SERVER=TRUE;AUTO_SERVER_PORT=9091;DB_CLOSE_DELAY=-1" flowable/all-in-one
    ```

For more information, visit Flowable's [Docker Hub](https://hub.docker.com/r/flowable/all-in-one) page.

### Option 2: Using Web Archive (.war) File

To run Flowable Modeler using a `.war` file, you need to have **Java JDK 8 or greater** installed.

1. Navigate to Flowable's official [Releases](https://github.com/flowable/flowable-engine/releases) page.
   - Download the release zip file from the **assets** section of the desired version.
   - Extract the zip file and navigate to the `wars` folder.
   - Locate the `flowable-ui.war` file.
2. Launch the Flowable Modeler by running:

    ```bash
    java -jar flowable-ui.war
    ```

3. Access Flowable Modeler at:
  [http://localhost:8080/flowable-ui/modeler](http://localhost:8080/flowable-ui/modeler)

By default, a folder named `flowable-db` will be created under the user's home directory. This folder contains the database files, which persist the data you create or modify within Flowable Modeler across your sessions.

## 2. Import Existing Workflow Templates into Flowable Modeler

To use existing workflow templates from CPD as a starting point for your modifications:

1. In CPD, go to **Administration > Workflows**.
2. On the "Workflow types" tab, click **Governance Artifact Management**.
3. Switch to the "Workflow Template Files" (or "Resources") tab.
4. Click **Export Files**.
5. Unzip the downloaded file. If there are nested zip files, unzip them as well until you have `.bpmn` files.
6. In Flowable Modeler, click **Import Process** and drag a `.bpmn` or `.bpmn20.xml` file into the window.

## 3. Edit Workflow Templates

Once the workflow templates are imported, you can start editing them:

1. In Flowable Modeler, click on the process definition thumbnail.
2. Click **Visual Editor** to open the editor.
3. Make the necessary changes to the template.
4. Save your changes by clicking the floppy disk icon, and then close the editor by clicking the button at the far right.

## 4. Deploy Custom Workflow Templates to CPD

Once you've completed your changes, deploy the updated template back to CPD:

1. In Flowable Modeler, on the **Processes** tab, select the process definition by clicking its thumbnail (do not open the Visual Editor).
2. Download the updated template by clicking the **Download** button (down arrow icon).
3. In CPD, navigate to **Administration > Workflows**.
4. On the "Workflow Types" tab, select the appropriate workflow type.
5. Switch to the "Workflow Template Files" (or "Resources") tab.
6. Click **Import Files** and follow the on-screen instructions.

### Creating New Workflow Types

If the workflow type does not yet exist, refer to the [IBM Documentation](https://www.ibm.com/docs/en/cloud-paks/cp-data/4.7.x?topic=workflows-importing-custom-process-definitions) for guidance on importing custom process definitions.
