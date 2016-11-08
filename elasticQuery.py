import requests
from requests.auth import HTTPBasicAuth
import urllib
import json

server = "http://146.148.93.156:80/elasticsearch/"
auth=HTTPBasicAuth("user","BwxuUA27")

def nameQuery():
  keywords = raw_input("Enter keyword: ")
  response = requests.get(server + "projects/_search?q=name:" + keywords, auth = auth)
  json_data = json.loads(response.text)
  print (json.dumps(json_data, indent=4, sort_keys=True))

def descriptionQuery():
  keywords = raw_input("Enter keyword: ")
  response = requests.get(server + "projects/_search?q=description:" + keywords, auth = auth)
  json_data = json.loads(response.text)
  print (json.dumps(json_data, indent=4, sort_keys=True))

def languageQuery():
  keywords = raw_input("Enter keyword: ")
  response = requests.get(server + "projects/_search?q=analysis.main_language_name:" + keywords, auth = auth)
  json_data = json.loads(response.text)
  print (json.dumps(json_data, indent=4, sort_keys=True))

def structureQuery():
  keywords = raw_input("Enter keyword: ")
  response = requests.get(server + "projects/_search?q=sourceCode:" + keywords, auth = auth)
  json_data = json.loads(response.text)
  print (json.dumps(json_data, indent=4, sort_keys=True))

def contributorQuery():
  keywords = raw_input("Enter nr. of contributor: ")
  response = requests.get(server + "projects/_search?q=project_activity_index.description:" + keywords, auth = auth)
  json_data = json.loads(response.text)
  print (json.dumps(json_data, indent=4, sort_keys=True))

def inactiveQuery():
  keywords = "Inactive"
  response = requests.get(server + "projects/_search?q=project_activity_index.description:" + keywords, auth = auth)
  json_data = json.loads(response.text)
  print (json.dumps(json_data, indent=4, sort_keys=True))

ans = True

while ans:  
  print("List of available quueries:")
  print("1 - Project name")
  print("2 - Project description")
  print("3 - Language")
  print("4 - Data structure")
  print("5 - Inactive")
  print("6 - Contributors")
  print("7 - Exit")

  ans = raw_input("Your decision: ")

  if ans=="1": 
    nameQuery()
  elif ans=="2":
    descriptionQuery()
  elif ans=="3":
    languageQuery()
  elif ans=="4":
    structureQuery()
  elif ans=="5":
    inactiveQuery()
  elif ans=="6":
    contributorQuery()
  elif ans=="7":
    print("\n Goodbye") 
    ans = False
  elif ans !="":
    print("\n Not Valid Choice Try again") 


