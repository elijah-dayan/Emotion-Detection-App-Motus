

import speech_recognition as sr
import boto3
import os
import uuid
import logging
import json
logger = logging.getLogger()
logger.setLevel(logging.INFO)
r = sr.Recognizer() 
s3 = boto3.client('s3')


def lambda_handler(event, context):
  record_id = str(uuid.uuid4())
  bucket = event['Records'][0]['s3']['bucket']['name'] 
  logger.info(">>>>>>>>>>>>>>>"+bucket)
  key = event['Records'][0]['s3']['object']['key']
  logger.info(">>>>>>>>>>>>>>>"+key)
  try:
    meta = s3.head_object(Bucket=bucket, Key=key)['Metadata']
    filename = os.path.basename(key) 
    tmp = u'/tmp/' + filename 
    s3.download_file(Bucket=bucket, Key=key, Filename=tmp)
  except Exception as e:  
    print(e)

  audio_fl = tmp
  logger.info(">>>>>AUDIO_INFO>>>>>>"+audio_fl)
  with sr.AudioFile(audio_fl) as source:
    audio = r.record(source)
  try:
    text = r.recognize_google(audio)
  except sr.UnknownValueError:
    text = "UnknownValueError"
  except sr.RequestError as e:
    text = "RequestError"
                
  logger.info(">>>>>>TEXT_INFO>>>>>>>"+text)

  # dynamodb = boto3.resource('dynamodb')
  # table = dynamodb.Table(os.environ['DB_TABLE_NAME'])
  # logger.info("start")
  # table.put_item(
  #       TextConversionProcess={
  #           "processID": record_id,
  #           "processName": "PythonSpeechRecognition",
  #           "processType": "SpeechRecognition",
  #           "processOutput": tex
  #           }
  #   )
    
  message = {"fileName": filename, "transcript": text}
  sns = boto3.client('sns',aws_access_key_id='AKIAJM7TGXYSRZUJONWQ',aws_secret_access_key='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')
  # sns = boto3.client('sns')
  response = sns.publish(
    TargetArn= "arn:aws:sns:us-west-2:548009560556:lambda_channel_1",
    Message=json.dumps({'default': json.dumps(message)}),
    MessageStructure='json'
    )
  return record_id