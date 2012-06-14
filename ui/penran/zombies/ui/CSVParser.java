package penran.zombies.ui;

import java.lang.annotation.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Parse a CSV file into provided object entry.
 *
 * @author gaetan
 */
public class CSVParser {

  public interface EntryBuidler<T> {
    T build(Map<String, Integer> columns, String[] elements);
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Column {
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Builder {
  }

  public static <T> List<T> list(EntryBuidler<T> builder, String separator, Scanner sc) {
    List<T> r = new ArrayList<>();
    Map<String, Integer> ids = parseColumns(separator, sc);

    while (sc.hasNext()) {
      r.add(builder.build(ids, sc.nextLine().split(separator)));
    }

    return r;
  }

  public static <T> Map<String, T> map(EntryBuidler<T> builder, String key, String separator, Scanner sc) {
    Map<String, T> r = new HashMap<>();
    Map<String, Integer> ids = parseColumns(separator, sc);

    int k = ids.get(key);

    while (sc.hasNext()) {
      String[] p = sc.nextLine().split(separator);
      r.put(p[k], builder.build(ids, p));
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

  public static <S, T> EntryBuidler<T> builder(Class<T> cls) {
    return builder(cls, null);
  }

  private static <S, T> EntryBuidler<T> builder(Class<S> cls, final Object instance) {
    final Method r = getFactory(cls);

    final List<Converter> converters = new ArrayList<>();

    Class<?>[] types = r.getParameterTypes();
    Annotation[][] ans = r.getParameterAnnotations();

    for (int i = 0; i < types.length; i++) {
      String key = null;
      for (Annotation a : ans[i]) {
        if (a.annotationType() == Column.class) {
          Column c = ((Column) a);
          key = c.value();
        }
      }
      if (key == null) {
        throw new RuntimeException("Missing annotation on " + r);
      }
      Converter c;
      Class<?> t = types[i];
      if (t == String.class) {
        c = new Converter(key) {
          @Override
          public Object convert(String value) {
            return value;
          }
        };
      }
      else if (t == Integer.class || types[i] == int.class) {
        c = new Converter(key) {
          @Override
          public Object convert(String value) {
            return Integer.decode(value);
          }
        };
      }
      else if (t == Double.class || t == double.class) {
        c = new Converter(key) {
          @Override
          public Object convert(String value) {
            return Double.valueOf(value);
          }
        };
      }
      else if (t == Long.class || t == long.class) {
        c = new Converter(key) {
          @Override
          public Object convert(String value) {
            return Long.valueOf(value);
          }
        };
      }
      else if (t == Boolean.class || t == boolean.class) {
        c = new Converter(key) {
          @Override
          public Object convert(String value) {
            String v = value.toLowerCase();
            return v.equals("true") || v.equals("t") || v.equals("yes") || v.equals("y");
          }
        };
      }
      else
        throw new RuntimeException("Unhandled type " + t);
      converters.add(c);
    }
    return new EntryBuidler<T>() {
      Object[] buf = new Object[converters.size()];
      @Override
      public T build(Map<String, Integer> columns, String[] elements) {
        for (int i = 0; i < converters.size(); i++) {
          Converter c = converters.get(i);
          buf[i] = c.convert(elements[columns.get(c.column)]);
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

  public static <T> EntryBuidler<T> builder(Object factory) {
    return builder(factory.getClass(), factory);
  }

  private static abstract class Converter {
    public final String column;

    public Converter(String column) {
      this.column = column;
    }

    public abstract Object convert(String value);
  }
}
