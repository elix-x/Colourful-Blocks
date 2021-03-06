package code.elix_x.coremods.colourfulblocks.core;

import java.lang.reflect.Modifier;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import net.minecraft.launchwrapper.IClassTransformer;

public class ColourfulBlocksTransformer implements IClassTransformer {

	public static final Logger logger = LogManager.getLogger("CoBl Core");

	@Override
	public byte[] transform(String name, String transformedName, byte[] bytes){
		if(name.equals("net.minecraft.client.renderer.RenderBlocks")){
			logger.debug("##################################################");
			logger.debug("Patching RenderBlocks");
			byte[] b = patchRenderBlocks(name, bytes);
			logger.debug("Patching RenderBlocks Completed");
			logger.debug("##################################################");
			return b;
		}
		if(name.equals("net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher")){
			logger.debug("##################################################");
			logger.debug("Patching TileEntityRendererDispatcher");
			byte[] b = patchTileEntityRendererDispatcher(name, bytes);
			logger.debug("Patching TileEntityRendererDispatcher Completed");
			logger.debug("##################################################");
			return b;
		}
		return dynamicPatchBlock(name, transformedName, bytes);
	}

	private byte[] patchRenderBlocks(String name, byte[] bytes){		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for(MethodNode method : classNode.methods){
			if((method.name.equals("renderBlockByRenderType") || method.name.equals("func_147805_b")) && method.desc.equals("(Lnet/minecraft/block/Block;III)Z")){
				try {
					logger.debug("**************************************************");
					logger.debug("Patching renderBlockByRenderType");


					AbstractInsnNode targetNode = null;

					for(AbstractInsnNode node : method.instructions.toArray()){
						if(node instanceof MethodInsnNode){
							MethodInsnNode mnode = (MethodInsnNode) node;
							if(mnode.owner.equals("net.minecraft.src.FMLRenderAccessLibrary".replace(".", "/"))){
								targetNode = node.getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious().getPrevious();
								break;
							}
						}
					}

					InsnList list = new InsnList();
					list.add(new LabelNode());
					list.add(new VarInsnNode(Opcodes.ALOAD, 0));
					list.add(new VarInsnNode(Opcodes.ILOAD, 2));
					list.add(new VarInsnNode(Opcodes.ILOAD, 3));
					list.add(new VarInsnNode(Opcodes.ILOAD, 4));
					list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "code.elix_x.coremods.colourfulblocks.core.ColourfulBlocksHooks".replace(".", "/"), "recolorBlock", "(L" + "net.minecraft.renderer.RenderBlock".replace(".", "/") + ";III)V", false));
					list.add(new LabelNode());

					method.instructions.insert(targetNode.getPrevious().getPrevious().getPrevious(), list);


					logger.debug("Patching renderBlockByRenderType Completed");
					logger.debug("**************************************************");
				} catch(Exception e){
					logger.error("Patching renderBlockByRenderType Failed With Exception:", e);
					logger.debug("**************************************************");
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private byte[] patchTileEntityRendererDispatcher(String name, byte[] bytes){		
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		for(MethodNode method : classNode.methods){
			if((method.name.equals("renderTileEntity") || method.name.equals("func_147544_a")) && method.desc.equals("(Lnet/minecraft/tileentity/TileEntity;F)V")){
				try {
					logger.debug("**************************************************");
					logger.debug("Patching renderTileEntity");


					AbstractInsnNode targetNode = null;

					for(AbstractInsnNode node : method.instructions.toArray()){
						if(node instanceof MethodInsnNode){
							MethodInsnNode mnode = (MethodInsnNode) node;
							if(mnode.owner.equals(GL11.class.getName().replace(".", "/"))){
								targetNode = node;
								break;
							}
						}
					}

					InsnList list = new InsnList();
					list.add(new LabelNode());
					list.add(new VarInsnNode(Opcodes.ALOAD, 1));
					list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "code.elix_x.coremods.colourfulblocks.core.ColourfulBlocksHooks".replace(".", "/"), "recolorTileEntity", "(L" + "net.minecraft.tileentity.TileEntity".replace(".", "/") + ";)V", false));
					list.add(new LabelNode());

					method.instructions.insert(targetNode, list);


					logger.debug("Patching renderTileEntity Completed");
					logger.debug("**************************************************");
				} catch(Exception e){
					logger.error("Patching renderTileEntity Failed With Exception:", e);
					logger.debug("**************************************************");
				}
			}
		}

		ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		classNode.accept(writer);
		return writer.toByteArray();
	}

	private byte[] dynamicPatchBlock(String name, String transformedName, byte[] bytes){
		ClassNode classNode = new ClassNode();
		ClassReader classReader = new ClassReader(bytes);
		classReader.accept(classNode, 0);

		boolean patched = false;
		Exception error = null;

		for(MethodNode method : classNode.methods){
			if((method.name.equals("colorMultiplier") || method.name.equals("func_149720_d")) && method.desc.equals("(Lnet/minecraft/world/IBlockAccess;III)I")){
				try {
					for(AbstractInsnNode node : method.instructions.toArray()){
						if(node.getOpcode() == Opcodes.IRETURN){
							InsnList list = new InsnList();
							if(Modifier.isStatic(method.access)){
								list.add(new VarInsnNode(Opcodes.ALOAD, 0));
								list.add(new InsnNode(Opcodes.ACONST_NULL));
								list.add(new VarInsnNode(Opcodes.ILOAD, 1));
								list.add(new VarInsnNode(Opcodes.ILOAD, 2));
								list.add(new VarInsnNode(Opcodes.ILOAD, 3));
							} else {
								list.add(new VarInsnNode(Opcodes.ALOAD, 1));
								list.add(new VarInsnNode(Opcodes.ALOAD, 0));
								list.add(new VarInsnNode(Opcodes.ILOAD, 2));
								list.add(new VarInsnNode(Opcodes.ILOAD, 3));
								list.add(new VarInsnNode(Opcodes.ILOAD, 4));
							}
							list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "code.elix_x.coremods.colourfulblocks.core.ColourfulBlocksHooks".replace(".", "/"), "colorMultiplier", "(ILnet/minecraft/world/IBlockAccess;Lnet/minecraft/block/Block;III)I", false));
							method.instructions.insertBefore(node, list);
						}
					}
				} catch(Exception e){
					error = e;
				}
				patched = true;
			}
		}

		if(patched){
			logger.debug("##################################################");
			logger.debug(String.format("Dynamically Patching %s(%s)", name, transformedName));

			logger.debug("**************************************************");
			logger.debug("Patching colorMultiplier");
			if(error != null){
				logger.error("Patching colorMultiplier Failed With Exception:", error);
				logger.debug("**************************************************");
			} else {
				logger.debug("Patching colorMultiplier Completed");
				logger.debug("**************************************************");
			}

			logger.debug(String.format("Dynamically Patching %s(%s) Completed", name, transformedName));
			logger.debug("##################################################");
		}

		if(patched){
			ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
			classNode.accept(writer);
			return writer.toByteArray();
		} else {
			return bytes;
		}
	}

}
