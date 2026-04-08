package me.white.sie.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.sie.Node;
import me.white.sie.argument.DurationArgumentType;
import me.white.sie.util.CommonCommandManager;
import me.white.sie.util.EditorUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class WeaponNode implements Node {
    private static final CommandSyntaxException NO_WEAPON_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.weapon.error.noweapon")).create();
    private static final CommandSyntaxException DAMAGE_ALREDY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.weapon.error.damagealreadyis")).create();
    private static final CommandSyntaxException DISABLING_ALREDY_IS_EXCEPTION = new SimpleCommandExceptionType(Component.translatable("commands.edit.weapon.error.disablingalreadyis")).create();
    private static final String OUTPUT_DAMAGE_GET = "commands.edit.weapon.damageget";
    private static final String OUTPUT_DAMAGE_SET = "commands.edit.weapon.damageset";
    private static final String OUTPUT_DISABLING_GET = "commands.edit.weapon.disablingget";
    private static final String OUTPUT_DISABLING_SET = "commands.edit.weapon.disablingset";
    private static final String OUTPUT_REMOVE = "commands.edit.weapon.remove";

    private static boolean hasWeapon(ItemStack stack) {
        return stack.has(DataComponents.WEAPON);
    }

    private static int getDamage(ItemStack stack) {
        Weapon component = stack.get(DataComponents.WEAPON);
        return component.itemDamagePerAttack();
    }

    private static int getDisabling(ItemStack stack) {
        Weapon component = stack.get(DataComponents.WEAPON);
        return (int)(component.disableBlockingForSeconds() * 20);
    }

    private static void setDamage(ItemStack stack, int damage) {
        float disabling = 0.0f;
        if (stack.has(DataComponents.WEAPON)) {
            Weapon component = stack.get(DataComponents.WEAPON);
            disabling = component.disableBlockingForSeconds();
        }
        stack.set(DataComponents.WEAPON, new Weapon(damage, disabling));
    }

    private static void setDisabling(ItemStack stack, int disabling) {
        int damage = 0;
        if (stack.has(DataComponents.WEAPON)) {
            Weapon component = stack.get(DataComponents.WEAPON);
            damage = component.itemDamagePerAttack();
        }
        stack.set(DataComponents.WEAPON, new Weapon(damage, disabling / 20.0f));
    }

    private static void removeWeapon(ItemStack stack) {
        stack.remove(DataComponents.WEAPON);
    }

    @Override
    public <S extends SharedSuggestionProvider> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandBuildContext registryAccess) {
        CommandNode<S> node = commandManager.literal("weapon").build();

        CommandNode<S> damageNode = commandManager.literal("damage").build();

        CommandNode<S> damageGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasWeapon(stack)) {
                throw NO_WEAPON_EXCEPTION;
            }
            int damage = getDamage(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_DAMAGE_GET, damage));
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
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_DAMAGE_SET, damage));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> disablingNode = commandManager.literal("disabling").build();

        CommandNode<S> disablingGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!hasWeapon(stack)) {
                throw NO_WEAPON_EXCEPTION;
            }
            int disabling = getDisabling(stack);

            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_DISABLING_GET, disabling));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> disablingSetNode = commandManager.literal("set").build();

        CommandNode<S> disablingSetDisablingNode = commandManager.argument("disabling", DurationArgumentType.duration(0, false)).executes(context -> {
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
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_DISABLING_SET, disabling));
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
            EditorUtil.sendFeedback(context.getSource(), Component.translatable(OUTPUT_REMOVE));
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
