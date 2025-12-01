package ecommerce_app.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for JSON serialization and deserialization using Jackson's ObjectMapper.
 * This class provides methods to convert objects to JSON strings and vice versa.
 * It ensures a singleton instance of ObjectMapper for efficient usage.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class JsonUtils {

  private static ObjectMapper objectMapper;

  /**
   * Returns a singleton instance of ObjectMapper.
   *
   * @return an ObjectMapper instance
   */
  public static ObjectMapper getObjectMapper() {
    if (objectMapper == null) {
      objectMapper = new ObjectMapper();
    }
    return objectMapper;
  }

  /**
   * Converts a JSON string into an object of the specified class.
   *
   * @param json  the JSON string to deserialize
   * @param clazz the target class type
   * @param <T>   the type of the object
   * @return an instance of the specified class
   * @throws JsonProcessingException if deserialization fails
   */
  public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
    return getObjectMapper().readValue(json, clazz);
  }

  /**
   * Converts a JSON string into an object of the specified type reference.
   * Useful for handling generic types such as Lists and Maps.
   *
   * @param json          the JSON string to deserialize
   * @param typeReference the target type reference
   * @param <T>           the type of the object
   * @return an instance of the specified type
   * @throws JsonProcessingException if deserialization fails
   */
  public static <T> T fromJson(String json, TypeReference<T> typeReference)
          throws JsonProcessingException {
    return getObjectMapper().readValue(json, typeReference);
  }

  /**
   * Serializes an object into a JSON string.
   *
   * @param data the object to serialize
   * @param <T>  the type of the object
   * @return a JSON string representation of the object
   * @throws JsonProcessingException if serialization fails
   */
  public static <T> String toJson(T data) throws JsonProcessingException {
    return getObjectMapper().writeValueAsString(data);
  }
}
