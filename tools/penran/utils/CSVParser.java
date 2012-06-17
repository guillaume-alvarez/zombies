package penran.utils;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Parse a CSV file into object. This is done by providing a factory, by
 * providing either a static or instance method (by providing either a class or
 * an object instance) marked with some annotation, or implementing an
 * interface:
 * <ul>
 * <li>{@link EntryBuidler}: used to implements user specified factory.
 * <li> {@link Builder}: mark the factory method if using reflection; this method
 * can be defined in any class, not necessarily in the built type.
 * <li> {@link Column}: marks parameter of the factory method to indicate where
 * goes each column of the CSV file. All parameters should have an annotation,
 * but not all column needs to be present.
 * </ul>
 *
 * @author gaetan
 */
public class CSVParser {

  /**
   * Interface for factories of object built from the CSV file entry.
   *
   * @author gaetan
   *
   * @param <T> The type of object built by the factory.
   */
  public interface EntryBuidler<T> {

    /**
     * Build a new object corresponding to a CSV line.
     *
     * @param columns Mapping between column names and their offset since start
     *          of line, the offset being the column number (not character
     *          offset).
     * @param elements Elements obtained from the current line.
     * @return A new object corresponding to the CSV line. If it returns null,
     *         the line is considered skipped.
     * @throws Exception If data is not valid, in which case the parsing process
     *           is halted.
     */
    T build(Map<String, Integer> columns, String[] elements) throws Exception;
  }

  /**
   * Marks a factory method.
   *
   * @author gaetan
   */
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Builder {
  }

  /**
   * Used on the parameters of a factory method, indicates which column should
   * be used to fill in the parameter.
   *
   * @author gaetan
   *
   */
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Column {
    /**
     * Name of the column bound to this parameter.
     *
     * @return A column name; it does not need to follow Java naming convention.
     */
    String value();

    /**
     * Whether an error should be thrown if the parameter is not set.
     *
     * @return True if parameter is required, false by default.
     */
    boolean required() default true;

    /**
     * Used to optionally specify a static class with a no parameter constructor
     * that can be used to parse incoming data.
     *
     * @return Converter from String to the parameter actual type.
     */
    Class<? extends Converter> converter() default DefaultConverter.class;
  }

  /**
   * Convert from String data to the actual parameter format.
   *
   * @author gaetan
   */
  public static interface Converter {

    /**
     * Convert data from the CSV file to the format accepted by the builder
     * method parameter.
     *
     * @param column The name of the column.
     * @param type The type of the parameter.
     * @param value The value as extracted from the CSV.
     * @return A value suitable for the builder method.
     * @throws Exception If an error occurs during the conversion.
     */
    Object convert(String column, Class<?> type, String value) throws Exception;
  }

  /**
   * Build a list of object from a CSV file.
   *
   * @param factory Factory for resulting objects.
   * @param separator Regex for separator between entries.
   * @param scanner A scanner used as input for the CSV data.
   * @return A list of objects built using factory.
   * @throws Exception Occurs if some conversion failed in input data.
   */
  public static <T> List<T> list(EntryBuidler<T> factory, String separator,
                                 Scanner scanner) throws Exception {
    List<T> r = new ArrayList<>();
    Map<String, Integer> ids = parseColumns(separator, scanner);

    while (scanner.hasNext()) {
      r.add(factory.build(ids, scanner.nextLine().split(separator)));
    }

    return r;
  }

  private static Map<String, Integer> parseColumns(String separator, Scanner sc) {
    Map<String, Integer> ids = new HashMap<>();

    for (String s : sc.nextLine().split(separator)) {
      ids.put(s, ids.size());
    }
    return ids;
  }

  /**
   * Create a builder using a static {@link Builder} method from provided class.
   */
  public static <T> EntryBuidler<T> builder(Class<?> cls) {
    return builder(cls, null);
  }

  private static <S, T> EntryBuidler<T> builder(Class<S> cls,
                                                final Object instance) {
    final Method r = getFactory(cls);

    class Conv {
      final Converter converter;
      final boolean required;
      final String name;
      final Class<?> type;
      private Conv(String name, Converter converter, boolean required, Class<?> type) {
        this.name = name;
        this.converter = converter;
        this.required = required;
        this.type = type;
      }
    }

    final List<Conv> converters = new ArrayList<>();

    Class<?>[] types = r.getParameterTypes();
    Annotation[][] ans = r.getParameterAnnotations();

    for (int i = 0; i < types.length; i++) {
      String key = null;
      Class<? extends Converter> conv = null;
      Class<?> type = null;
      boolean required = false;
      for (Annotation a : ans[i]) {
        if (a.annotationType() == Column.class) {
          Column c = ((Column) a);
          key = c.value();
          conv = c.converter();
          required = c.required();
        }
      }
      if (key == null) {
        throw new RuntimeException("Missing annotation on " + r);
      }

      try {
        converters.add(new Conv(key, conv.newInstance(), required, types[i]));
      }
      catch (InstantiationException | IllegalAccessException e) {
        throw new IllegalArgumentException("No no-args constructor in " + conv);
      }
    }
    return new EntryBuidler<T>() {
      Object[] buf = new Object[converters.size()];

      @Override
      public T build(Map<String, Integer> columns, String[] elements) throws Exception {
        for (int i = 0; i < converters.size(); i++) {
          Conv c = converters.get(i);
          buf[i] = c.converter.convert(c.name, c.type, elements[columns.get(c.name)]);
        }
        try {
          return (T) r.invoke(instance, buf);
        }
        catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  private static <T> Method getFactory(Class<T> cls) {
    for (Method m : cls.getMethods()) {
      if (m.isAnnotationPresent(Builder.class)) {
        return m;
      }
    }
    throw new RuntimeException("No factory found in " + cls);
  }

  /**
   * Create a builder using an instance {@link Builder} method from provided
   * object.
   */
  public static <T> EntryBuidler<T> builder(Object factory) {
    return builder(factory.getClass(), factory);
  }

  private static class DefaultConverter implements Converter {
    @Override
    public Object convert(String column, Class<?> type, String value) {
      if (type == String.class) {
        return value;
      }
      if (type == Integer.class || type == int.class) {
        return Integer.decode(value);
      }
      if (type == Double.class || type == double.class) {
        return Double.valueOf(value);
      }
      if (type == Long.class || type == long.class) {
        return Long.valueOf(value);
      }
      if (type == Boolean.class || type == boolean.class) {
        String v = value.toLowerCase();
        return v.equals("true") || v.equals("t") || v.equals("yes")
            || v.equals("y");
      }
      throw new RuntimeException("Unhandled type " + type + " for " + column);
    }
  }

}
