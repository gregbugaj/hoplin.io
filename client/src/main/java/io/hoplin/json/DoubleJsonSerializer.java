package io.hoplin.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;

public class DoubleJsonSerializer implements JsonSerializer<Double>
{
    @Override
    public JsonElement serialize(final Double src, final Type typeOfSrc, final JsonSerializationContext context)
    {
        BigDecimal value = BigDecimal.valueOf(src);

        try
        {
            value = new BigDecimal(value.toBigIntegerExact());
        } catch (ArithmeticException e)
        {
            // ignore
        }

        return new JsonPrimitive(value);
    }
}
