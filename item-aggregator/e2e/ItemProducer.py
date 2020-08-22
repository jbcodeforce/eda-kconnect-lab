import logging
from kafka.KafkaProducer import KafkaProducer
import kafka.eventStreamsConfig as config

'''
Product some item sold event
'''
if __name__ == '__main__':
    print("Start Item Sold Event Producer")
    logging.basicConfig(level=logging.INFO)
    producer = KafkaProducer(kafka_brokers = config.KAFKA_BROKERS, 
                kafka_user = config.KAFKA_USER, 
                kafka_pwd = config.KAFKA_PWD, 
                kafka_cacert = config.KAFKA_CERT, 
                kafka_sasl_mechanism=config.KAFKA_SASL_MECHANISM,
                topic_name = "items")
    producer.prepare("ItemSoldProducer-1")
    item = {'storeName': "Store-1",'itemCode': 'Item-2', 'type': 'RESTOCK', 'quantity': 5}
    producer.publishEvent(item,"itemCode")
    item = {'storeName': "Store-1",'itemCode': 'Item-2', 'type': 'SALE', 'quantity': 2, 'price': 10.0}
    producer.publishEvent(item,"itemCode")
