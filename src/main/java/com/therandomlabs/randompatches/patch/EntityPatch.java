package com.therandomlabs.randompatches.patch;

import com.therandomlabs.randompatches.core.Patch;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class EntityPatch extends Patch {
	public static final String ENTITYPATCH = getName(EntityPatch.class);
	public static final String SET_POSITION = getName("setPosition", "func_70107_b");

	@Override
	public void apply(ClassNode node) {
		patchWriteToNBT(findMethod(node, "writeToNBT", "func_189511_e"));
		patchReadFromNBT(findMethod(node, "readFromNBT", "func_70020_e"));
	}

	public static void writeAABBTag(Entity entity, NBTTagCompound compound) {
		final AxisAlignedBB aabb = entity.getEntityBoundingBox();
		final NBTTagList list = new NBTTagList();

		list.appendTag(new NBTTagDouble(aabb.minX));
		list.appendTag(new NBTTagDouble(aabb.minY));
		list.appendTag(new NBTTagDouble(aabb.minZ));
		list.appendTag(new NBTTagDouble(aabb.maxX));
		list.appendTag(new NBTTagDouble(aabb.maxY));
		list.appendTag(new NBTTagDouble(aabb.maxZ));

		compound.setTag("AABB", list);
	}

	public static void readAABBTag(Entity entity, NBTTagCompound compound) {
		if(!compound.hasKey("AABB")) {
			return;
		}

		final NBTTagList aabb = compound.getTagList("AABB", Constants.NBT.TAG_DOUBLE);

		entity.setEntityBoundingBox(new AxisAlignedBB(
				aabb.getDoubleAt(0),
				aabb.getDoubleAt(1),
				aabb.getDoubleAt(2),
				aabb.getDoubleAt(3),
				aabb.getDoubleAt(4),
				aabb.getDoubleAt(5)
		));
	}

	private static void patchWriteToNBT(MethodNode method) {
		MethodInsnNode setTag = null;

		for(int i = 0; i < method.instructions.size(); i++) {
			final AbstractInsnNode instruction = method.instructions.get(i);

			if(instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
				setTag = (MethodInsnNode) instruction;

				if("net/minecraft/nbt/NBTTagCompound".equals(setTag.owner)) {
					break;
				}

				setTag = null;
			}
		}

		final VarInsnNode loadThis = new VarInsnNode(
				Opcodes.ALOAD,
				0
		);

		final VarInsnNode loadCompound = new VarInsnNode(
				Opcodes.ALOAD,
				1
		);

		final MethodInsnNode writeAABBTag = new MethodInsnNode(
				Opcodes.INVOKESTATIC,
				ENTITYPATCH,
				"writeAABBTag",
				"(Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/NBTTagCompound;)V",
				false
		);

		method.instructions.insert(setTag, loadThis);
		method.instructions.insert(loadThis, loadCompound);
		method.instructions.insert(loadCompound, writeAABBTag);
	}

	@SuppressWarnings("Duplicates")
	private static void patchReadFromNBT(MethodNode method) {
		JumpInsnNode jumpIfShouldNotSetPosition = null;
		MethodInsnNode setPosition = null;

		for(int i = method.instructions.size() - 1; i >= 0; i--) {
			final AbstractInsnNode instruction = method.instructions.get(i);

			if(setPosition == null) {
				if(instruction.getOpcode() == Opcodes.INVOKEVIRTUAL) {
					setPosition = (MethodInsnNode) instruction;

					if(!SET_POSITION.equals(setPosition.name)) {
						setPosition = null;
					}
				}

				continue;
			}

			if(instruction.getOpcode() == Opcodes.IFEQ) {
				jumpIfShouldNotSetPosition = (JumpInsnNode) instruction;
				break;
			}
		}

		final LabelNode jumpTo = new LabelNode();

		jumpIfShouldNotSetPosition.label = jumpTo;

		final VarInsnNode loadThis = new VarInsnNode(
				Opcodes.ALOAD,
				0
		);

		final VarInsnNode loadCompound = new VarInsnNode(
				Opcodes.ALOAD,
				1
		);

		final MethodInsnNode readAABBTag = new MethodInsnNode(
				Opcodes.INVOKESTATIC,
				ENTITYPATCH,
				"readAABBTag",
				"(Lnet/minecraft/entity/Entity;Lnet/minecraft/nbt/NBTTagCompound;)V",
				false
		);

		method.instructions.insert(setPosition, jumpTo);
		method.instructions.insert(jumpTo, loadThis);
		method.instructions.insert(loadThis, loadCompound);
		method.instructions.insert(loadCompound, readAABBTag);
	}
}