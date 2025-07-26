package me.white.simpleitemeditor.node.mob;

//? if >=1.21.2 {
import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.EnumArgumentType;
//? if <1.21.5 {
/*import me.white.simpleitemeditor.node.DataNode;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
*///?}
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class SalmonNode implements Node {
    private static final CommandSyntaxException ISNT_SALMON_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.salmon.error.isntsalmon")).create();
    private static final CommandSyntaxException SIZE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.salmon.error.sizealreadyis")).create();
    private static final CommandSyntaxException NO_SIZE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.mob.salmon.error.nosize")).create();
    private static final String OUTPUT_GET_SIZE = "commands.edit.mob.salmon.sizeget";
    private static final String OUTPUT_SET_SIZE = "commands.edit.mob.salmon.sizeset";
    private static final String OUTPUT_REMOVE_SIZE = "commands.edit.mob.salmon.sizeremove";
    private static final String SIZE_SMALL = "variant.minecraft.salmon.small";
    private static final String SIZE_MEDIUM = "variant.minecraft.salmon.medium";
    private static final String SIZE_LARGE = "variant.minecraft.salmon.large";
    //? if <1.21.5 {
    /*private static final String VARIANT_KEY = "type";
    *///?}

    private static Text translation(SalmonEntity.Variant variant) {
        return switch (variant) {
            case SMALL -> Text.translatable(SIZE_SMALL);
            case MEDIUM -> Text.translatable(SIZE_MEDIUM);
            case LARGE -> Text.translatable(SIZE_LARGE);
        };
    }

    private static boolean isSalmon(ItemStack stack) {
        return EditorUtil.getEntityType(stack) == EntityType.SALMON;
    }

    private static boolean hasSize(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.contains(DataComponentTypes.SALMON_SIZE);
        //?} else {
        /*if (!stack.contains(DataComponentTypes.ENTITY_DATA)) {
            return false;
        }
        NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        if (!nbt.contains(VARIANT_KEY, NbtElement.STRING_TYPE)) {
            return false;
        }
        String id = nbt.getString(VARIANT_KEY);
        for (SalmonEntity.Variant variant : SalmonEntity.Variant.values()) {
            if (variant.asString().equals(id)) {
                return true;
            }
        }
        return false;
        *///?}
    }

    private static SalmonEntity.Variant getSize(ItemStack stack) {
        //? if >=1.21.5 {
        return stack.get(DataComponentTypes.SALMON_SIZE);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        String id = nbt.getString(VARIANT_KEY);
        for (SalmonEntity.Variant variant : SalmonEntity.Variant.values()) {
            if (variant.asString().equals(id)) {
                return variant;
            }
        }
        return null;
        *///?}
    }

    private static void setSize(ItemStack stack, SalmonEntity.Variant size) {
        //? if >=1.21.5 {
        stack.set(DataComponentTypes.SALMON_SIZE, size);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.putString(VARIANT_KEY, size.asString());
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    private static void removeSize(ItemStack stack) {
        //? if >=1.21.5 {
        stack.remove(DataComponentTypes.SALMON_SIZE);
        //?} else {
        /*NbtCompound nbt = DataNode.DataSource.ENTITY.get(stack);
        nbt.remove(VARIANT_KEY);
        DataNode.DataSource.ENTITY.set(stack, nbt);
        *///?}
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("salmon").build();

        CommandNode<S> sizeNode = commandManager.literal("size").build();

        CommandNode<S> sizeGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isSalmon(stack)) {
                throw ISNT_SALMON_EXCEPTION;
            }
            if (!hasSize(stack)) {
                throw NO_SIZE_EXCEPTION;
            }
            SalmonEntity.Variant variant = getSize(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_SIZE, translation(variant)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sizeSetNode = commandManager.literal("set").build();

        CommandNode<S> sizeSetSizeNode = commandManager.argument("size", EnumArgumentType.enums(SalmonEntity.Variant.class)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isSalmon(stack)) {
                throw ISNT_SALMON_EXCEPTION;
            }
            SalmonEntity.Variant size = context.getArgument("size", SalmonEntity.Variant.class);
            if (hasSize(stack)) {
                SalmonEntity.Variant oldSize = getSize(stack);
                if (size == oldSize) {
                    throw SIZE_ALREADY_IS_EXCEPTION;
                }
            }
            setSize(stack, size);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_SIZE, translation(size)));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> sizeRemoveNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isSalmon(stack)) {
                throw ISNT_SALMON_EXCEPTION;
            }
            if (!hasSize(stack)) {
                throw NO_SIZE_EXCEPTION;
            }
            removeSize(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE_SIZE));
            EditorUtil.setStack(context.getSource(), stack);
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... size
        node.addChild(sizeNode);
        // ... get
        sizeNode.addChild(sizeGetNode);
        // ... set <size>
        sizeNode.addChild(sizeSetNode);
        sizeSetNode.addChild(sizeSetSizeNode);
        // ... remove
        sizeNode.addChild(sizeRemoveNode);

        return node;
    }
}
//?}
