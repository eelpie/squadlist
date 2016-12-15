package uk.co.squadlist.web.localisation;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

abstract public class BaseGoverningBody {

    private static final List<Integer> BOAT_SIZES = Lists.newArrayList(1, 2, 4, 8);

    final public List<Integer> getBoatSizes() {
        return BOAT_SIZES;
    }

    final public Map<Integer, String> getWeights() {
        Map<Integer, String> weights  = Maps.newLinkedHashMap();
        for(int i = 40; i<= 200; i++) {
            weights.put(i, i + "kg");
        }
        return weights;
    }

}
