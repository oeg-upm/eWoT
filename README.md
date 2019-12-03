# eWoT

eWoT is an implementation that enables semantic interoperability for an IoT ecosystem. It relies on Thing Descriptions (TD) to profile the different IoT devices, and WoT-Mappings to translate their heterogeneus data into a normalised RDF modelled with a specific ontology. The ontologies endow for this purpose are the [Thing Description](http://iot.linkeddata.es/def/wot/index-en.html) and the [WoT-Mapping](http://iot.linkeddata.es/def/wot-mappings/index-en.html); nevertheless they can be extended with any other to enhance contextual information of the IoT device.

## Quickstart

Run the client jar with the following command

````
java -jar ewot.jar --server.port=9000 --server.repository=http://localhost:32768/repositories/discovery
````

* The statement *--server.port=9000* specifies that the client will be running on the port 9000. The port can be changed freely
* The statement *--server.repository=http://localhost:32768/repositories/discovery* specifies the URL of a SPARQL endpoint where all the descriptions that this client can discover will be allocated. This can be changed freely as far as the triple store implements SPARQL 1.1

## Registering a Thing Description

In order to include a new IoT device in the ecosystem, making it interoperable, a user must register in the SPARQL endpoint that eWoT relies on a Thing Description (TD). The thing description must be stored in a named graph which name is the subject that has the type **core:Thing**. The TD must contain a specification of where ther IoT device data is, by means of the Web of Things Thing Description ontology, and also, specify how such data is translated by means of a WoT-Mapping. Find below an example of TD with the WoT-Mappings.

## Issuing a SPARQL query in the ecosystem

In order to transparently query the ecosystem a SPARQL query must be issued to the endpoint *http://localhost:9000/sparql*. Alternativelly, if an user navigates with the browser to the very same address a GUI will be displayed where the SPARQL query could be written and its results displayed.
