package io.hbp.com;

import java.util.*;

class ListUtils
{
    static List<String> join(List<String> l1, List<String> l2, List<String> l3, List<String> l4)
    {
        List<String> result = new ArrayList<>(l1.size() + l2.size() + l3.size() + l4.size());
        result.addAll(l1);
        result.addAll(l2);
        result.addAll(l3);
        result.addAll(l4);
        return result;
    }
}
