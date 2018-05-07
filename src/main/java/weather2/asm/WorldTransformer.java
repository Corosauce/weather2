package weather2.asm;

import java.util.List;

import CoroUtil.forge.CULog;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import com.google.common.collect.Lists;

import net.minecraft.launchwrapper.IClassTransformer;

public class WorldTransformer implements IClassTransformer
{
    private static final String[] IS_RAINING_AT_NAMES = new String[] { "isRainingAt", "func_175727_C", "B" };

    public static boolean debug = true;
    
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass)
    {
        if (transformedName.equals("net.minecraft.world.World"))
        {
            return transformWorld(basicClass, !transformedName.equals(name));
        }
        
        return basicClass;
    }
    
    private byte[] transformWorld(byte[] bytes, boolean obfuscatedClass)
    {

        //to fix it trying to use srg names when its remapped names, caused by point transformer is run at since using @SortingIndex
        obfuscatedClass = false;

        //Decode the class from bytes
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(bytes);
        classReader.accept(classNode, 0);

        List<String> successfulTransformations = Lists.newArrayList();

        dbg("transforming world, obfuscatedClass = " + obfuscatedClass);
        
        //Iterate over the methods in the class
        for (MethodNode methodNode : classNode.methods)
        {

            dbg("considering " + methodNode.name);

            if (ASMHelper.methodEquals(methodNode, IS_RAINING_AT_NAMES,
                    ObfHelper.createMethodDescriptor(obfuscatedClass, "Z", "net/minecraft/util/math/BlockPos")))
            {

                dbg("found isRaining method");

                InsnList insnList = new InsnList();

                //Invoke our replacement method
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 1));
                insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "weather2/asm/WeatherASMHelper", "isRainingAt",
                        ObfHelper.createMethodDescriptor(obfuscatedClass, "Z", "net/minecraft/world/World", "net/minecraft/util/math/BlockPos"), false));
                insnList.add(new InsnNode(Opcodes.IRETURN));

                methodNode.instructions.clear();
                methodNode.instructions.insert(insnList);
                
                successfulTransformations.add(methodNode.name + " " + methodNode.desc);
            }
        }
        
        if (successfulTransformations.size() != 1) throw new RuntimeException("An error occurred transforming World. Applied transformations: " + successfulTransformations.toString());
        
        //Encode the altered class back into bytes
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        bytes = writer.toByteArray();
        
        return bytes;
    }

    public static void dbg(String str) {
        if (debug) {
            System.out.println(str);
        }
    }
}
