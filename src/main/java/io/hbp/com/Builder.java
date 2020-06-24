package io.hbp.com;

import java.util.ArrayList;
import java.util.List;

public class Builder
{
    interface Buildy
    {

    }

    List<Buildy> all = new ArrayList<>();

    Builder add(Buildy buildy)
    {
        all.add(buildy);
        return this;
    }

    void build()
    {
    }

    static void test(Buildy buildy)
    {
        Builder builder = new Builder();
        builder
            .add(buildy)
            .add(buildy)
            .add(buildy)
            .build();
    }
}
