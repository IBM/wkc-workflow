# BPMKafkaConnectorSample
 The BPMKafkaConnectorSample folder contains the source code of a sample Java Integration component that allows a BPM process or service to interact with a configured Kafka topic. Currently, this sample allows you to wait for an incoming java message and to get its contents for further processing in BPM. 

# Class BPMKafkaConsumer
This class implements a sample consumer for the messages sent to a Kafka topic. Note: You might not need the kafka [security propeties](https://github.com/IBM/wkc-workflow/blob/0cfc9258c3e2d4ea17c54bb61ba328bd0d546d96/BPMKafkaIntegration/BPMKafkaConnectorSample/src/main/java/com/ibm/is/StewardshipCenterSamples/BPMKafkaIntegrationSample/BPMKafkaConsumer.java#L124) if you do not use IBM Information Server Enterprise Search.

## Method getNextEvent(String kafkaBootstrapServers, String kafkaTopic, String kafkaConsumerGroup)
This method waits for the next incoming message on the configured Kafka topic and returns the message. It waits for at most the timeout configured in the configureDetails() method and returns null if there was no new message to return.

Parameters:
* String **kafkaBootstrapServers**: Connection URL to connect to zookeeper. Typically, this is the output of istool kafka listBrokers commans (see below).
* String **kafkaTopic**: Kafka topic to listen on. Typically, this is "InfosphereEvents".
* String **kafkaConsumerGroup**: Consumer Group used by this Kafka consumer. Typically, this is "BPMEventConsumerGroup".

Returns:
* String **message**: the new Kafka message, or null if the waiting timed out.

## Method shutdownConsumer()
Optional method, allows you to gracefully shutdown the Kafka consumer that gets created under the covers when calling getNextEvent().

# How to build the sample yourself
These instructions assume that you have a Java build environment along with GIT and Maven up and running.
* Get all the files from the GIT repository, e.g. via `git clone https://github.com/IBM/wkc-workflow.git`.
* cd into the project folder: `cd BPMKafkaConnectorSample`.
* Compile and build the package: `mvn package`. This will create a file *BPMKafkaConnectorSample-2.0.jar* in the *target* folder.

# How to deploy the sample's .jar file to BPM
Before you configure the BAW application, obtain the Kafka CA certificate.
Note: Step 1 and 2 below are copied from https://www.ibm.com/support/pages/add-or-upgrade-microservices-tier-existing-installation-information-server-11711-or-later

1. Run the following command in the microservices tier shell:
$ INSTALL_DIR/run_playbook.sh -y INSTALL_DIR/playbooks/shared_services/kafka_get_ca_crt.yaml -e kafka_ssl_ca_crt_file=/tmp/kafka_ca.pem

2. After the command finishes, copy the resulting /tmp/kafka_ca.pem file to the same location on the services tier host. 
Then, use the following commands on the services tier machine to create a JKS truststore that will be used for Kafka client connections, 
replacing IS_INSTALL_HOME with the actual install location:
$ IS_INSTALL_HOME/jdk/bin/keytool -import -alias kafka -file /tmp/kafka_ca.pem -keystore /tmp/ug-host-truststore.jks -storepass secret! -noprompt
(you will need the provided password later)

3. Copy the resulting ug-host-truststore.jks file to the BAW machine (you will need that truststore location later).

Copy the required kafka jar files

4. In the microservices tier shell, access the kafka pod shell and copy all kafka jar files
   kubectl exec -it kafka-0 sh
   tar -czvf /opt/kafka/logs/kafkalibs.tar.gz /opt/kafka/libs/*.jar
   exit
   kubectl cp kafka-0:/opt/kafka/logs/kafkalibs.tar.gz .

5. Copy the resulting kafkalibs.tar.gz to the BAW system and extract it into the lib/ext folder (clean that folder first). 
   If you have a multi-node cluster, the file needs to be copied into the same folder on each node.

Determine the kafka bootstrap server list 

6. In the IIS client or engine tier shell run:
   /opt/IBM/InformationServer/Clients/istools/cli/istool.sh kafka listBrokers -u isadmin -p password
   
   the ouput looks like: SASL_SSL://ughost.com:9092
   (you will need that bootstrap server list later)

Configure required BAW JVM arguments

7. Follow the instructions in this technote to set the BAW JVM arguments listed below: 
   https://www.ibm.com/support/pages/setting-generic-jvm-arguments-websphere-application-server
   If you have a multi-node cluster, the steps need to be repeated on each node.

   -Dcom.ibm.iis.events.kafka.securityProtocol=SASL_SSL 
   -Dcom.ibm.iis.events.kafka.truststorePassword=tsPassword                                     (see step2 above)
   -Dcom.ibm.iis.events.kafka.truststoreLocation=/bawtruststorelocation/ug-host-truststore.jks  (see step3 above)  
   -Dcom.ibm.iis.events.kafka.truststoreType=JKS 
   -Dcom.ibm.iis.events.kafka.saslUser=saslUser                                                 (specified when installing IIS Enterprise Search)
   -Dcom.ibm.iis.events.kafka.saslPassword=saslPassword                                         (specified when installing IIS Enterprise Search)
   -Dcom.ibm.iis.events.debugEnabled=true

Update your BAW application with the new kafka connector sample

8. Use the BAW application designer to replace the existing BPMKafkaConnectorSample-1.0.jar with the provided BPMKafkaConnectorSample-2.0.jar. 
