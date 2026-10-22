package org.example.metrics;

public class Metrics {

    public long edgeExaminations;
    public long comparisons;
    public long findCalls;
    public long unionCalls;
    public long queuePushes;
    public long queuePops;

    @Override
    public String toString() {
        return "edgeExaminations=" + edgeExaminations
                + ", comparisons=" + comparisons
                + ", findCalls=" + findCalls
                + ", unionCalls=" + unionCalls
                + ", queuePushes=" + queuePushes
                + ", queuePops=" + queuePops;
    }
}