# Generic Semantic Interoperability Client (GSIC)

The GSIC is a generic software agent that is able to discover and access distributed enpoints that expose data. 

## Quickstart

Run the client jar with the following command

````
java -jar gsic.jar --server.port=9000 --server.repository=http://localhost:32768/repositories/discovery
````

* The statement *--server.port=9000* specifies that the client will be running on the port 9000. The port can be changed freely
* The statement *--server.repository=http://localhost:32768/repositories/discovery* specifies the URL of a SPARQL endpoint where all the descriptions that this client can discover will be allocated. This can be changed freely as far as the triple store implements SPARQL 1.1