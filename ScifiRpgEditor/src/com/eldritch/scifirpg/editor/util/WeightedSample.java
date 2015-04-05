package com.eldritch.scifirpg.editor.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeSet;

public class WeightedSample<T> {
    private final double totalWeight;
    private final NavigableSet<WeightedElement<T>> selection = new TreeSet<>();
    private final WeightedElement<T> search = new WeightedElement<>(null, 0);

    public WeightedSample(Map<T, Double> weights) {
        // calculate the cumulative sum map
        double total = 0;
        for (Entry<T, Double> weight : weights.entrySet()) {
            total += weight.getValue();
            selection.add(new WeightedElement<>(weight.getKey(), total));
        }
        totalWeight = total;
    }

    public T sample() {
        search.cumulativeWeight = Math.random() * totalWeight;
        return selection.ceiling(search).elem;
    }
    
    private static class WeightedElement<T> implements Comparable<WeightedElement<T>> {
        private final T elem;
        private double cumulativeWeight;

        public WeightedElement(T elem, double weight) {
            this.elem = elem;
            this.cumulativeWeight = weight;
        }

        @Override
        public int compareTo(WeightedElement<T> other) {
            return Double.compare(this.cumulativeWeight, other.cumulativeWeight);
        }
    }
}