package com.maxdemarzi;

import org.junit.Rule;
import org.junit.Test;
import org.neo4j.driver.v1.*;
import org.neo4j.harness.junit.Neo4jRule;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.neo4j.driver.v1.Values.parameters;

public class RoutesTest {
    @Rule
    public final Neo4jRule neo4j = new Neo4jRule()
            .withProcedure(Procedures.class)
            .withFixture(MODEL_STATEMENT);

    @Test
    public void shouldFindRoutes()
    {
        // In a try-block, to make sure we close the driver after the test
        try( Driver driver = GraphDatabase.driver( neo4j.boltURI() , Config.build().withoutEncryption().toConfig() ) )
        {
            // Given I've started Neo4j with the procedure
            //       which my 'neo4j' rule above does.
            Session session = driver.session();

            // When I use the procedure
            StatementResult result = session.run( "MATCH (j1:Junction { fra_node_id:'j1' }), (j6:Junction { fra_node_id:'j6' }) WITH j1, j6 CALL com.maxdemarzi.routes(j1, j6, $limit) YIELD path, weight RETURN path, weight",
                    parameters("limit", 2));

            // Then I should get what I expect
            assertThat(result.next().get("weight").asDouble(), equalTo(12.0));
            assertThat(result.next().get("weight").asDouble(), equalTo(13.0));
        }
    }

    private static final String MODEL_STATEMENT =
            "CREATE (j1:Junction { fra_node_id:'j1', latitude: -122.766750577806476, longitude: 45.593594410320861 })" +
            "CREATE (j2:Junction { fra_node_id:'j2', latitude: -122.766664795802114, longitude: 45.641088785037418 })" +
            "CREATE (j3:Junction { fra_node_id:'j3', latitude: -122.766608304775175, longitude: 45.593653799868534 })" +
            "CREATE (j4:Junction { fra_node_id:'j4', latitude: -122.766510452945582, longitude: 45.606348429447735 })" +
            "CREATE (j5:Junction { fra_node_id:'j5', latitude: -122.766456128389734, longitude: 45.925653170922408 })" +
            "CREATE (j6:Junction { fra_node_id:'j6', latitude: -122.766444127812449, longitude: 45.925562171940499 })" +
            "CREATE (j1)-[:LINKS { network: 'M', miles:10.0 }]->(j2)" +
            "CREATE (j1)-[:LINKS { network: 'M', miles:10.0 }]->(j3)" +
            "CREATE (j1)-[:LINKS { network: 'M', miles:10.0 }]->(j4)" +
            "CREATE (j1)-[:LINKS { network: 'M', miles:10.0 }]->(j5)" +
            "CREATE (j2)-[:LINKS { network: 'M', miles:4.0 }]->(j6)" +
            "CREATE (j3)-[:LINKS { network: 'M', miles:3.0 }]->(j6)" +
            "CREATE (j4)-[:LINKS { network: 'M', miles:2.0 }]->(j6)" +
            "CREATE (j5)-[:LINKS { network: 'X', miles:1.0 }]->(j6)";
}
