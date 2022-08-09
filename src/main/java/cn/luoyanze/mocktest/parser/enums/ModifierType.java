package cn.luoyanze.mocktest.parser.enums;


public enum ModifierType {

    PUBLIC("public"),
    PRIVATE("private"),
    PROTECTED("protected"),

    ABSTRACT("abstract"),

    FINAL("final"),
    STATIC("static"),

    VOLATILE("volatile"),
    TRANSIENT("transient"),
    NATIVE("native"),
    SYNCHRONIZED("synchronized"),

    NONE("")
    ;


    public String value;

    ModifierType(String value) {
        this.value = value;
    }

    public static String removeAllModifier(String origin) {
        String temp = origin;
        for (ModifierType modifier : ModifierType.values()) {
            temp = temp.replaceFirst("([\n| ])" + modifier + " ", "$1");
        }
        return temp;
    }

    public static ModifierType getVisitType(String modifier) {
        if (modifier.contains(PUBLIC.value)) {
            return PUBLIC;
        } else if (modifier.contains(PRIVATE.value)) {
            return PRIVATE;
        } else if (modifier.contains(PROTECTED.value)) {
            return PROTECTED;
        }
        else return NONE;
    }

    public static boolean isAbstract(String modifier) {
        return modifier.contains(ABSTRACT.value);
    }

    public static boolean isStatic(String modifier) {
        return modifier.contains(STATIC.value);
    }

    public static boolean isFinal(String modifier) {
        return modifier.contains(FINAL.value);
    }

    public static boolean isVolatile(String modifier) {
        return modifier.contains(VOLATILE.value);
    }

    public static boolean isTransient(String modifier) {
        return modifier.contains(TRANSIENT.value);
    }

    public static boolean isNative(String modifier) {
        return modifier.contains(NATIVE.value);
    }

    public static boolean isSynchronized(String modifier) {
        return modifier.contains(SYNCHRONIZED.value);
    }

}
