/*
 * Copyright IBM Corp. 2016,2021
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
package com.ibm.is.StewardshipCenterSamples.BPMKafkaIntegrationSample;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;

public class BPMKafkaConsumer {

	public static final String ISKEYNAME__EVENTS_KAFKA_SECURITY_PROTOCOL = "com.ibm.iis.events.kafka.securityProtocol";
	public static final String ISKEYNAME__EVENTS_KAFKA_TRUSTSTORE_PASSWORD = "com.ibm.iis.events.kafka.truststorePassword";
	public static final String ISKEYNAME__EVENTS_KAFKA_TRUSTSTORE_TYPE = "com.ibm.iis.events.kafka.truststoreType";
	public static final String ISKEYNAME__EVENTS_KAFKA_TRUSTSTORE_LOCATION = "com.ibm.iis.events.kafka.truststoreLocation";
	public static final String ISKEYNAME__EVENTS_KAFKA_SASL_USER = "com.ibm.iis.events.kafka.saslUser";
	public static final String ISKEYNAME__EVENTS_KAFKA_SASL_PASSWORD = "com.ibm.iis.events.kafka.saslPassword";

	public static final String DEBUG_ENABLED = "com.ibm.iis.events.debugEnabled";
	
	private static String kafkaSecurityProtocol = null;
	private static String kafkaTruststorePassword = null;
	private static String kafkaTruststoreType = null;
	private static String kafkaSaslUser = null;
	private static String kafkaSaslPassword = null;
	private static String kafkaTruststoreLocation = null;

	private static Properties environment = null;
	private static boolean debug = false;

	/**
	 * don't access directly! Always use getKafkaConsumer() to obtain a reference!
	 */
	private static KafkaConsumer<String, String> privateConsumer = null;
	
	static {
		InputStream is = BPMKafkaConsumer.class.getResourceAsStream("environment.properties");
		if (is != null) {
			logInfo("using environment.properties");
			environment = new Properties();
			try {
				environment.load(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logInfo("using system properties");
		}
	}

	public static String getNextEvent(String kafkaBootstrapServers, String kafkaTopic, String kafkaConsumerGroup) {
		logDebug("getNextEvent start");

		KafkaConsumer<String, String> consumer = getKafkaConsumer(kafkaBootstrapServers, kafkaTopic, kafkaConsumerGroup);

		String value = null;
		ConsumerRecords<String, String> records = consumer.poll(100);
		if (records.count() > 0) {
			logDebug("record count: " + records.count());
		}
	
		for (ConsumerRecord<String, String> record : records) {
			logDebug("record offset: " + record.offset());
			logDebug("record value: " + record.value());
			//System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
			value = record.value();
		}

		return value;
	}

	public static void shutdownConsumer() {
		logDebug("BPMKafkaConsumer.shutdownConsumer() -- start");

		if (privateConsumer != null) {
			privateConsumer.close();
			privateConsumer = null;
		}

		logDebug("BPMKafkaConsumer.shutdownConsumer() -- end");
	}

	public static KafkaConsumer<String, String> getKafkaConsumer(String kafkaBootstrapServers, String kafkaTopic, String kafkaConsumerGroup) {
		if (privateConsumer == null) {
			debug = Boolean.parseBoolean(getProperty(DEBUG_ENABLED));
	
			logDebug("kafkaBootstrapServers: " + kafkaBootstrapServers + ", kafkaTopic: " + kafkaTopic + ", kafkaConsumerGroup" + kafkaConsumerGroup);

			Properties defaultConsumerProperties = new Properties();
			
			defaultConsumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);
			defaultConsumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
			defaultConsumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			defaultConsumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			defaultConsumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
			defaultConsumerProperties.put(org.apache.kafka.clients.consumer.ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerGroup);
			defaultConsumerProperties.put("max.poll.records", 1);

			// security
			defaultConsumerProperties.putAll(getKafkaSecurityProperties());

			privateConsumer = new KafkaConsumer<String, String>(defaultConsumerProperties);
			privateConsumer.subscribe(Arrays.asList(kafkaTopic));
			logDebug("consumer created");
		} else {
			logDebug("consumer reused");
		}

		return privateConsumer;
	}

	private static void logDebug(String msg) {
		// TODO: switch to LOGGER
		if (debug) {
			System.out.println("DEBUG: " + msg);
		}
	}

	private static void logWarn(String msg) {
		// TODO: switch to LOGGER
		System.out.println("WARN: " + msg);
	}

	private static void logInfo(String msg) {
		// TODO: switch to LOGGER
		System.out.println("INFO: " + msg);
	}

	private static void logError(String msg) {
		// TODO: switch to LOGGER
		System.err.println("ERROR: " + msg);
	}
	
	public static Properties getKafkaSecurityProperties() {
		Properties kafkaSecurityProperties = new Properties();
		String configuredSecurityProtocol = getKafkaSecurityProtocol();
		if (!configuredSecurityProtocol.equals(SecurityProtocol.PLAINTEXT.name)) {
			kafkaSecurityProperties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, configuredSecurityProtocol);
			kafkaSecurityProperties.put("ssl.truststore.location", getKafkaTruststoreLocation());
			kafkaSecurityProperties.put("ssl.truststore.password", getKafkaTruststorePassword());
			kafkaSecurityProperties.put("ssl.truststore.type", getKafkaTruststoreType());
			// SASL_SSL
			if (getKafkaSecurityProtocol().equals(SecurityProtocol.SASL_SSL.name)) {
				kafkaSecurityProperties.put("sasl.mechanism", "PLAIN");
				kafkaSecurityProperties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + getKafkaSaslUser() + "\"password=\"" + getKafkaSaslPassword() + "\";");
			}
		}
		return kafkaSecurityProperties;
	}

	public static String getKafkaSecurityProtocol() {
		if (kafkaSecurityProtocol == null) {
			kafkaSecurityProtocol = getProperty(ISKEYNAME__EVENTS_KAFKA_SECURITY_PROTOCOL);
		}
		return kafkaSecurityProtocol;
	}

	public static String getKafkaTruststoreType() {
		if (kafkaTruststoreType == null) {
			kafkaTruststoreType = getProperty(ISKEYNAME__EVENTS_KAFKA_TRUSTSTORE_TYPE);
		}
		return kafkaTruststoreType;
	}

	public static String getKafkaTruststorePassword() {
		if (kafkaTruststorePassword == null) {
			kafkaTruststorePassword = getProperty(ISKEYNAME__EVENTS_KAFKA_TRUSTSTORE_PASSWORD);
		}
		return kafkaTruststorePassword;
	}

	public static String getKafkaTruststoreLocation() {
		if (kafkaTruststoreLocation == null) {
			kafkaTruststoreLocation = getProperty(ISKEYNAME__EVENTS_KAFKA_TRUSTSTORE_LOCATION);
		}
		return kafkaTruststoreLocation;
	}

	public static String getKafkaSaslUser() {
		if (kafkaSaslUser == null) {
			kafkaSaslUser = getProperty(ISKEYNAME__EVENTS_KAFKA_SASL_USER);
		}
		return kafkaSaslUser;
	}

	public static String getKafkaSaslPassword() {
		if (kafkaSaslPassword == null) {
			kafkaSaslPassword = getProperty(ISKEYNAME__EVENTS_KAFKA_SASL_PASSWORD);
		}
		return kafkaSaslPassword;
	}

	private static String getProperty(String property) {
		logDebug("getProperty: " + property);
		if (environment != null) {
			logDebug("environment property value: " + environment.getProperty(property));
			return environment.getProperty(property);

		} else {
			logDebug("system property value: " + System.getProperty(property));
			return System.getProperty(property);
		}

	}

	//test
	public static void main(String[] args) throws InterruptedException {  
		
		for (int i = 0; i < 1000; i++) {
			System.out.println(BPMKafkaConsumer.getNextEvent("SASL_SSL://your_ug_host:9092", "ISALiteTest", "yourgroup"));
         Thread.sleep(2000);
		}

		BPMKafkaConsumer.shutdownConsumer();
		System.out.println("finished");
		
	}
}
