---
title: "7.2 To the cloud"
linkTitle: "7.2 To the cloud"
weight: 720
sectionnumber: 7.2
onlyWhen: artemis
description: >
   Deploy our microservices with ActiveMQ Artemis to the cloud.
---

Let's configure our microservices and message broker on OpenShift so we can test this in our production ready environment.

Create the artemis resources:

```yaml

parameters:
  - name: METADATA_LABELS_APPLICATION
    value: {{% param "k8s_user_name" %}}
  - name: METADATA_LABELS_TEMPLATE
    value: quarkus-techlab-template
  - name: ARTEMIS_SERVICE_NAME
    description: Name for the openshift service
    value: artemis-activemq
  - name: ARTEMIS_IMAGE_NAME
    value: vromero/activemq-artemis
  - name: ARTEMIS_IMAGE_TAG
    value: 2.10.1
  - name: ARTEMIS_VOLUME_CLAIM_SIZE
    description: Amount of data for the pvc for artemis
    value: 1Gi
  - name: ARTEMIS_DISABLE_SECURITY
    description: Enables non-tls connections
    value: "true"
  - name: ARTEMIS_RESTORE_CONFIGURATION
    description: Restores configurations on container startup
    value: "true"
  - name: ARTEMIS_ENABLE_JMX_EXPORTER
    description: Enables the jmx exporter function
    value: "true"
  - name: ARTEMIS_ANONYMOUS_LOGIN
    description: Allows anonymous login for the artemis activemq system
    value: "false"
  - name: ARTEMIS_PORT_MQTT
    description: Port for MQTT
    value: "1883"
  - name: ARTEMIS_PORT_WEB_SERVER
    description: Port for the web server
    value: "8161"
  - name: ARTEMIS_PORT_HORNETQ
    description: Port for HORNETQ
    value: "5445"
  - name: ARTEMIS_PORT_AMQP
    description: Port for AMQP
    value: "5672"
  - name: ARTEMIS_PORT_JMX_EXPORTER
    description: Port for the jmx exporter for prometheus
    value: "9404"
  - name: ARTEMIS_PORT_STOMP
    description: Port for STOMP
    value: "61613"
  - name: ARTEMIS_PORT_CORE
    description: Port for the core api
    value: "61616"
  - name: ARTEMIS_CPU_LIMIT
    value: 500m
  - name: ARTEMIS_MEMORY_LIMIT
    value: 2Gi
  - name: ARTEMIS_CPU_REQUEST
    value: 200m
  - name: ARTEMIS_MEMORY_REQUEST
    value: 1Gi
  - name: ROUTE_ARTEMIS_HOST
    description: Route for the artemis web ui
    value: artemis-quarkus-techlab.ocp.aws.ch

kind: Template
apiVersion: v1
metadata:
  name: b4u-application-template
  annotations:
    iconClass: icon-java
    tags: java,microservice,b4u
objects:

  - apiVersion: v1
    kind: ImageStream
    metadata:
      labels:
        application: ${METADATA_LABELS_APPLICATION}
        template: ${METADATA_LABELS_TEMPLATE}
      name: ${ARTEMIS_SERVICE_NAME}
    spec:
      lookupPolicy:
        local: false
      tags:
        - from:
            kind: DockerImage
            name: ${ARTEMIS_IMAGE_NAME}:${ARTEMIS_IMAGE_TAG}
          name: ${ARTEMIS_IMAGE_TAG}

  - apiVersion: v1
    kind: PersistentVolumeClaim
    metadata:
      labels:
        application: ${METADATA_LABELS_APPLICATION}
        template: ${METADATA_LABELS_TEMPLATE}
      name: artemis-data
    spec:
      accessModes:
        - ReadWriteOnce
      resources:
        requests:
          storage: ${ARTEMIS_VOLUME_CLAIM_SIZE}

  - apiVersion: v1
    kind: DeploymentConfig
    metadata:
      labels:
        application: ${METADATA_LABELS_APPLICATION}
        template: ${METADATA_LABELS_TEMPLATE}
      name: ${ARTEMIS_SERVICE_NAME}
    spec:
      replicas: 1
      selector:
        deploymentConfig: ${ARTEMIS_SERVICE_NAME}
      strategy:
        type: Recreate
      template:
        metadata:
          labels:
            application: ${METADATA_LABELS_APPLICATION}
            deploymentConfig: ${ARTEMIS_SERVICE_NAME}
          name: ${ARTEMIS_SERVICE_NAME}
        spec:
          containers:
            - env:
                - name: ARTEMIS_USERNAME
                  value: quarkus
                - name: ARTEMIS_PASSWORD
                  value: quarkus
                - name: ANONYMOUS_LOGIN
                  value: ${ARTEMIS_ANONYMOUS_LOGIN}
                - name: DISABLE_SECURITY
                  value: ${ARTEMIS_DISABLE_SECURITY}
                - name: RESTORE_CONFIGURATION
                  value: ${ARTEMIS_RESTORE_CONFIGURATION}
                - name: ENABLE_JMX_EXPORTER
                  value: ${ARTEMIS_ENABLE_JMX_EXPORTER}
              image: ${ARTEMIS_IMAGE_NAME}
              imagePullPolicy: Always
              livenessProbe:
                failureThreshold: 5
                successThreshold: 1
                httpGet:
                  path: /
                  port: 8161
                  scheme: HTTP
                initialDelaySeconds: 120
                preiodSeconds: 20
                timeoutSeconds: 15
              readinessProbe:
                failureThreshold: 5
                successThreshold: 1
                httpGet:
                  path: /
                  port: 8161
                  scheme: HTTP
                initialDelaySeconds: 30
                preiodSeconds: 20
                timeoutSeconds: 15
              resources:
                limits:
                  cpu: ${ARTEMIS_CPU_LIMIT}
                  memory: ${ARTEMIS_MEMORY_LIMIT}
                requests:
                  cpu: ${ARTEMIS_CPU_REQUEST}
                  memory: ${ARTEMIS_MEMORY_REQUEST}
              volumeMounts:
                - name: artemis-data
                  mountPath: /var/lib/artemis/data
                - name: artemis-config-override
                  mountPath: /var/lib/artemis/etc-override
                - name: artemis-config-dir
                  mountPath: /var/lib/artemis/etc
                - name: artemis-lock
                  mountPath: /var/lib/artemis/lock
                - name: artemis-temp
                  mountPath: /var/lib/artemis/tmp
              name: ${ARTEMIS_SERVICE_NAME}
              ports:
                - containerPort: 1883
                  name: mqtt
                  protocol: TCP
                - containerPort: 5445
                  name: hornetq
                  protocol: TCP
                - containerPort: 5672
                  name: amqp
                  protocol: TCP
                - containerPort: 8161
                  name: webserver
                  protocol: TCP
                - containerPort: 9404
                  name: jmx
                  protocol: TCP
                - containerPort: 61613
                  name: stomp
                  protocol: TCP
                - containerPort: 61616
                  name: core
                  protocol: TCP
          volumes:
            - name: artemis-data
              persistentVolumeClaim:
                claimName: artemis-data
            - name: artemis-config-override
              configMap:
                name: artemis-config
            - name: artemis-config-dir
              emptyDir: {}
            - name: artemis-lock
              emptyDir: {}
            - name: artemis-temp
              emptyDir: {}
      triggers:
        - imageChangeParams:
            automatic: true
            containerNames:
              - ${ARTEMIS_SERVICE_NAME}
            from:
              kind: ImageStreamTag
              name: ${ARTEMIS_SERVICE_NAME}:${ARTEMIS_IMAGE_TAG}
          type: ImageChange
        - type: ConfigChange

  - apiVersion: v1
    kind: Service
    metadata:
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/path: "/"
        prometheus.io/port: ${ARTEMIS_PORT_JMX_EXPORTER}
      labels:
        application: ${METADATA_LABELS_APPLICATION}
        template: ${METADATA_LABELS_TEMPLATE}
        prometheus-monitoring: "true"
        monitoring: ${ARTEMIS_SERVICE_NAME}
      name: ${ARTEMIS_SERVICE_NAME}
    spec:
      ports:
        - name: artemis-mqtt
          port: 1883
          targetPort: 1883
          protocol: TCP
        - name: artemis-hornetq
          port: 5445
          targetPort: 5445
          protocol: TCP
        - name: artemis-amqp
          port: 5672
          targetPort: 5672
          protocol: TCP
        - name: artemis-web-server
          port: 8161
          targetPort: 8161
          protocol: TCP
        - name: artemis-jmx-exporter
          port: 9404
          targetPort: 9404
          protocol: TCP
        - name: artemis-stomp
          port: 61613
          targetPort: 61613
          protocol: TCP
        - name: artemis-core
          port: 61616
          targetPort: 61616
          protocol: TCP
      selector:
        deploymentConfig: ${ARTEMIS_SERVICE_NAME}
      sessionAffinity: None
      type: ClusterIP

  - apiVersion: v1
    kind: Route
    metadata:
      labels:
        application: ${METADATA_LABELS_APPLICATION}
        template: ${METADATA_LABELS_TEMPLATE}
      name: artemis-route
    spec:
      host: ${ROUTE_ARTEMIS_HOST}
      port:
        targetPort: artemis-web-server
      tls:
        insecureEdgeTerminationPolicy: Redirect
        termination: edge
      to:
        kind: Service
        name: ${ARTEMIS_SERVICE_NAME}
      wildcardPolicy: None

  - apiVersion: v1
    kind: ConfigMap
    metadata:
      labels:
        application: ${METADATA_LABELS_APPLICATION}
        template: ${METADATA_LABELS_TEMPLATE}
      name: artemis-config
    data:
      broker-1.xml: |-
        <?xml version='1.0'?>
        <configuration xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                      xmlns="urn:activemq"
                      xsi:schemaLocation="urn:activemq /schema/artemis-configuration.xsd">

            <core xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:activemq:core" xsi:schemaLocation="urn:activemq:core ">

                <name>quarkus-techlab-artemis</name>

                <persistence-enabled>true</persistence-enabled>

                <!-- this could be ASYNCIO, MAPPED, NIO
                    ASYNCIO: Linux Libaio
                    MAPPED: mmap files
                    NIO: Plain Java Files
                -->
                <journal-type>ASYNCIO</journal-type>

                <paging-directory>data/paging</paging-directory>

                <bindings-directory>data/bindings</bindings-directory>

                <journal-directory>data/journal</journal-directory>

                <large-messages-directory>data/large-messages</large-messages-directory>

                <journal-datasync>true</journal-datasync>

                <journal-min-files>2</journal-min-files>

                <journal-pool-files>10</journal-pool-files>

                <journal-device-block-size>4096</journal-device-block-size>

                <journal-file-size>10M</journal-file-size>

                <!--
                This value was determined through a calculation.
                Your system could perform 62.5 writes per millisecond
                on the current journal configuration.
                That translates as a sync write every 16000 nanoseconds.

                Note: If you specify 0 the system will perform writes directly to the disk.
                      We recommend this to be 0 if you are using journalType=MAPPED and journal-datasync=false.
                -->
                <journal-buffer-timeout>16000</journal-buffer-timeout>


                <!--
                  When using ASYNCIO, this will determine the writing queue depth for libaio.
                -->
                <journal-max-io>4096</journal-max-io>
                <!--
                  You can verify the network health of a particular NIC by specifying the <network-check-NIC> element.
                  <network-check-NIC>theNicName</network-check-NIC>
                  -->

                <!--
                  Use this to use an HTTP server to validate the network
                  <network-check-URL-list>http://www.apache.org</network-check-URL-list> -->

                <!-- <network-check-period>10000</network-check-period> -->
                <!-- <network-check-timeout>1000</network-check-timeout> -->

                <!-- this is a comma separated list, no spaces, just DNS or IPs
                    it should accept IPV6

                    Warning: Make sure you understand your network topology as this is meant to validate if your network is valid.
                              Using IPs that could eventually disappear or be partially visible may defeat the purpose.
                              You can use a list of multiple IPs, and if any successful ping will make the server OK to continue running -->
                <!-- <network-check-list>10.0.0.1</network-check-list> -->

                <!-- use this to customize the ping used for ipv4 addresses -->
                <!-- <network-check-ping-command>ping -c 1 -t %d %s</network-check-ping-command> -->

                <!-- use this to customize the ping used for ipv6 addresses -->
                <!-- <network-check-ping6-command>ping6 -c 1 %2$s</network-check-ping6-command> -->


                <!-- how often we are looking for how many bytes are being used on the disk in ms -->
                <disk-scan-period>5000</disk-scan-period>

                <!-- once the disk hits this limit the system will block, or close the connection in certain protocols
                    that won't support flow control. -->
                <max-disk-usage>90</max-disk-usage>

                <!-- should the broker detect dead locks and other issues -->
                <critical-analyzer>true</critical-analyzer>

                <critical-analyzer-timeout>120000</critical-analyzer-timeout>

                <critical-analyzer-check-period>60000</critical-analyzer-check-period>

                <critical-analyzer-policy>HALT</critical-analyzer-policy>

                <!-- the system will enter into page mode once you hit this limit.
                    This is an estimate in bytes of how much the messages are using in memory

                      The system will use half of the available memory (-Xmx) by default for the global-max-size.
                      You may specify a different value here if you need to customize it to your needs.

                      <global-max-size>100Mb</global-max-size>

                -->

                <acceptors>

                    <!-- useEpoll means: it will use Netty epoll if you are on a system (Linux) that supports it -->
                    <!-- amqpCredits: The number of credits sent to AMQP producers -->
                    <!-- amqpLowCredits: The server will send the # credits specified at amqpCredits at this low mark -->

                    <!-- Note: If an acceptor needs to be compatible with HornetQ and/or Artemis 1.x clients add
                              "anycastPrefix=jms.queue.;multicastPrefix=jms.topic." to the acceptor url.
                              See https://issues.apache.org/jira/browse/ARTEMIS-1644 for more information. -->

                    <!-- Acceptor for every supported protocol -->
                    <acceptor name="artemis">
                        tcp://0.0.0.0:61616?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=CORE,AMQP,STOMP,HORNETQ,MQTT,OPENWIRE;useEpoll=true;amqpCredits=1000;amqpLowCredits=300
                    </acceptor>

                    <!-- AMQP Acceptor.  Listens on default AMQP port for AMQP traffic.-->
                    <acceptor name="amqp">
                        tcp://0.0.0.0:5672?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=AMQP;useEpoll=true;amqpCredits=1000;amqpLowCredits=300
                    </acceptor>

                    <!-- STOMP Acceptor. -->
                    <acceptor name="stomp">
                        tcp://0.0.0.0:61613?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=STOMP;useEpoll=true
                    </acceptor>

                    <!-- HornetQ Compatibility Acceptor.  Enables HornetQ Core and STOMP for legacy HornetQ clients. -->
                    <acceptor name="hornetq">
                        tcp://0.0.0.0:5445?anycastPrefix=jms.queue.;multicastPrefix=jms.topic.;protocols=HORNETQ,STOMP;useEpoll=true
                    </acceptor>

                    <!-- MQTT Acceptor -->
                    <acceptor name="mqtt">tcp://0.0.0.0:1883?tcpSendBufferSize=1048576;tcpReceiveBufferSize=1048576;protocols=MQTT;useEpoll=true
                    </acceptor>

                </acceptors>


                <security-settings>
                    <security-setting match="#">
                        <permission type="createNonDurableQueue" roles="amq"/>
                        <permission type="deleteNonDurableQueue" roles="amq"/>
                        <permission type="createDurableQueue" roles="amq"/>
                        <permission type="deleteDurableQueue" roles="amq"/>
                        <permission type="createAddress" roles="amq"/>
                        <permission type="deleteAddress" roles="amq"/>
                        <permission type="consume" roles="amq"/>
                        <permission type="browse" roles="amq"/>
                        <permission type="send" roles="amq"/>
                        <!-- we need this otherwise ./artemis data imp wouldn't work -->
                        <permission type="manage" roles="amq"/>
                    </security-setting>
                </security-settings>

                <address-settings>
                    <!-- if you define auto-create on certain queues, management has to be auto-create -->
                    <address-setting match="activemq.management#">
                        <dead-letter-address>DLQ</dead-letter-address>
                        <expiry-address>ExpiryQueue</expiry-address>
                        <redelivery-delay>0</redelivery-delay>
                        <!-- with -1 only the global-max-size is in use for limiting -->
                        <max-size-bytes>-1</max-size-bytes>
                        <message-counter-history-day-limit>10</message-counter-history-day-limit>
                        <address-full-policy>PAGE</address-full-policy>
                        <auto-create-queues>true</auto-create-queues>
                        <auto-create-addresses>true</auto-create-addresses>
                        <auto-create-jms-queues>true</auto-create-jms-queues>
                        <auto-create-jms-topics>true</auto-create-jms-topics>
                    </address-setting>
                    <!--default for catch all-->
                    <address-setting match="#">
                        <dead-letter-address>DLQ</dead-letter-address>
                        <expiry-address>ExpiryQueue</expiry-address>
                        <redelivery-delay>0</redelivery-delay>
                        <!-- with -1 only the global-max-size is in use for limiting -->
                        <max-size-bytes>-1</max-size-bytes>
                        <message-counter-history-day-limit>10</message-counter-history-day-limit>
                        <address-full-policy>PAGE</address-full-policy>
                        <auto-create-queues>true</auto-create-queues>
                        <auto-create-addresses>true</auto-create-addresses>
                        <auto-create-jms-queues>true</auto-create-jms-queues>
                        <auto-create-jms-topics>true</auto-create-jms-topics>
                    </address-setting>
                </address-settings>

                <addresses>
                    <address name="DLQ">
                        <anycast>
                            <queue name="DLQ"/>
                        </anycast>
                    </address>
                    <address name="ExpiryAddress">
                        <anycast>
                            <queue name="ExpiryQueue"/>
                        </anycast>
                    </address>
                    <address name="data-inbound">
                        <anycast>
                            <queue name="data-inbound"/>
                        </anycast>
                    </address>
                    <address name="data-transformed">
                        <anycast>
                            <queue name="data-transformed"/>
                        </anycast>
                    </address>
                </addresses>
            </core>
        </configuration>

```

Apply the template for your message broker and OpenShift will rollout a new instance of Artemis ActiveMQ in your namespace. Tag your images and push your images to the OpenShift registry as shown in the Lab 3. Your deployments should get rolled out automatically!
