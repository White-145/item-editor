package me.white.simpleitemeditor;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.white.simpleitemeditor.argument.*;
import me.white.simpleitemeditor.argument.enums.*;
import me.white.simpleitemeditor.command.EditCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleItemEditor implements ClientModInitializer, DedicatedServerModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("item-editor");
    public static CommandDispatcher<FabricClientCommandSource> clientCommandDispatcher = new CommandDispatcher<>();

    public static boolean executeCommand(String command) {
        MinecraftClient client = MinecraftClient.getInstance();
        FabricClientCommandSource commandSource = (FabricClientCommandSource)client.getNetworkHandler().getCommandSource();
        client.getProfiler().push(command);

        try {
            clientCommandDispatcher.execute(command, commandSource);
        } catch (CommandSyntaxException e) {
            CommandExceptionType type = e.getType();
            BuiltInExceptionProvider builtins = CommandSyntaxException.BUILT_IN_EXCEPTIONS;
            if (type == builtins.dispatcherUnknownCommand() || type == builtins.dispatcherParseException()) {
                LOGGER.debug("Syntax exception for client-sided command '{}'", command, e);
                return false;
            }
            LOGGER.warn("Syntax exception for client-sided command '{}'", command, e);
            Text message = Texts.toText(e.getRawMessage());
            String context = e.getContext();
            Text errorMessage = context != null ? Text.translatable("command.context.parse_error", message, e.getCursor(), context) : message;
            commandSource.sendError(errorMessage);
        } catch (Exception e) {
            LOGGER.warn("Error while executing client-sided command '{}'", command, e);
            commandSource.sendError(Text.of(e.getMessage()));
        } finally {
            client.getProfiler().pop();
        }
        return true;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerArgumentTypes() {
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "color"), ColorArgumentType.class, ConstantArgumentSerializer.of(ColorArgumentType::color));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "duration"), DurationArgumentType.class, new DurationArgumentType.Serializer());
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "identifier"), IdentifierArgumentType.class, ConstantArgumentSerializer.of(IdentifierArgumentType::identifier));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "infinitedouble"), InfiniteDoubleArgumentType.class, new InfiniteDoubleArgumentType.Serializer());
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "legacytext"), LegacyTextArgumentType.class, ConstantArgumentSerializer.of(LegacyTextArgumentType::text));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "registry"), (Class<? extends RegistryArgumentType<?>>)(Class<? extends RegistryArgumentType>)RegistryArgumentType.class, new RegistryArgumentType.Serializer());

        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "attributeoperation"), AttributeOperationArgumentType.class, ConstantArgumentSerializer.of(AttributeOperationArgumentType::attributeOperation));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "attributeslot"), AttributeSlotArgumentType.class, ConstantArgumentSerializer.of(AttributeSlotArgumentType::attributeSlot));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "dyecolor"), DyeColorArgumentType.class, ConstantArgumentSerializer.of(DyeColorArgumentType::dyeColor));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "datasource"), DataSourceArgumentType.class, ConstantArgumentSerializer.of(DataSourceArgumentType::dataSource));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "exclusiveslot"), ExclusiveSlotArgumentType.class, ConstantArgumentSerializer.of(ExclusiveSlotArgumentType::exclusiveSlot));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "rarity"), RarityArgumentType.class, ConstantArgumentSerializer.of(RarityArgumentType::rarity));
        ArgumentTypeRegistry.registerArgumentType(Identifier.of("sie", "tooltippart"), TooltipPartArgumentType.class, ConstantArgumentSerializer.of(TooltipPartArgumentType::tooltipPart));
    }

    @Override
    public void onInitializeClient() {
        registerArgumentTypes();
    }

    @Override
    public void onInitializeServer() {
        registerArgumentTypes();
        CommandRegistrationCallback.EVENT.register(EditCommand::registerServer);
    }
}
