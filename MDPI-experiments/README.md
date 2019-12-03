# IoT Ecosystem Simulator

The IoT ecosystem simulator aims at producing a set of endpoints for IoT devices that simulate a smart house. In addition, the simulator registers in a SPARQL repository the Thing Descriptions, and the related WoT-Mappings, into a triple store. Anytime the simulator deploys an IoT device it provides a file with historical consumption from a smart house. Then, the device chooses a random point in the historical data, and starts publishing such data (with the current date) from that point in time with a refresh of a minute.

## Running the simulator

Clone the eWoT repository, run eWoT along with a triple store. Go to the folder /MDPI-experiments/smart_house unzip the file *virtualLab.7z*. Once is unziped, navigate to the folder /virtualLab and run the following script

`````
bash deploy.sh [number of IoT devices]
`````

This script will start a number of IoT devices as specified in the previous capture. In addition, the script will register automatically their Thing Descriptions into the triple store. 
The script asummes that the triple store is a graphDB with a repository called discovery, which is under the address *http://localhost:7200/repositories/discovery/statements*. Nevertheless, if it would be required to change the address open the script and modify the first two lines that contain the address.
The first IoT device deployed is in the address localhost:8080, and, the rest are deployed incrementally in the following ports, i.e., the second will be in localhost:8081, the third in localhost:8082, and so on.
