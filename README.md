# FlinkCDC


## Introduction
An example application in which a database is replicated in real-time using Kafka Connect CDC,
and the normalized tables are denormalized using Flink embedded in a Spring Boot microservice.

As responsibilities go, the database, Kafka Connect and this denormalizing service are all owned by the same team.
The public viewable output is the denormalized/joined event stream produced by Flink.

## Usage / Demo

Start the services using `docker-compose up -d`. This will create Postgres, a Kafka broker (+ zookeeper still), 
and Kafka Connect configured with Debezium postgres CDC.

When that has started you can start this application, either in Intellij, VS Code, or on the command
line: `mvn spring-boot:run`



## Database + CDC

As a database we'll use Postgres, and we'll use the Debezium CDC to get data out of there.

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