package hoplin.io.json;

import java.lang.reflect.Type;

/**
 * Codec interface used to encode/decode data
 */
public interface Codec
{
    /**
     * Serialize object to <code>byte[]</code>
     * 
     * @param value to serialize
     * @return serialized data
     */
    byte[] serialize(final Object value);

    /**
     * Serialize object to <code>byte[]</code>
     * @param value
     * @param clazz
     * @return
     */
    byte[] serialize(final Object value, Class<?> clazz);

    /**
     * Deserialize previously encoded data
     * 
     * @param data
     * @param clazz
     * @return
     */
    <E> E deserialize(byte[] data, Class<? extends E> clazz);

    /**
     * Deserialize data that might have generic type object
     * Sample usage

     * @param data
     * @param type
     * @return
     */
    default <E> E deserialize(final byte[] data, final Type type)
    {
        throw new RuntimeException("Not Implemented for this serializer");
    }
}
