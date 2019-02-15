package com.maxdemarzi;

import org.neo4j.graphalgo.*;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.stream.Stream;

import static com.maxdemarzi.schema.Properties.MILES;

public class Procedures {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static final ValidPathExpander validPaths = new ValidPathExpander();
    private static final DistanceEstimateEvaluator distanceHeuristic = new DistanceEstimateEvaluator();

    @Procedure(name = "com.maxdemarzi.routes", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.routes(Node from, Node to, Number limit)")
    public Stream<WeightedPathResult> routes(@Name("from") Node from, @Name("to") Node to, @Name("limit") Number limit) {
        PathFinder<WeightedPath> aStar = GraphAlgoFactory.aStar(validPaths,
                CommonEvaluators.doubleCostEvaluator(MILES), distanceHeuristic );

        ArrayList<WeightedPathResult> results = new ArrayList<>();
        int count = 0;
        WeightedPath single = aStar.findSinglePath(from, to);
        results.add(new WeightedPathResult(single, single.weight()));

//        for (WeightedPath path : aStar.findAllPaths(from, to)) {
//            results.add(new WeightedPathResult(path, path.weight()));
//            if(count++ >= limit.intValue() ) {
//                break;
//            }
//        }

        return results.stream();
    }

}
