import json
import logging
from kafka import KafkaConsumer
import ssl


logging.basicConfig(level=logging.INFO)

SHOP_TOPIC_EVENT_NAME = "SHOP_TOPIC_EVENT"

consumer = KafkaConsumer(bootstrap_servers='instance-1:9093,instance-2:9093',
                          security_protocol='SSL',
                          ssl_password='password',
                          ssl_check_hostname=True,
                          value_deserializer=lambda x: json.loads(x.decode("utf-8")),
                          ssl_cafile='/data/certs/truststore.pem',
                          ssl_certfile='/data/certs/certificate.cer',
                          ssl_keyfile='/data/certs/privatekey.pem')
consumer.subscribe([SHOP_TOPIC_EVENT_NAME])


# Dictionary to store message counts by identifier
message_counts = {}

# Function to process shop events
def process_shop_event(shop_event):
    try:
        #shop_dto = shop_event["value"]
        #identifier = shop_dto["identifier"]
        #logging.info("Purchase received on topic: %s.", identifier)
        print("shop events:",shop_event)
        # Increment the message count for the identifier
   #     message_counts[identifier] = message_counts.get(identifier, 0) + 1

        # Process the shop event and update shop status
        # Replace the following code with your actual processing logic
        # shop = shopRepository.findByIdentifier(shop_dto["identifier"])
        # shop.setStatus(shop_dto["status"])
        # shopRepository.save(shop)

        logging.info("Message count for identifier %s: %d", identifier, message_counts[identifier])

    except Exception as e:
        logging.error("Error processing purchase %s", identifier)
        logging.exception(e)

print("Start")
# Start consuming messages from the Kafka stream
for message in consumer:
    print("start 0")
    shop_event = message.value
    process_shop_event(shop_event)
    print("start 2")
print("end")
