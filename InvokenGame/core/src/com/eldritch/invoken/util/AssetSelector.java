package com.eldritch.invoken.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.protobuf.Message;

public abstract class AssetSelector<V extends Message> {
    private final Map<Integer, Set<V>> levelToAsset = new HashMap<>();
    private final Map<String, V> assets = new HashMap<>();

    public void load() {
        levelToAsset.clear();
        assets.clear();

        // read from disk and index by the min level
        // when selecting we filter out all from [0, level] whose max level < level
        loadInto(levelToAsset, assets);
    }

    public List<V> select(int level) {
        List<V> results = new ArrayList<>();
        for (int i = 0; i <= level; i++) {
            for (V asset : get(levelToAsset, level)) {
                if (isValid(asset, level)) {
                    results.add(asset);
                }
            }
        }
        return results;
    }

    public V get(String id) {
        return assets.get(id);
    }

    private <K> Set<V> get(Map<K, Set<V>> map, K key) {
        if (!map.containsKey(key)) {
            return ImmutableSet.of();
        }
        return map.get(key);
    }

    protected abstract void loadInto(Map<Integer, Set<V>> levelToAsset, Map<String, V> assets);

    protected abstract boolean isValid(V asset, int level);
}
