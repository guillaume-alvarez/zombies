package penran.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.channels.FileLock;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various utilities methods.
 * 
 * @author gaetan
 */
public class Util {

  public static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private Util() {
    /* */
  }

  /**
   * Generates an unmodifiable mapping between an enum instances and an index,
   * the index being given by an accessor. The method will try to either get a
   * field or call a method with name "accessor", and use the result as key in
   * the mapping.
   * <p>
   * This methods was created to alleviate the amount of boiler plate that is
   * required to generate a map from an enum:
   * 
   * <pre>
   * enum E {
   *   A(-1), B(2), C(4);
   *   public final int value;
   *   E(int i) { value = i; }
   * 
   *   private static final fromId = makMapping(E.class, "value");
   * }
   * ...
   * E var = E.fromId(2); // var == B;
   * </pre>
   * 
   * @param <T> Key type.
   * @param <X> The enum type.
   * @param c The class of the enum.
   * @param acc The field or method name. In either case, it should be a public
   *          member, and if it is a method, it should have no parameters.
   * @return An unmodifiable mapping.
   */
  @SuppressWarnings("unchecked")
  public static <T, X> Map<T, X> makeMapping(Class<X> c, String acc) {
    Map<T, X> r = new HashMap<T, X>();
    try {
      Method m = c.getMethod(acc.trim());
      for (X e : c.getEnumConstants())
        r.put((T) m.invoke(e), e);
    } catch (NoSuchMethodException ex) {
      try {
        Field f = c.getField(acc.trim());
        if (f != null) {
          for (X e : c.getEnumConstants())
            r.put((T) f.get(e), e);
        }
      } catch (NoSuchFieldException e) {
        throw new IllegalArgumentException("No field or method names " + acc);
      } catch (Exception e) {
        throw new IllegalArgumentException("Error while accessing data using field " + acc);
      }
    } catch (Exception e) {
      throw new IllegalArgumentException("Error while accessing data using method " + acc);
    }
    return Collections.unmodifiableMap(r);
  }

  /**
   * This method returns a logger associated with the name of the class of the
   * method calling it...
   * <p>
   * For instance:
   * 
   * <pre>
   * package gv.commons;
   * 
   * import static gv.util.Util.logger;
   * 
   * class MyClass {
   *   private static Logger l = logger(); // category of &quot;l&quot; is &quot;gv.commons.MyClass&quot;
   * };
   * </pre>
   * 
   * @return A logger, corresponding to the class of the method calling this.
   */
  public static Logger logger() {
    return LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
  }

  /**
   * Retrieve the extension of a file (everything after the last dot, so for a
   * archive.tar.bz2 it will be bz2, for document.legal, it will be legal, ...)
   */
  public static String extension(File file) {
    String[] n = file.getName().split("\\.");
    return n.length > 1 ? n[n.length - 1] : "";
  }

  /**
   * Give back the home dir of the user, where data should be put.
   */
  public static File getHomeDir() {
    return new File(System.getProperty("user.home"));
  }

  /**
   * Try to acquire a working set directory, and register an hook to get the
   * corresponding lock file removed on shutdown.
   * 
   * @param workingDir Directory where the application will lay its data.
   * @param applog A logger where additional additional information about
   *          failure may go.
   * @throws IOException
   */
  public static void acquireWorkingDirectory(final File workingDir, final Logger applog) throws IOException {
    final File lockFile = new File(workingDir, "lock");
    try (RandomAccessFile file = new RandomAccessFile(lockFile, "rw")) {
      final FileLock lock = file.getChannel().tryLock();
      if (lock == null)
        throw new IOException("Another instance is already using the working dir " + workingDir);

      Runtime.getRuntime().addShutdownHook(new Thread() {
        @Override
        public void run() {
          try {
            lock.release();
            lock.channel().close();
            lockFile.delete();
          } catch (IOException e) {
            applog.error("Could not release lock on working dir " + workingDir);
          }
        }
      });
    }
  }

  /**
   * Convert an array of value into a set.
   */
  public static <T> Set<T> asSet(T ... v) {
    Set<T> s = new HashSet<T>();
    for (T t : v)
      s.add(t);
    return s;
  }

  public static void die(String message) {
    System.err.println("Quitting program: " + message);
    System.exit(1);
  }

  public static FilenameFilter getFileFilter(String regex) {
    final Pattern p = Pattern.compile(regex);
    return (dir, name) -> p.matcher(name).find();
  }

  /**
   * Convert a list of element into a map using the parameter field as a key
   * extractor.
   * 
   * @param values The list of values for the map.
   * @param keyField The key field used for the mapping.
   * @return A map containing
   * 
   *         <pre>
   * {v[keyField] -> v | v : values}
   * </pre>
   * @throws IllegalArgumentException If an error occurs while using keyField.
   */
  public static <K, V> Map<K, V> asMap(List<V> values, Field keyField) throws IllegalArgumentException {
    Map<K, V> r = new HashMap<>(values.size());
    for (V v : values) {
      try {
        r.put((K) keyField.get(v), v);
      } catch (IllegalAccessException e) {
        throw new IllegalArgumentException("Field " + keyField + " is not accessible in " + v);
      }
    }
    return r;
  }

  public static boolean foundAt(CharSequence sequence, CharSequence checked, int offset) {
    int i = 0;
    while (i < sequence.length() && i + offset < checked.length()) {
      if (checked.charAt(i) != sequence.charAt(i)) {
        return false;
      }
      i++;
    }
    return i == sequence.length();
  }
}
