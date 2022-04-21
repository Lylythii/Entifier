package wiresegal.thicc.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

// Boilerplate code taken with love from Vazkii's Quark mod and JamiesWhiteShirt's Clothesline
// Quark is distributed at https://github.com/Vazkii/Quark
// Clothesline is distributed at https://github.com/JamiesWhiteShirt/clothesline

public class ThiccAsmTransformer implements IClassTransformer, Opcodes {

    private static final String ASM_HOOKS = "wiresegal/thicc/asm/hooks/ThiccAsmHooks";
    private static final Map<String, Transformer> transformers = new HashMap<>();

    static {
        transformers.put("net.minecraft.entity.Entity", ThiccAsmTransformer::transformEntity);
//        transformers.put("net.minecraft.entity.player.EntityPlayer", ThiccAsmTransformer::transformEntityPlayer);
        transformers.put("net.minecraft.client.renderer.entity.Render", ThiccAsmTransformer::transformRender);
    }


    private static final String ENTITY = "net/minecraft/entity/Entity";

    private static byte[] transformEntity(byte[] basicClass) {
        MethodSignature size = new MethodSignature("setSize", "func_70105_a", "(FF)V");

        return transform(basicClass, size, "Size hook", (method) -> {
            InsnList newInstructions = new InsnList();

            newInstructions.add(new VarInsnNode(ALOAD, 0));
            newInstructions.add(new VarInsnNode(FLOAD, 1));
            newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "rescale", "(L" + ENTITY + ";F)F", false));
            newInstructions.add(new VarInsnNode(FSTORE, 1));

            newInstructions.add(new VarInsnNode(ALOAD, 0));
            newInstructions.add(new VarInsnNode(FLOAD, 2));
            newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "rescale", "(L" + ENTITY + ";F)F", false));
            newInstructions.add(new VarInsnNode(FSTORE, 2));

            method.instructions.insertBefore(method.instructions.getFirst(), newInstructions);

            return true;
        });
    }

/*
	Conflicts with Aqua Acrobatics.
*/

/*
    private static byte[] transformEntityPlayer(byte[] basicClass) {
        MethodSignature get = new MethodSignature("getEyeHeight", "func_70047_e", "()F");
        MethodSignature set = new MethodSignature("getDefaultEyeHeight", "", "()F");

        return transform(transform(transform(basicClass,
                set, "Default eye height", combine(
                node -> node.getOpcode() == FRETURN,
                (method, retNode) -> {
                    InsnList newInstructions = new InsnList();

                    newInstructions.add(new VarInsnNode(ALOAD, 0));
                    newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "rescale", "(FL" + ENTITY + ";)F", false));

                    method.instructions.insertBefore(retNode, newInstructions);

                    return false;
                })),

                get, "Rescale original height", combine(
                node -> node.getOpcode() == GETFIELD && ((FieldInsnNode) node).name.equals("eyeHeight"),
                (method, fieldNode) -> {
                    InsnList newInstructions = new InsnList();

                    newInstructions.add(new VarInsnNode(ALOAD, 0));
                    newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "descale", "(FL" + ENTITY + ";)F", false));

                    method.instructions.insert(fieldNode, newInstructions);

                    return false;
                })),

                get, "Eye height hook", combine(
                node -> node.getOpcode() == FRETURN,
                (method, retNode) -> {
                    InsnList newInstructions = new InsnList();

                    newInstructions.add(new VarInsnNode(ALOAD, 0));
                    newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "rescale", "(FL" + ENTITY + ";)F", false));

                    method.instructions.insertBefore(retNode, newInstructions);

                    return false;
                }));
    }
*/

    private static byte[] transformRender(byte[] basicClass) {
        MethodSignature shadow = new MethodSignature("renderShadow", "func_76975_c",
                "(L" + ENTITY + ";DDDFF)V");

        FieldSignature size = new FieldSignature("shadowSize", "field_76989_e",
                "F");

        return transform(basicClass, shadow, "Resize shadow", combine(
                (node) -> node.getOpcode() == GETFIELD && size.matches((FieldInsnNode) node),
                (method, node) -> {
                    InsnList newInstructions = new InsnList();

                    newInstructions.add(new VarInsnNode(ALOAD, 1));
                    newInstructions.add(new MethodInsnNode(INVOKESTATIC, ASM_HOOKS, "rescale", "(FL" + ENTITY + ";)F", false));

                    method.instructions.insert(node, newInstructions);

                    return false;
                }));
    }

    // BOILERPLATE =====================================================================================================

    public static byte[] transform(byte[] basicClass, MethodSignature sig, String simpleDesc, MethodAction action) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode node = new ClassNode();
        reader.accept(node, 0);

        log("Applying Transformation to method (" + sig + ")");
        log("Attempting to insert: " + simpleDesc);
        boolean didAnything = findMethodAndTransform(node, sig, action);

        if (didAnything) {
            ClassWriter writer = new SafeClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            node.accept(writer);
            return writer.toByteArray();
        }

        return basicClass;
    }

    public static boolean findMethodAndTransform(ClassNode node, MethodSignature sig, MethodAction pred) {
        for (MethodNode method : node.methods) {
            if (sig.matches(method)) {

                boolean finish = pred.test(method);
                log("Patch result: " + (finish ? "Success" : "!!!!!!! Failure !!!!!!!"));

                return finish;
            }
        }

        log("Patch result: !!!!!!! Couldn't locate method! !!!!!!!");

        return false;
    }

    public static MethodAction combine(NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNode(node, filter, action);
    }

    public static boolean applyOnNode(MethodNode method, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        Iterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes);

        boolean didAny = false;
        while (iterator.hasNext()) {
            AbstractInsnNode anode = iterator.next();
            if (filter.test(anode)) {
                didAny = true;
                if (action.test(method, anode))
                    break;
            }
        }

        return didAny;
    }

    public static MethodAction combineByLast(NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeByLast(node, filter, action);
    }

    public static boolean applyOnNodeByLast(MethodNode method, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes, method.instructions.size());

        boolean didAny = false;
        while (iterator.hasPrevious()) {
            AbstractInsnNode anode = iterator.previous();
            if (filter.test(anode)) {
                didAny = true;
                if (action.test(method, anode))
                    break;
            }
        }

        return didAny;
    }

    public static MethodAction combineFrontPivot(NodeFilter pivot, NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeFrontPivot(node, pivot, filter, action);
    }

    public static boolean applyOnNodeFrontPivot(MethodNode method, NodeFilter pivot, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes);

        int pos = 0;

        boolean didAny = false;
        while (iterator.hasNext()) {
            pos++;
            AbstractInsnNode pivotTest = iterator.next();
            if (pivot.test(pivotTest)) {
                ListIterator<AbstractInsnNode> internal = new InsnArrayIterator(nodes, pos);
                while (internal.hasPrevious()) {
                    AbstractInsnNode anode = internal.previous();
                    if (filter.test(anode)) {
                        didAny = true;
                        if (action.test(method, anode))
                            break;
                    }
                }
            }
        }

        return didAny;
    }

    public static MethodAction combineBackPivot(NodeFilter pivot, NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeBackPivot(node, pivot, filter, action);
    }

    public static boolean applyOnNodeBackPivot(MethodNode method, NodeFilter pivot, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes, method.instructions.size());

        int pos = method.instructions.size();

        boolean didAny = false;
        while (iterator.hasPrevious()) {
            pos--;
            AbstractInsnNode pivotTest = iterator.previous();
            if (pivot.test(pivotTest)) {
                ListIterator<AbstractInsnNode> internal = new InsnArrayIterator(nodes, pos);
                while (internal.hasNext()) {
                    AbstractInsnNode anode = internal.next();
                    if (filter.test(anode)) {
                        didAny = true;
                        if (action.test(method, anode))
                            break;
                    }
                }
            }
        }

        return didAny;
    }

    public static MethodAction combineFrontFocus(NodeFilter focus, NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeFrontFocus(node, focus, filter, action);
    }

    public static boolean applyOnNodeFrontFocus(MethodNode method, NodeFilter focus, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes);

        int pos = method.instructions.size();

        boolean didAny = false;
        while (iterator.hasNext()) {
            pos++;
            AbstractInsnNode focusTest = iterator.next();
            if (focus.test(focusTest)) {
                ListIterator<AbstractInsnNode> internal = new InsnArrayIterator(nodes, pos);
                while (internal.hasNext()) {
                    AbstractInsnNode anode = internal.next();
                    if (filter.test(anode)) {
                        didAny = true;
                        if (action.test(method, anode))
                            break;
                    }
                }
            }
        }

        return didAny;
    }

    public static MethodAction combineBackFocus(NodeFilter focus, NodeFilter filter, NodeAction action) {
        return (MethodNode node) -> applyOnNodeBackFocus(node, focus, filter, action);
    }

    public static boolean applyOnNodeBackFocus(MethodNode method, NodeFilter focus, NodeFilter filter, NodeAction action) {
        AbstractInsnNode[] nodes = method.instructions.toArray();
        ListIterator<AbstractInsnNode> iterator = new InsnArrayIterator(nodes, method.instructions.size());

        int pos = method.instructions.size();

        boolean didAny = false;
        while (iterator.hasPrevious()) {
            pos--;
            AbstractInsnNode focusTest = iterator.previous();
            if (focus.test(focusTest)) {
                ListIterator<AbstractInsnNode> internal = new InsnArrayIterator(nodes, pos);
                while (internal.hasPrevious()) {
                    AbstractInsnNode anode = internal.previous();
                    if (filter.test(anode)) {
                        didAny = true;
                        if (action.test(method, anode))
                            break;
                    }
                }
            }
        }

        return didAny;
    }

    public static void log(String str) {
        LogManager.getLogger("Thicc ASM").info(str);
    }

    public static void prettyPrint(MethodNode node) {
        Printer printer = new Textifier();

        TraceMethodVisitor visitor = new TraceMethodVisitor(printer);
        node.accept(visitor);

        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();

        log(sw.toString());
    }

    public static void prettyPrint(AbstractInsnNode node) {
        Printer printer = new Textifier();

        TraceMethodVisitor visitor = new TraceMethodVisitor(printer);
        node.accept(visitor);

        StringWriter sw = new StringWriter();
        printer.print(new PrintWriter(sw));
        printer.getText().clear();

        log(sw.toString());
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformers.containsKey(transformedName)) {
            String[] arr = transformedName.split("\\.");
            log("Transforming " + arr[arr.length - 1]);
            return transformers.get(transformedName).apply(basicClass);
        }

        return basicClass;
    }

    public interface Transformer extends Function<byte[], byte[]> {
        // NO-OP
    }

    public interface MethodAction extends Predicate<MethodNode> {
        // NO-OP
    }

    // Basic interface aliases to not have to clutter up the code with generics over and over again

    public interface NodeFilter extends Predicate<AbstractInsnNode> {
        // NO-OP
    }

    public interface NodeAction extends BiPredicate<MethodNode, AbstractInsnNode> {
        // NO-OP
    }

    private static class InsnArrayIterator implements ListIterator<AbstractInsnNode> {

        private final AbstractInsnNode[] array;
        private int index;

        public InsnArrayIterator(AbstractInsnNode[] array) {
            this(array, 0);
        }

        public InsnArrayIterator(AbstractInsnNode[] array, int index) {
            this.array = array;
            this.index = index;
        }

        @Override
        public boolean hasNext() {
            return array.length > index + 1 && index >= 0;
        }

        @Override
        public AbstractInsnNode next() {
            if (hasNext())
                return array[++index];
            return null;
        }

        @Override
        public boolean hasPrevious() {
            return index > 0 && index <= array.length;
        }

        @Override
        public AbstractInsnNode previous() {
            if (hasPrevious())
                return array[--index];
            return null;
        }

        @Override
        public int nextIndex() {
            return hasNext() ? index + 1 : array.length;
        }

        @Override
        public int previousIndex() {
            return hasPrevious() ? index - 1 : 0;
        }

        @Override
        public void remove() {
            throw new Error("Unimplemented");
        }

        @Override
        public void set(AbstractInsnNode e) {
            throw new Error("Unimplemented");
        }

        @Override
        public void add(AbstractInsnNode e) {
            throw new Error("Unimplemented");
        }
    }

    public static class MethodSignature {
        private final String funcName, srgName, funcDesc;

        public MethodSignature(String funcName, String srgName, String funcDesc) {
            this.funcName = funcName;
            this.srgName = srgName;
            this.funcDesc = funcDesc;
        }

        @Override
        public String toString() {
            return "Names [" + funcName + ", " + srgName + "] Descriptor " + funcDesc;
        }

        public boolean matches(String methodName, String methodDesc) {
            return (methodName.equals(funcName) || methodName.equals(srgName))
                    && (methodDesc.equals(funcDesc));
        }

        public boolean matches(MethodNode method) {
            return matches(method.name, method.desc);
        }

        public boolean matches(MethodInsnNode method) {
            return matches(method.name, method.desc);
        }
    }

    public static class FieldSignature {
        private final String fieldName, srgName, fieldDesc;

        public FieldSignature(String fieldName, String srgName, String fieldDesc) {
            this.fieldName = fieldName;
            this.srgName = srgName;
            this.fieldDesc = fieldDesc;
        }

        @Override
        public String toString() {
            return "Names [" + fieldName + ", " + srgName + "] Descriptor " + fieldDesc;
        }

        public boolean matches(String fieldName, String fieldDesc) {
            return (fieldName.equals(this.fieldName) || fieldName.equals(srgName))
                    && (fieldDesc.equals(this.fieldDesc));
        }

        public boolean matches(FieldInsnNode field) {
            return matches(field.name, field.desc);
        }

        public boolean matches(FieldNode field) {
            return matches(field.name, field.desc);
        }
    }

    /**
     * Safe class writer.
     * The way COMPUTE_FRAMES works may require loading additional classes. This can cause ClassCircularityErrors.
     * The override for getCommonSuperClass will ensure that COMPUTE_FRAMES works properly by using the right ClassLoader.
     *
     * Code from: https://github.com/JamiesWhiteShirt/clothesline/blob/master/src/core/java/com/jamieswhiteshirt/clothesline/core/SafeClassWriter.java
     */
    public static class SafeClassWriter extends ClassWriter {
        public SafeClassWriter(int flags) {
            super(flags);
        }

        public SafeClassWriter(ClassReader classReader, int flags) {
            super(classReader, flags);
        }

        @Override
        protected String getCommonSuperClass(String type1, String type2) {
            Class<?> c, d;
            ClassLoader classLoader = Launch.classLoader;
            try {
                c = Class.forName(type1.replace('/', '.'), false, classLoader);
                d = Class.forName(type2.replace('/', '.'), false, classLoader);
            } catch (Exception e) {
                throw new RuntimeException(e.toString());
            }
            if (c.isAssignableFrom(d)) {
                return type1;
            }
            if (d.isAssignableFrom(c)) {
                return type2;
            }
            if (c.isInterface() || d.isInterface()) {
                return "java/lang/Object";
            } else {
                do {
                    c = c.getSuperclass();
                } while (!c.isAssignableFrom(d));
                return c.getName().replace('.', '/');
            }
        }
    }
}
