package com.maxdemarzi;

import org.neo4j.graphdb.Path;

public class WeightedPathResult {
    public Path path;
    public double weight;

    public WeightedPathResult(Path path, double weight) {
        this.path = path;
        this.weight = weight;
    }

    public int getLength() {
        return path.length();
    }

    public double getWeight() {
        return weight;
    }
}
