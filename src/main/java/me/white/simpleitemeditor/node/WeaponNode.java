package me.white.simpleitemeditor.node;

//? if >=1.21.5 {
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.DurationArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WeaponComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class WeaponNode implements Node {
    private static final CommandSyntaxException NO_WEAPON_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.weapon.error.noweapon")).create();
    private static final CommandSyntaxException DAMAGE_ALREDY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.weapon.error.damagealreadyis")).create();
    private static final CommandSyntaxException DISABLING_ALREDY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.weapon.error.disablingalreadyis")).create();
    private static final String OUTPUT_DAMAGE_GET = "commands.edit.weapon.damageget";
    private static final String OUTPUT_DAMAGE_SET = "commands.edit.weapon.damageset";
    private static final String OUTPUT_DISABLING_GET = "commands.edit.weapon.disablingget";
    private static final String OUTPUT_DISABLING_SET = "commands.edit.weapon.disablingset";
    private static final String OUTPUT_REMOVE = "commands.edit.weapon.remove";

    private static boolean hasWeapon(ItemStack stack) {
        return stack.contains(DataComponentTypes.WEAPON);
    }

    private static int getDamage(ItemStack stack) {
        WeaponComponent component = stack.get(DataComponentTypes.WEAPON);
        return component.itemDamagePerAttack();
    }

    private static int getDisabling(ItemStack stack) {
        WeaponComponent component = stack.get(DataComponentTypes.WEAPON);
        return (int)(component.disableBlockingForSeconds() * 20);
    }

    private static void setDamage(ItemStack stack, int damage) {
        float disabling = 0.0f;
        if (stack.contains(DataComponentTypes.WEAPON)) {
            WeaponComponent component = stack.get(DataComponentTypes.WEAPON);
            disabling = component.disableBlockingForSeconds();
        }
        stack.set(DataComponentTypes.WEAPON, new WeaponComponent(damage, disabling));
    }

    private static void setDisabling(ItemStack stack, int disabling) {
        int damage = 0;
        if (stack.contains(DataComponentTypes.WEAPON)) {
            WeaponComponent component = stack.get(DataComponentTypes.WEAPON);
            damage = component.itemDamagePerAttack();
        }
        stack.set(DataComponentTypes.WEAPON, new WeaponComponent(damage, disabling / 20.0f));
    }

    private static void removeWeapon(ItemStack stack) {
        stack.remove(DataComponentTypes.WEAPON);
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("weapon").build();

        CommandNode<S> damageNode = commandManager.literal("damage").build();

        CommandNode<S> damageGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasWeapon(stack)) {
                throw NO_WEAPON_EXCEPTION;
            }
            int damage = getDamage(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_DAMAGE_GET, damage));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> damageSetNode = commandManager.literal("set").build();

        CommandNode<S> damageSetDamageNode = commandManager.argument("damage", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int damage = IntegerArgumentType.getInteger(context, "damage");
            if (hasWeapon(stack)) {
                int oldDamage = getDamage(stack);
                if (damage == oldDamage) {
                    throw DAMAGE_ALREDY_IS_EXCEPTION;
                }
            }
            setDamage(stack, damage);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_DAMAGE_SET, damage));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> disablingNode = commandManager.literal("disabling").build();

        CommandNode<S> disablingGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasWeapon(stack)) {
                throw NO_WEAPON_EXCEPTION;
            }
            int disabling = getDisabling(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_DISABLING_GET, disabling));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> disablingSetNode = commandManager.literal("set").build();

        CommandNode<S> disablingSetDisablingNode = commandManager.argument("disabling", DurationArgumentType.duration(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            int disabling = DurationArgumentType.getDuration(context, "disabling");
            if (hasWeapon(stack)) {
                int oldDisabling = getDisabling(stack);
                if (disabling == oldDisabling) {
                    throw DISABLING_ALREDY_IS_EXCEPTION;
                }
            }
            setDisabling(stack, disabling);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_DISABLING_SET, disabling));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> removeNode = commandManager.literal("remove").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!hasWeapon(stack)) {
                throw NO_WEAPON_EXCEPTION;
            }
            removeWeapon(stack);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... damage
        node.addChild(damageNode);
        // ... get
        damageNode.addChild(damageGetNode);
        // ... set <damage>
        damageNode.addChild(damageSetNode);
        damageSetNode.addChild(damageSetDamageNode);

        // ... disabling
        node.addChild(disablingNode);
        // ... get
        disablingNode.addChild(disablingGetNode);
        // ... set <disabling>
        disablingNode.addChild(disablingSetNode);
        disablingSetNode.addChild(disablingSetDisablingNode);

        // ... remove
        node.addChild(removeNode);

        return node;
    }
}
//?}
