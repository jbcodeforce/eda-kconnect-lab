import os

KAFKA_BROKERS = os.getenv("KAFKA_BROKERS","kafka1:9092")
KAFKA_USER = os.getenv("KAFKA_USER","")
KAFKA_PASSWORD = os.getenv("KAFKA_PASSWORD","")
KAFKA_CERT_PATH = os.getenv("KAFKA_CERT_PATH","")


def getBrokerEndPoints():
    return KAFKA_BROKERS

def getEndPointAPIKey():
    return KAFKA_APIKEY

def hasAPIKey():
    return KAFKA_APIKEY != ""

def isEncrypted():
    return KAFKA_CERT != ""

def getKafkaCertificate():
    return KAFKA_CERT

def getTopicName():
    return os.getenv("TOPIC_NAME","TestTopic")
