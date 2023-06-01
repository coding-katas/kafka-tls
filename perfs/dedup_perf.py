import json
import logging
from kafka import KafkaConsumer
import ssl
import requests
import threading
import time

logging.basicConfig(level=logging.INFO, filename='logfile.log')

start_thread_request = True

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
received_messages = 0
dedup_count = 5
nb_messages = 6
request_count = dedup_count * nb_messages

def post_shop_request(buyer_identifier, product_identifier, amount, price):
    url = 'http://localhost:8080/shop'
    headers = {'Content-Type': 'application/json'}
    data = {
        'buyerIdentifier': buyer_identifier,
        'items': [
            {
                'productIdentifier': product_identifier,
                'amount': amount,
                'price': price
            }
        ]
    }
    response = requests.post(url, headers=headers, data=json.dumps(data))
    return response.json()

def send_requests():
    global start_thread_request
    product_identifier = "123456789"
    amount = "10"
    price = "1000"
    new_id = 0
    if start_thread_request == True:
        print("waiting...")
        time.sleep(2)
        start_thread_request = False
    print("starting requests")
    for i in range(request_count):
        identifier = "ID-" + str(new_id)
        response = post_shop_request(identifier, product_identifier, amount, price)
        #print(response)
        if (i%dedup_count==0):
           print("sended",i,"messages", "last one:", response)
           new_id += 1

def process_shop_event(shop_event):
    global received_messages
    global message_counts
    try:
        identifier = shop_event['buyerIdentifier']
        if identifier not in message_counts:
            message_counts[identifier] = 0
        message_counts[identifier] += 1
        received_messages += 1

        logging.info("Message count for identifier %s: %d", identifier, message_counts[identifier])
        current_time = time.time()
        event_timestamp = shop_event["timeShop"]
        time_diff = (current_time * 1000) - event_timestamp
#        print("event ID",shop_event["buyerIdentifier"] ,"after :",time_diff);

        logging.info("Event ID %s arrive after: %d", identifier, time_diff)

    except Exception as e:
        print("Error processing purchase:", identifier,e)
        logging.exception(e)

# Create a separate thread to send requests
request_thread = threading.Thread(target=send_requests)
request_thread.start()

# Start consuming messages from the Kafka stream
for message in consumer:
    shop_event = message.value
    process_shop_event(shop_event)
#    print("Received messages:",received_messages, len(message_counts))
    if len(message_counts) == nb_messages:
        print(message_counts)
        # Check if all values in message_counts are equal to 1
        if all(value == 1 for value in message_counts.values()):
            print("Test OK")
        else:
            print("Test Fail")
        break

request_thread.join()
