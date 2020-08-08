package knowledge.graph.visualization.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class XJson
{
    private static ObjectMapper mapper = new ObjectMapper();

    static Logger log = LoggerFactory.getLogger(XJson.class);

    public static JavaType getCollectionType(Class<?> collectionClass,
                                             Class<?>... elementClasses)
    {
        return mapper.getTypeFactory().constructParametricType(collectionClass,
                elementClasses);
    }

    public static String getObjectFieldValue(String fieldName, Object o)
    {
        try
        {
            Field field = o.getClass().getDeclaredField(fieldName);
            //设置对象的访问权限，保证对private的属性的访问
            field.setAccessible(true);
            return String.valueOf(field.get(o));
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T decodeJson(byte[] src, Class<T> c)
    {
        try
        {
            return mapper.readValue(src, c);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static <T> T decodeJson(String src, Class<T> c)
    {
        try
        {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
            return mapper.readValue(src, c);
        }
        catch (Exception e)
        {
            log.error("decode json error: {}", src);
            log.error(e.getMessage(), e);
            return null;
        }
    }


    public static <T> List<T> decodeJsonList(String src, Class<T> c)
    {
        try
        {
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            JavaType javaType  = getCollectionType(mapper, ArrayList.class, c);
            return mapper.readValue(src, javaType);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static String encodeJson(Object o)
    {
        if (o == null)
        {
            return null;
        }

        try
        {
            mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return mapper.writeValueAsString(o);
        }
        catch (JsonProcessingException e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public static byte[] encodeJsonBytes(Object o)
    {
        if (o == null)
        {
            return null;
        }

        try
        {
            return mapper.writeValueAsBytes(o);
        }
        catch (JsonProcessingException e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    private static JavaType getCollectionType(ObjectMapper mapper,
                                              Class<?> collectionClass,
                                              Class<?>... elementClasses) {
        return mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }
}