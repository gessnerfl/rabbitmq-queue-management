# RabbitMQ Queue Management
[![Build Status](https://travis-ci.org/gessnerfl/rabbitmq-queue-management.svg?branch=master)](https://travis-ci.org/gessnerfl/rabbitmq-queue-management)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=de.gessnerfl.rabbitmq-queue-management&metric=alert_status)](https://sonarcloud.io/dashboard?id=de.gessnerfl.rabbitmq-queue-management)

*Application to list, delete and re-queue messages of queues.*

## Introduction

RabbitMQ does not provide tooling to re-queue or move messages out of the box. However this can by quite handy when 
working e.g. with dead letter queues/exchanges. This tool tries to close this gap. By offering the following features

* List queues of a RabbitMQ 
* List messages of queues
* Delete first or all message(s)
* Move first or all message(s)
* Re-queue first or all message(s)

As messages must not have a unique identifier in RabbitMQ this tools creates a checksum of the message and compares the 
checksum before applying move or delete operations on messages to avoid unintended changes.

To use the application the **RabbitMQ Management Plugin** has to be activated.

The application is based on Spring Boot. For more details please also consult the Spring Boot Documentation 
(http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle).

## Operations
The following paragraphs describe the different implementations in more details.

### Delete

The delete operations applies the following steps:

- Receive first message of queue
- Ensure checksum matches
- Confirm message

In case of any error send nack with re-queuing or simply close the channel so that the message remains in the queue.

In case of deleting all messages of the queue the purge operation of the HTTP API of RabbitMQ will be used instead of
processing the messages

### Move operations

The following steps are applied to move a message from one queue to another exchange and routing key:

- Receive the first message of the queue
- Ensure checksum matches
- Activate Publisher Acknowledgement (https://www.rabbitmq.com/confirms.html)
- Register return listener to ensure that the message can be delivered to the target queue
- Append header to count requeue operations (x-rmqmgmt-move-count)
- Publish message with its body and properties as mandatory to the given exchange name and routing key
- Wait for confirmation
- Ensure no basic.return was received by return listener

In case of any error send nack with re-queuing or simply close the channel so that the message remains in the queue.

In case of moving all messages in the queue will stop when an error occurs or the message was already processed in the 
same execution to prevent an endless loop.

### Requeue operation

Requeue is only available for dead lettered messages. It is based on the x-death header with provides information about
the exchange name and routing key which was used to publish the message initially.

The following steps are applied to requeue a message

- Receive the first message of a queue
- Ensure checksum matches
- Ensure x-death header with exchange name and routing key is available
- Activate Publisher Acknowledgement (https://www.rabbitmq.com/confirms.html)
- Register return listener to ensure that the message can be delivered to the target queue
- Append header to count requeue operations (x-rmqmgmt-requeue-count)
- Publish message with its body and properties as mandatory to the exchange name and routing key from x-death header
- Wait for confirmation
- Ensure no basic.return was received by return listener

In case of any error send nack with re-queuing or simply close the channel so that the message remains in the queue.

In case of re-queueing all messages in the queue will stop when an error occurs, a message does not contain an x-death 
header with a exchange and routing key or the message was already processed in the same execution to prevent an endless 
loop.

# Configuration

As the application is based on Spring Boot the same rules applies to the configuration as described in the Spring Boot 
Documentation (http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-external-config).

The configuration file application.properties can be placed next to the application jar, in a sub-directory config or in 
any other location when specifying the location with the parameter `-Dspring.config.location=<path to config file>`.

The following paragraphs describe the application specific resp. pre-defined configuration parameters. If more 
configuration e.g. authentication is needed please check the Spring Boot Documentation for more detail.

## RabbitMQ Connection setup
The following snippet shows the configuration of a RabbitMQ connection with its default values.

```yaml
de:
  gessnerfl:
    rabbitmq:
      hostname: rabbitmq.mydomain.com  #The hostname of the rabbitmq host
      port: 5672                       #AMQP Port number
      managementPort: 15672            #RabbitMQ management port number
      managemnetPortSecured: false     #Indicator if the management interface is accessible via http (false) or https (true)
      username: guest                  #Username for connection to rabbitmq host
      password: guest                  #Password for connection to rabbitmq host
```

## LDAP Authentication
The following snippet shows the configuration of the optional LDAP Authentication. By default authentication is disabled.

```yaml
de:
  gessnerfl:
    security:
      authentication:
        enabled: true #enable authentication. By default authentication is disabled
        ldap:
          user-search-base:  ou=users,ou=myorg,dc=example,dc=com    #ldap user search base 
          user-search-filter: (uid={0})                             #ldap user search filter
          group-search-base: ou=groups,ou=myorg,dc=example,dc=com   #ldap group search base
          group-search-filter: member={0}                           #ldap group search filter
          contextSource:
            url: ldap://mylap.example.com:389   #ldap server URL
            root: dc=example,dc=com             #ldap root DN
            managerDn: manager                  #ldap manager DN
            managerPassword: fXLREqUsc6Ies      #ldap manager DN password
```

## Web UI
The following snippet shows the pre-defined web application configuration

    server.port=8780     #Port of the web interface
    management.port=8781 #Port of the http management api

