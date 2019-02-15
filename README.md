# choo_choo
Proof of Concept Weighted Shortest Path of Railroads

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
    
    
    // From Chicago to Milwaukee
    MATCH (chicago:Junction {fra_node_id: "414657"}), (milwaukee:Junction {fra_node_id: "412167"}),
        p = shortestPath((chicago)-[:LINKS*]-(milwaukee))
    RETURN p
    
    // From Chicago to Milwaukee on valid parts of the network
    MATCH (chicago:Junction {fra_node_id: "414657"}), (milwaukee:Junction {fra_node_id: "412167"}),
        p = shortestPath((chicago)-[:LINKS*]-(milwaukee))
    WHERE NONE( x IN relationships(p) WHERE x.network = "X" )  
    RETURN p
    
    // From Chicago to Milwaukee on valid parts of the network mileage
    MATCH (chicago:Junction {fra_node_id: "414657"}), (milwaukee:Junction {fra_node_id: "412167"}),
        p = shortestPath((chicago)-[:LINKS*]-(milwaukee))
    WHERE NONE( x IN relationships(p) WHERE x.network = "X" )  
    RETURN p, reduce(totalMiles = 0, n IN relationships(p) | totalMiles + n.miles) AS miles 
    
    // From Chicago to San Francisco
    MATCH (chicago:Junction {fra_node_id: "414657"}), (san_francisco:Junction {fra_node_id: "306128"}),
        p = shortestPath((chicago)-[:LINKS*]-(san_francisco))
    WHERE NONE( x IN relationships(p) WHERE x.network = "X" )  
    RETURN p, reduce(totalMiles = 0, n IN relationships(p) | totalMiles + n.miles) AS miles
    
    // TOP x From Chicago to Milwaukee on valid parts of the network mileage
    MATCH (chicago:Junction {fra_node_id: "414657"}), (san_francisco:Junction {fra_node_id: "306128"})
    CALL algo.shortestPath.stream(chicago, san_francisco, 'miles', 
        {relationshipQuery:'LINKS', defaultValue:1.0, direction:'BOTH'})
    YIELD nodeId, cost
    
    relationshipQuery:'MATCH(n:Loc)-[r:ROAD]->(m:Loc) RETURN id(n) as source, id(m) as target, r.cost as weight',
    graph:'cypher'