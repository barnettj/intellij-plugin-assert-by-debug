package dev.fervento.assertbydebug;

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VirtualMachine;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserUtils {

    public static String format(String s, Object... vars) {
        return String.format(Locale.US, s, vars);
    }

    public static class GenericsHelper {
        private String signature;
        private Map<String, ReferenceType> genericMap = new HashMap<>();

        private static final Pattern PATTERN = Pattern.compile("([^:]+):([^:]+);");

        public GenericsHelper(VirtualMachine virtualMachine, String genericSignature) {
            int openTemplate = genericSignature.indexOf('<');
            int closeTemplate = genericSignature.indexOf('>');

            if (openTemplate >= 0 && closeTemplate >= 0) {
                genericSignature = genericSignature.substring(openTemplate+1, closeTemplate);
                Matcher matcher = PATTERN.matcher(genericSignature);
                while (matcher.find()) {
                    String label = matcher.group(1);
                    String className = new JNITypeResolver(matcher.group(2)).toString();
                    ReferenceType referenceType = virtualMachine.classesByName(className).get(0);
                    genericMap.put(label, referenceType);
                }
            }
        }

        public boolean matched() {
            return genericMap.isEmpty() == false;
        }

        public Map<String, ReferenceType> getGenericMap() {
            return genericMap;
        }
    }

    public static class JNITypeResolver {
        private String jniName;
        private StringBuilder stringBuilder = new StringBuilder();

        public JNITypeResolver(String jniName) {
            this.jniName = jniName;
            convertString(0);
        }

        public static String toJavaName(String s) {
            return s.replace('$', '.');
        }

        /**
         * <h2>Type Signatures</h2><br/>
         * The JNI uses the Java VM’s representation of type signatures. Table 3-2 shows
         * these type signatures.
         * <p/>
         * Table 3-2 Java VM Type Signatures
         *
         * <pre>
         * +---------------------------+-----------------------+
         * | Type Signature            | Java Type             |
         * +---------------------------+-----------------------+
         * | Z                         | boolean               |
         * +---------------------------+-----------------------+
         * | B                         | byte                  |
         * +---------------------------+-----------------------+
         * | C                         | char                  |
         * +---------------------------+-----------------------+
         * | S                         | short                 |
         * +---------------------------+-----------------------+
         * | I                         | int                   |
         * +---------------------------+-----------------------+
         * | J                         | long                  |
         * +---------------------------+-----------------------+
         * | F                         | float                 |
         * +---------------------------+-----------------------+
         * | D                         | double                |
         * +---------------------------+-----------------------+
         * | L fully-qualified-class ; | fully-qualified-class |
         * +---------------------------+-----------------------+
         * | [ type                    | type[]                |
         * +---------------------------+-----------------------+
         * | ( arg-types ) ret-type    | method type           |
         * +---------------------------+-----------------------+
         * </pre>
         *
         * For example, the Java method:
         *
         * <pre>
         * long f(int n, String s, int[] arr);
         * </pre>
         *
         * has the following type signature:
         *
         * <pre>
         * (ILjava/lang/String;[I)J
         * </pre>
         *
         * Note, for a constructor, supply &lt;init&gt; as the method name and void (V)
         * as the return type.
         *
         * @see <a href=
         *      "http://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/types.html#wp16432">Type
         *      Signatures</a>
         */
        public void convertString(int index) {
            char charType = jniName.charAt(index);
            switch (charType) {
                case 'V': {
                    stringBuilder.append("void");
                    break;
                }
                case 'Z': {
                    stringBuilder.append("boolean");
                    break;
                }
                case 'C': {
                    stringBuilder.append("char");
                    break;
                }
                case 'B': {
                    stringBuilder.append("byte");
                    break;
                }
                case 'S': {
                    stringBuilder.append("short");
                    break;
                }
                case 'I': {
                    stringBuilder.append("int");
                    break;
                }
                case 'F': {
                    stringBuilder.append("float");
                    break;
                }
                case 'J': {
                    stringBuilder.append("long");
                    break;
                }
                case 'D': {
                    stringBuilder.append("double");
                    break;
                }
                case '[': {
                    convertString(index+1);
                    stringBuilder.append("[]");
                    break;
                }
                case 'L': {
                    stringBuilder.append(
                        jniName.substring(index+1, jniName.length())
                                .replace('/', '.')
                    );
                    break;
                }
            }
        }

        @Override
        public String toString() {
            return stringBuilder.toString();
        }
    }

}
