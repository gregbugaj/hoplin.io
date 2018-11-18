package io.hoplin;

import java.util.HashMap;
import java.util.Map;

public class ConnectionStringParser
{
    private Map<String, OptionSetter> setters  = new HashMap<>();

    public static void parse(final String connectionString)
    {
        final String[] parts = connectionString.split(";");

        for(String part : parts)
        {
            final String[] kv = part.split("=");
            if(kv.length != 2)
                throw new RuntimeException("Invalid KeyValue pair");

        }
    }

    private class OptionSetter
    {

    }
}
