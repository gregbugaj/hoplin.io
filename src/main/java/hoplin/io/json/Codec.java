package hoplin.io.json;

import java.lang.reflect.ParameterizedType;
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
     * 
     * <pre>
     * final BoStatus<Payload> val = codec.deserialize(serialize, new CodecTypeCapure<BoStatus<Payload>>()
     * {
     * }.getType());
     * </pre>
     * 
     * @param data
     * @param type
     * @return
     */
    default <E> E deserialize(final byte[] data, final Type type)
    {
        throw new RuntimeException("Not Implemented for this serializer");
    }

    /**
     * Class used during deserialization to determining type of the generics
     * 
     * Usage :
     * <pre>
     * private Type getResponseType()
     * {
     *     return new CodecTypeCapure<List<ByteBuffer>>()
     *     {
     *     }.getType();
     * }
     * </pre>
     * 
     * @param <T> type of capture
     */
    abstract class CodecTypeCapure<T>
    {
        private final Type type;

        public CodecTypeCapure()
        {
            type = capture();
        }

        public Type getType()
        {
            return type;
        }

        /** Returns the captured type. */
        final Type capture()
        {
            final Type superclass = getClass().getGenericSuperclass();
            return ((ParameterizedType) superclass).getActualTypeArguments()[0];
        }
    }
}
