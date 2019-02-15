package com.maxdemarzi;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.neo4j.graphalgo.*;
import org.neo4j.graphdb.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.stream.Stream;

import static com.maxdemarzi.Heuristics.getCost;
import static com.maxdemarzi.schema.Properties.*;

public class Procedures {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    private static GraphDatabaseService graph;

    // Junctions don't tend to move, so we will cache their latitudes and longitudes.

    static final LoadingCache<Node, Double> latitudes = Caffeine.newBuilder()
            .maximumSize(1_000_000L)
            .build(Procedures::getJunctionLatitude);

    static final LoadingCache<Node, Double> longitudes = Caffeine.newBuilder()
            .maximumSize(1_000_000L)
            .build(Procedures::getJunctionLongitude);

    private static Double getJunctionLatitude(Node node) {
        return (double)node.getProperty(LATITUDE);
    }

    private static Double getJunctionLongitude(Node node) {
        return (double)node.getProperty(LONGITUDE);
    }

    @Procedure(name = "com.maxdemarzi.routes", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.routes(Node from, Node to, Number limit)")
    public Stream<WeightedPathResult> routes(@Name("from") Node from, @Name("to") Node to, @Name("limit") Number limit) {
        // Initialize graph for cache
        if (graph == null) {
            graph = this.db;
        }

        // Prevent going to any Junction more than 120% distance away from source to destination
        double maxCost = getCost(from, to) * 1.2;
        ValidPathExpander validPaths = new ValidPathExpander(maxCost, latitudes.get(to), longitudes.get(to));

        PathFinder<WeightedPath> dijkstra = GraphAlgoFactory.dijkstra(validPaths,
                CommonEvaluators.doubleCostEvaluator(MILES), limit.intValue() );

        ArrayList<WeightedPathResult> results = new ArrayList<>();
        for(WeightedPath path : dijkstra.findAllPaths(from, to)) {
            results.add(new WeightedPathResult(path, path.weight()));
        }

        return results.stream();
    }


}
