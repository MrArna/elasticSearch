CS441 @ UIC: HOMEWORK3
======================
Developed by Marco Arnaboldi (marnab2@uic.edu)

#Description
--------------------
A goal of this homework is to create a code search engine based on [ElasticSearch](https://www.elastic.co/about/partners/google-compute-engine). You will deploy your code search engine in the Google Cloud using your provided Google Cloud accounts. Your client program will take key words from users and then it will make a REST call to your web service that will use the key words to retrieve software projects where these key words are located. As parameters to your web service, clients can specify in what attributes they want to search the key words (e.g., language, committers, issues, or code) and how many results they want to retrieve

#Development & Design choices
-----------------

######Client 
The client script was developed using pyhton. Its main purpose is to provide a simple and intuitive tool to query the elastic cluster. 
It returns in JSON format the list of file, where the different keywords were found.

######Elastic Cluster
The ElasticSearch engine is deployed on the Google Cloud Platform. After a comparision between the development stack 
provided by [Elastic](https://www.elastic.co) and the one provided by [Bitnami](https://bitnami.com), I've decided to deploy the 
second one. This decison was taken, based on the fact that the Bitnami stack required less computational resources (e.g. VM) and it
was also integrated with a UI in order to work via browser with the search engine.

######Application
The application was developed with with IntelliJIDEA IDE. SBT was also exploited in order to manage the libraries. In particular it was developed using the following environment: OS X 10 native.
The application was written in Scala, adopting the Akka framework.

In the following schema is represented the general flow of communication between the actors.

![Alt text](https://bytebucket.org/MrArnab/marco_arnaboldi_cs441hw3/raw/eee3de1da7378a8a2048acea066835a45422c88c/images/HW3-flow.png?token=71ce8e0e4dd07115fc5a8fba12e1e61e0b069ce3)

In the second one, how is organized the actor system, using a telecommunication schema:

![Alt text](https://bytebucket.org/MrArnab/marco_arnaboldi_cs441hw3/raw/eee3de1da7378a8a2048acea066835a45422c88c/images/HW3-network.png?token=13681f734044ed5b96d48cbf656861b5f8e3a5df)


It has been designed in order to be as extendable as possible. In detail, it's composed by 3 modules composed by submodules and/or classes:

+ **App**: contains the object in charge to launch the actor system
    + *Elastic*: this object parses the args passed from the user and then instantiate the actor system with that configuration

+ **Actors**: contains all the actors that compose the Akka actor system
    + *Master*: this actor creates the pools of actors and manages the communication between pools
    + *Downloader*: this akka http actor is in charge to make an http request to the Olholo API rest service, parse its response and send
    the just retrieved data back to the master
    + *Parser*: this akka http actor is in charge to enrich the data that receive with a field that is a bag of word of the project, if its a public project that is possible to clone
    + *Uploader*: this akka http actor is in charge to upload the data to the elastic search cluster
    + *Sinker*: this akka actor is in charge to close the system and to terminate the application

+ **Messages**: contains the messages passed and received by the actors 
  
Further information about actors and messages can be found as comment into the code.

#Functionalities and Personal notes
----------------

#####Functionalities

The application downloads a number of project information from Olholo starting from a given project number. At the moment it is able to 
clone and analyze only projects developed using the SVN versioning system. The script provides some queries to the search engine.

#####Personal Notes

During the work I've noticed that only a small amount of the projects available on Olholo are publicly accessible. In fact the greater part of them asks for credential when I try to clone the repository locally.
This significantly reduced the amount of available projects for parsing. Furthermore, the limited amount of API requests per day limits the number of project to analyze to 500 per day, since my implementation makes 2 API calls for each project to be analyzed. 

#Usage
----------------

######Script

`python elasticQuery.py`

Tested and working with python 2.7.12.

#####Application

`sbt run -i <starting-project-id> -n <nr-of-projects>  -w <nr-of-workers>`

Git enabled for svn should be installed and configured for the terminal. Working on OSX.

#Test
----------------
##### TestKit
Automated tests with TestKit were made for actors in order to prove the correct behaviour. The actors tested were the HTTP actors,
since their behaviour is the most complicated. Also the master behaviour was tested.

##### Load test
Load tests were made in order to verify the scalability in term of response of the elastic cluster.
In order to do so the SOAPUI tool were used. Different requests were made using a "thread approach", this means that the
number of threads making the requests increased during a time window of 60 seconds. The following graphs represent the obtained results:

##### Credential
The followings are the credential needed in order to access the Elastic search cluster deployed on Google Clouds:

| Property      | Value     |
| ------------- | --------- |
| User          | user      |
| Password      | BwxuUA27  |


#Acknowledgments
---------------
Inspiration was taken by the Akka documentation and tutorials provided online.