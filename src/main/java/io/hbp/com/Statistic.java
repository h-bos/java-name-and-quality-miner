package io.hbp.com;

class Statistic
{
    String id;
    String value;

    Statistic(String id, int value)
    {
        this.id    = id;
        this.value = String.valueOf(value);
    }

    Statistic(String id, float value)
    {
        this.id = id;
        this.value = String.valueOf(value);
    }

    Statistic(String id, String value)
    {
        this.id = id;
        this.value = value;
    }
}
