from mq-utils.env import EnvStore
import os
import json
import datetime
import pymqi


import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


# function to establish connection to MQ Queue Manager
def connect():
    logger.info('Establising Connection with MQ Server')
    try:
        cd = None
        if not EnvStore.ccdtCheck():
            logger.info('CCDT URL export is not set, will be using json envrionment client connections settings')

            cd = pymqi.CD(Version=pymqi.CMQXC.MQCD_VERSION_11)
            cd.ChannelName = MQDetails[EnvStore.CHANNEL]
            cd.ConnectionName = conn_info
            cd.ChannelType = pymqi.CMQC.MQCHT_CLNTCONN
            cd.TransportType = pymqi.CMQC.MQXPT_TCP

            logger.info('Checking Cypher details')
            # If a cipher is set then set the TLS settings
            if MQDetails[EnvStore.CIPHER]:
                logger.info('Making use of Cypher details')
                cd.SSLCipherSpec = MQDetails[EnvStore.CIPHER]

        # Key repository is not specified in CCDT so look in envrionment settings
        # Create an empty SCO object
        sco = pymqi.SCO()
        if MQDetails[EnvStore.KEY_REPOSITORY]:
            logger.info('Setting Key repository')
            sco.KeyRepository = MQDetails[EnvStore.KEY_REPOSITORY]

        #options = pymqi.CMQC.MQPMO_NO_SYNCPOINT | pymqi.CMQC.MQPMO_NEW_MSG_ID | pymqi.CMQC.MQPMO_NEW_CORREL_ID
        options = pymqi.CMQC.MQPMO_NEW_CORREL_ID

        qmgr = pymqi.QueueManager(None)
        qmgr.connect_with_options(MQDetails[EnvStore.QMGR],
                                  user=credentials[EnvStore.USER],
                                  password=credentials[EnvStore.PASSWORD],
                                  opts=options, cd=cd, sco=sco)
        return qmgr

    except pymqi.MQMIError as e:
        logger.error("Error connecting")
        logger.error(e)
        return None


# function to establish connection to Queue
def getQueue():
    logger.info('Connecting to Queue')
    try:
        # Can do this in one line, but with an Object Descriptor
        # can or in more options.
        # q = pymqi.Queue(qmgr, MQDetails[EnvStore.QUEUE_NAME])
        q = pymqi.Queue(qmgr)

        od = pymqi.OD()
        od.ObjectName = MQDetails[EnvStore.QUEUE_NAME]
        q.open(od, pymqi.CMQC.MQOO_OUTPUT)

        return q

    except pymqi.MQMIError as e:
        logger.error("Error getting queue")
        logger.error(e)
        return None

# function to put message onto Queue
def putMessage():
    logger.info('Attempting put to Queue')
    try:
        md = pymqi.MD()
        md.Format = pymqi.CMQC.MQFMT_STRING
        # queue.put(json.dumps(msgObject).encode())
        # queue.put(json.dumps(msgObject))
        # queue.put(str(json.dumps(msgObject)))
        #queue.put(bytes(json.dumps(msgObject), 'utf-8'))
        queue.put(EnvStore.stringForVersion(json.dumps(msgObject)),md)

        logger.info("Put message successful")
    except pymqi.MQMIError as e:
        logger.error("Error in put to queue")
        logger.error(e)


def buildMQDetails():
    for key in [EnvStore.QMGR, EnvStore.QUEUE_NAME, EnvStore.CHANNEL, EnvStore.HOST,
                EnvStore.PORT, EnvStore.KEY_REPOSITORY, EnvStore.CIPHER]:
        MQDetails[key] = EnvStore.getEnvValue(key)

def parseArguments():
    topic = TOPICNAME
    size = NBRECORDS

    if len(sys.argv) == 1:
        print("Usage: ProduceInventoryEvents --size integer --topic topicname")
        exit(1)
    else:
        for idx in range(1, len(sys.argv)):
            arg=sys.argv[idx]
            if arg == "--size":
                sizeArg = sys.argv[idx+1]
                if sizeArg not in ['small','medium', 'large']:
                    size = int(sizeArg)
                if sizeArg == "medium":
                    size = 10000
                if sizeArg == "large":
                    size = 100000
            if arg == "--topic":
                topic =sys.argv[idx+1]
            if arg == "--help":
                print("Send n messages to a kafka cluster. Use environment variables KAFKA_BROKERS")
                print(" and KAFKA_APIKEY is the cluster accept sasl connection with token user")
                print(" and KAFKA_CERT to ca.crt path to add for TLS communication when using TLS")
                print(" --size small  | medium| large | a_number")
                print("        small= 1000| medium= 10k| large= 100k")
                print(" --topic topicname")
                exit(0)
    return size, topic

if __name__ == "__main__":
    size, queue = parseArguments()