# choo_choo
Proof of Concept Weighted Shortest Path of Railroads

This project requires Neo4j 3.5.x or higher

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/procedures-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/railroads-1.0-SNAPSHOT.jar neo4j-enterprise-3.5.2/plugins/.
    
Restart your Neo4j Server. A new Stored Procedure is available:

    com.maxdemarzi.routes(fromNode, toNode, numberOfPaths)

Import
------

Copy the rail_junctions.csv and rail_roads.csv files to your Neo4j /import directory. Then run these commands:


    USING PERIODIC COMMIT LOAD CSV WITH HEADERS FROM "file:///rail_junctions.csv" AS line WITH line
    CREATE (j:Junction {object_id:line.OBJECTID, fra_node_id:line.FRANODEID, 
                       latitude:toFloat(line.X), longitude:toFloat(line.Y),
                       location: point({latitude: toFloat(line.X), longitude: toFloat(line.Y), crs: 'WGS-84'}) })
    RETURN COUNT(*);

    CREATE CONSTRAINT ON (j:Junction) ASSERT j.fra_node_id IS UNIQUE;

    
    USING PERIODIC COMMIT LOAD CSV WITH HEADERS FROM "file:///rail_roads.csv" AS line WITH line
    MATCH (j1:Junction {fra_node_id:line.FRFRANODE}), (j2:Junction {fra_node_id:line.TOFRANODE})
    MERGE (j1)-[l:LINKS { object_id:line.OBJECTID, fra_aarc_id:line.FRAARCID, owner:line.RROWNER1, trackage:line.TRKRGHTS1 , tracks: toInteger(line.TRACKS), network:line.NET, miles:toFloat(line.MILES) }]->(j2)
    RETURN COUNT(*);
    
    
    // From Chicago to Milwaukee Shortest Number of Relationships
    MATCH (chicago:Junction {fra_node_id: "414657"}), (milwaukee:Junction {fra_node_id: "412167"}),
        p = shortestPath((chicago)-[:LINKS*]-(milwaukee))
    RETURN p
    
    // From Chicago to Milwaukee Shortest Number of Relationships on valid parts of the network
    MATCH (chicago:Junction {fra_node_id: "414657"}), (milwaukee:Junction {fra_node_id: "412167"}),
        p = shortestPath((chicago)-[:LINKS*]-(milwaukee))
    WHERE NONE( x IN relationships(p) WHERE x.network = "X" )  
    RETURN p
    
    // From Chicago to Milwaukee Shortest Number of Relationships on valid parts of the network with mileage
    MATCH (chicago:Junction {fra_node_id: "414657"}), (milwaukee:Junction {fra_node_id: "412167"}),
        p = shortestPath((chicago)-[:LINKS*]-(milwaukee))
    WHERE NONE( x IN relationships(p) WHERE x.network = "X" )  
    RETURN p, reduce(totalMiles = 0, n IN relationships(p) | totalMiles + n.miles) AS miles 
    
    // From Chicago to San Francisco Shortest Number of Relationships on valid parts of the network with mileage
    MATCH (chicago:Junction {fra_node_id: "414657"}), (san_francisco:Junction {fra_node_id: "306128"}),
        p = shortestPath((chicago)-[:LINKS*]-(san_francisco))
    WHERE NONE( x IN relationships(p) WHERE x.network = "X" )  
    RETURN p, reduce(totalMiles = 0, n IN relationships(p) | totalMiles + n.miles) AS miles
 
 
    // TOP 10 From Chicago to Milwaukee Lowest Mileage Path on valid parts of the network with mileage
     MATCH (chicago:Junction {fra_node_id: "414657"}), (milwaukee:Junction {fra_node_id: "412167"})
     CALL com.maxdemarzi.routes(chicago, milwaukee, 10)
     YIELD path, weight
     RETURN path, weight
        
    // TOP 10 From Chicago to San Francisco Lowest Mileage Path on valid parts of the network with mileage
    MATCH (chicago:Junction {fra_node_id: "414657"}), (san_francisco:Junction {fra_node_id: "306128"})
    CALL com.maxdemarzi.routes(chicago, san_francisco, 10)
    YIELD path, weight
    RETURN path, weight

    // TOP 10 From Chicago to San Francisco Lowest Mileage Path on valid parts of the network with path length and mileage
    MATCH (chicago:Junction {fra_node_id: "414657"}), (san_francisco:Junction {fra_node_id: "306128"})
    CALL com.maxdemarzi.routes(chicago, san_francisco, 10)
    YIELD path, weight
    RETURN length(path), weight
    