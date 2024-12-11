# FlinkCDC


## Introduction
An example application in which a database is replicated in real-time using Kafka Connect CDC,
and the normalized tables are denormalized using Flink embedded in a Spring Boot microservice.

As responsibilities go, the database, Kafka Connect and this denormalizing service are all owned by the same team.
The public viewable output is the denormalized/joined event stream produced by Flink.

## Usage / Demo

Start the services using `docker-compose up -d`. This will create Postgres, a Kafka broker (+ zookeeper still), 
and Kafka Connect configured with Debezium postgres CDC. Do check if the Debezium connector has been registered
using `docker logs connect`, as it may be that `connect` isn't up even if it should be.

When that has started you can start this application, either in Intellij, VS Code, or on the command
line: `mvn spring-boot:run`

## Tests

Integration tests are written using Testcontainers; to get that working on a Mac with Rancher Desktop
you probably have to check [the Rancher documentation for Testcontainers](https://docs.rancherdesktop.io/ui/preferences/application/general)

### Localhost Links

- Nothing at 8080 as my machine tends to run random stuff there :) 
- localhost:8082 : source app
- localhost:8083 : Kafka connect REST API
- localhost:18080 : Kafka UI. Broker to add is broker-1:9092 (local in docker)

## Database + CDC

As a database we'll use Postgres, and we'll use the Debezium CDC to get data out of there. Debezium has 2 options, the 
superior `decoderbufs` Postgres plugin, which needs to be installed on your Postgres installation, and the less
good but built-in `pgoutput` plugin. 

In this demo `pgoutput` will be used as that is applicable to most Postgres installations, managed cloud and on-prem.
If you run your own Postgres in containers, or have a large ops team that manages all your companies databases,
consider `decoderbufs` and managing that custom image, as it is a better choice.

### Configuration of Postgres

Postgres must start with `wal_level=logical`. This is not the default, and it is not easily configured as an 
environment parameter on the default Postgres docker images. As such, the docker-compose file contains a custom 
`command` that overrides the default `CMD=["postgres"]` and replaces it with `postgres -c wal_level=logical`.

Next to that a volume is configured that contains startup scripts. The Postgres image by default executes all
shell scripts and `.sql` files in that directory. In this directory we configure the starting database and 
schema and create the application and debezium roles (usernames + passwords + rights).

### Configuration of Debezium Permissions

### Configuration of Debezium CDC

While we won't configure security on our development Kafka the output streams reflect the database model and
as such these topics are not (supposed to be) visible for others.

We will have a very simple data structure:
```yaml

order:
  orderId: string
  customerId: String
  dateTimePlaced: datetime
  dateTimeModified: datetime
  paid: boolean
  sendToWarehouse: boolean
  orderItems: 
    - orderItemId: string
      productId: string
      quantity: integer
      pricePerCents: integer
      priceTotalCents: integer
      dateTimeModified: datetime
    - orderItemId: string
      productId: string
      quantity: integer
      pricePerCents: integer
      priceTotalCents: integer
      dateTimeModified: datetime
```

As you can see, this service owns both orders and order items and sends the details of those. It does
not own product information or customer information so it does not send those. Alternatively it could have a copy
of some of those fields to remember the product name or customer address at the time of order placement but that
is not the choice here.

We have 2 tables, orders and order items, and these will be joined. Please note that as order items are changed so
is the order (order dateTimeModified is max(orderItem.dateTimeModified)). This is a choice for convenience of the
consumer but also makes it easier to limit the size of the time windows when processing events.

TODO: Deal with collecting order items when they have not changed.


# How this came to be

Building docker-compose files in which you demonstrate an integration between systems is a lot of work. You probably
work with systems outside of your expertise. Blogs and other resources use a different version, or a different
container, or assume expert knowledge you just don't have.

To create the docker-compose file here, which integrates Postgres with Debezium, and uses Kafka Native, I have stopped
and started my containers countless times. It really is an unending iteration of:
- `docker-compose up -d` to start all containers
- `docker-compose ps` to see if everything started
- `docker logs <containername>` to see why it did not start, or what is going on in connect
- `curl -X POST --json @connector-config.json localhost:8083/connectors` to register the connector
- `docker logs connect` to see if the connector started successfully 
- `docker exec -it postgres bash` and then `psql -u postgres` `\c orders` to check what is happening in Postgres
- `docker-compose down -v`, the `-v` to remove all state that may have been preserved

# Some resources

- [Debezium home](https://debezium.io)
- [Debezium documentation: postgres connectors](https://debezium.io/documentation/reference/stable/connectors/postgresql.html)
- [Dockerhub: Debezium](https://hub.docker.com/u/debezium/)
- [Dockerhub: Kafka native](https://hub.docker.com/r/apache/kafka-native/)
- [Dockerhub: Use the official Postgres image](https://www.docker.com/blog/how-to-use-the-postgres-docker-official-image/)
- [Dockerhub: Postgres images](https://hub.docker.com/_/postgres/)
- [DZone: Configure Postgres with pgoutput plugin](https://dzone.com/articles/using-postgresql-pgoutput-plugin-for-change-data-c)
- [Jetbrains: HTMX w/ Boot](https://blog.jetbrains.com/idea/2024/09/introduction-to-htmx-for-spring-boot-developers/)
- [Icons from Google (Apache license)](https://fonts.google.com/icons)