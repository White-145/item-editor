package me.white.itemeditor.node;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.util.Colored;
import me.white.itemeditor.util.ItemUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BookNode {
	public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.cannotedit")).create();
	public static final CommandSyntaxException NO_AUTHOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.noauthor")).create();
	public static final CommandSyntaxException NO_TITLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.notitle")).create();
	public static final CommandSyntaxException NO_PAGES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.nopages")).create();
	public static final Dynamic2CommandExceptionType OUT_OF_BOUNDS_EXCEPTION = new Dynamic2CommandExceptionType((index, size) -> Text.translatable("commands.edit.book.error.pageoutofbounds", index, size));
    private static final String OUTPUT_AUTHOR_GET = "commands.edit.book.authorget";
    private static final String OUTPUT_AUTHOR_SET = "commands.edit.book.authorset";
    private static final String OUTPUT_TITLE_GET = "commands.edit.book.titleget";
    private static final String OUTPUT_TITLE_SET = "commands.edit.book.titleset";
    private static final String OUTPUT_GENERATION_GET = "commands.edit.book.generationget";
    private static final String OUTPUT_GENERATION_SET = "commands.edit.book.generationset";
    private static final String OUTPUT_PAGES_GET = "commands.edit.book.pagesget";
    private static final String OUTPUT_PAGES_GET_PAGE = "";
    private static final String OUTPUT_PAGES_ADD = "";
    private static final String OUTPUT_PAGES_SET = "";
    private static final String OUTPUT_PAGES_INSERT = "";
    private static final String OUTPUT_PAGES_REMOVE = "";
    private static final String OUTPUT_PAGES_CLEAR = "";
    private static final String OUTPUT_PAGES_CLEAR_BEFORE = "";
    private static final String OUTPUT_PAGES_CLEAR_AFTER = "";
    private static final String AUTHOR_KEY = "author";
    private static final String TITLE_KEY = "title";
    private static final String GENERATION_KEY = "generation";
    private static final String PAGES_KEY = "pages";

	private static enum Generation {
		ORIGINAL(0, "commands.edit.book.generationoriginal"),
		COPY(1, "commands.edit.book.generationcopy"),
		COPY_OF_COPY(2, "commands.edit.book.generationcopyofcopy"),
		TATTERED(3, "commands.edit.book.generationtattered");

		int id;
		String translationKey;

		private Generation(int id, String translationKey) {
			this.id = id;
			this.translationKey = translationKey;
		}

		public static Generation byId(int id) {
			switch (id) {
				case 0: return ORIGINAL;
				case 1: return COPY;
				case 2: return COPY_OF_COPY;
				case 3: return TATTERED;
			}
			return null;
		}
	}

    private static void checkCanEdit(FabricClientCommandSource source) throws CommandSyntaxException {
		Item item = ItemUtil.getItemStack(source).getItem();
        if (item != Items.WRITTEN_BOOK) throw CANNOT_EDIT_EXCEPTION;
    }

    private static void checkHasPages(FabricClientCommandSource source) throws CommandSyntaxException {
		checkCanEdit(source);
		ItemStack item = ItemUtil.getItemStack(source);
        if (!item.hasNbt()) throw NO_PAGES_EXCEPTION;
        if (!item.getNbt().contains(PAGES_KEY, NbtElement.LIST_TYPE));
    }

	public static void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
			.literal("book")
			.build();
        
        LiteralCommandNode<FabricClientCommandSource> authorNode = ClientCommandManager
            .literal("author")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> authorGetNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                if (!item.hasNbt()) throw NO_AUTHOR_EXCEPTION;
                NbtCompound nbt = item.getNbt();
                if (!nbt.contains(AUTHOR_KEY, NbtElement.STRING_TYPE)) throw NO_AUTHOR_EXCEPTION;
                String author = nbt.getString(AUTHOR_KEY);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_AUTHOR_GET, author));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> authorSetNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                NbtCompound nbt = item.getOrCreateNbt();
                nbt.putString(AUTHOR_KEY, "");
                item.setNbt(nbt);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_AUTHOR_SET, ""));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, String> authorSetAuthorNode = ClientCommandManager
            .argument("author", StringArgumentType.greedyString())
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                String author = StringArgumentType.getString(context, "author");
                NbtCompound nbt = item.getOrCreateNbt();
                nbt.putString(AUTHOR_KEY, author);
                item.setNbt(nbt);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_AUTHOR_SET, author));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> titleNode = ClientCommandManager
            .literal("title")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> titleGetNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                if (!item.hasNbt()) throw NO_TITLE_EXCEPTION;
                NbtCompound nbt = item.getNbt();
                if (!nbt.contains(TITLE_KEY, NbtElement.STRING_TYPE)) throw NO_TITLE_EXCEPTION;
                String title = nbt.getString(TITLE_KEY);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_TITLE_GET, title));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> titleSetNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                NbtCompound nbt = item.getOrCreateNbt();
                nbt.putString(TITLE_KEY, "");
                item.setNbt(nbt);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_TITLE_SET, ""));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, String> titleSetTitleNode = ClientCommandManager
            .argument("title", StringArgumentType.greedyString())
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
                String title = StringArgumentType.getString(context, "title");
                NbtCompound nbt = item.getOrCreateNbt();
                nbt.putString(TITLE_KEY, title);
                item.setNbt(nbt);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_TITLE_SET, title));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> generationNode = ClientCommandManager
            .literal("generation")
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> generationGetNode = ClientCommandManager
            .literal("get")
            .executes(context -> {
                ItemUtil.checkHasItem(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource());
                int generation = 0;
                if (item.hasNbt()) generation = item.getNbt().getInt(GENERATION_KEY);
                String generationStr = Generation.byId(generation).translationKey;

                context.getSource().sendFeedback(Text.translatable(OUTPUT_GENERATION_GET, Text.translatable(generationStr)));
                return generation;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> generationSetNode = ClientCommandManager
            .literal("set")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Generation> generationSetGenerationNode = ClientCommandManager
			.argument("generation", EnumArgumentType.enumArgument(Generation.class))
            .executes(context -> {
                ItemUtil.checkCanEdit(context.getSource());
                checkCanEdit(context.getSource());

                ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				Generation generation = EnumArgumentType.getEnum(context, "generation", Generation.class);

                NbtCompound nbt = item.getOrCreateNbt();
                int old = nbt.getInt(GENERATION_KEY);
                nbt.putInt(GENERATION_KEY, generation.id);
                item.setNbt(nbt);

                ItemUtil.setItemStack(context.getSource(), item);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_GENERATION_SET, Text.translatable(generation.translationKey)));
                return old;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> pageNode = ClientCommandManager
            .literal("page")
            .build();
        
		LiteralCommandNode<FabricClientCommandSource> pageGetNode = ClientCommandManager
		.literal("get")
		.executes(context -> {
			ItemUtil.checkHasItem(context.getSource());
			checkHasPages(context.getSource());
			
			ItemStack item = ItemUtil.getItemStack(context.getSource());
			NbtList pages = item.getNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);

			context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_GET));
			for (int i = 0; i < pages.size(); ++i) {
				Text textPage = Text.Serializer.fromJson(((NbtString)pages.get(i)).asString());
				Text feedback = Text.empty().append(Text.empty().append(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(textPage);
				context.getSource().sendFeedback(feedback);
			}
			return pages.size();
		})
		.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageGetIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemUtil.checkHasItem(context.getSource());
				checkHasPages(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource());
				int i = IntegerArgumentType.getInteger(context, "index");

				NbtList pages = item.getNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (pages.size() <= i) throw OUT_OF_BOUNDS_EXCEPTION.create(i, pages.size());
				Text textPage = Text.Serializer.fromJson(((NbtString)pages.get(i)).asString());
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_GET_PAGE, i, textPage));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> pageSetNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageSetIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 99))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");

				NbtList pages = item.getOrCreateNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (pages == null) pages = new NbtList();
				if (i < pages.size()) {
					pages.set(i, Colored.EMPTY_LINE);
				} else {
					int off = i - pages.size() + 1;
					for (int j = 0; j < off; ++j) pages.add(Colored.EMPTY_LINE);
				}
				item.setSubNbt(PAGES_KEY, pages);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_SET, i, ""));
				return 1;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> pageSetIndexPageNode = ClientCommandManager
			.argument("page", StringArgumentType.greedyString())
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				Text page = Colored.of(StringArgumentType.getString(context, "page"));

				NbtList pages = item.getOrCreateNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (pages == null) pages = new NbtList();
				if (i < pages.size()) {
					pages.set(i, NbtString.of(Text.Serializer.toJson(page)));
				} else {
					int off = i - pages.size();
					for (int j = 0; j < off; ++j) pages.add(Colored.EMPTY_LINE);
					pages.add(NbtString.of(Text.Serializer.toJson(page)));
				}
				item.setSubNbt(PAGES_KEY, pages);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_SET, i, page));
				return 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> pageRemoveNode = ClientCommandManager
			.literal("remove")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageRemoveIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				checkHasPages(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");

				NbtList pages = item.getNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (pages.size() < i) throw OUT_OF_BOUNDS_EXCEPTION.create(pages.size(), i);
				pages.remove(i);
				item.setSubNbt(PAGES_KEY, pages);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_REMOVE, i));
				return 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> pageAddNode = ClientCommandManager
			.literal("add")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				NbtList pages = item.getOrCreateNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (pages == null) pages = new NbtList();
				pages.add(Colored.EMPTY_LINE);
				item.setSubNbt(PAGES_KEY, pages);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_ADD, ""));
				return pages.size() - 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, String> pageAddPageNode = ClientCommandManager
			.argument("page", StringArgumentType.greedyString())
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				Text textPage = Colored.of(StringArgumentType.getString(context, "page"));

				NbtString page = NbtString.of(Text.Serializer.toJson(textPage));
				NbtList pages = item.getOrCreateNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (pages == null) pages = new NbtList();
				pages.add(page);
				item.setSubNbt(PAGES_KEY, pages);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_ADD, textPage));
				return pages.size() - 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> pageInsertNode = ClientCommandManager
			.literal("insert")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageInsertIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 63))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");

				NbtList pages = item.getOrCreateNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (pages == null) pages = new NbtList();
				if (i < pages.size()) {
					pages.add(i, Colored.EMPTY_LINE);
				} else {
					int off = i - pages.size();
					for (int j = 0; j < off; ++j) pages.add(Colored.EMPTY_LINE);
				}
				item.setSubNbt(PAGES_KEY, pages);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_INSERT, "", i));
				return 1;
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, String> pageInsertIndexPageNode = ClientCommandManager
			.argument("page", StringArgumentType.greedyString())
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");
				Text textPage = Colored.of(StringArgumentType.getString(context, "page"));

				NbtString page = NbtString.of(Text.Serializer.toJson(textPage));
				NbtList pages = item.getOrCreateNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (pages == null) pages = new NbtList();
				if (i < pages.size()) {
					pages.add(i, page);
				} else {
					int off = i - pages.size() - 1;
					for (int j = 0; j < off; ++j) pages.add(Colored.EMPTY_LINE);
					pages.add(page);
				}
				item.setSubNbt(PAGES_KEY, pages);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_INSERT, textPage, i));
				return 1;
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> pageClearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				checkHasPages(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();

				item.setSubNbt(PAGES_KEY, new NbtList());

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_CLEAR));
				return 1;
			})
			.build();
	
		LiteralCommandNode<FabricClientCommandSource> pageClearBeforeNode = ClientCommandManager
			.literal("before")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageClearBeforeIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 99))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				checkHasPages(context.getSource());
				
				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");

				NbtList pages = item.getOrCreateNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				for (int j = 0; j < i; ++j) pages.remove(0);
				item.setSubNbt(PAGES_KEY, pages);

				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_CLEAR_BEFORE, i));
				return 1;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> pageClearAfterNode = ClientCommandManager
			.literal("after")
			.build();
	
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageClearAfterIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 99))
			.executes(context -> {
				ItemUtil.checkCanEdit(context.getSource());
				checkHasPages(context.getSource());

				ItemStack item = ItemUtil.getItemStack(context.getSource()).copy();
				int i = IntegerArgumentType.getInteger(context, "index");

				NbtList pages = item.getOrCreateNbt().getList(PAGES_KEY, NbtElement.STRING_TYPE);
				if (i >= pages.size()) throw OUT_OF_BOUNDS_EXCEPTION.create(i, pages.size());
				int off = pages.size() - i - 1;
				for (int j = 0; j < off; ++j) pages.remove(pages.size() - 1);
				item.setSubNbt(PAGES_KEY, pages);
				
				ItemUtil.setItemStack(context.getSource(), item);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGES_CLEAR_AFTER, i));
				return 1;
			})
			.build();

		rootNode.addChild(node);

        // ... author ...
        node.addChild(authorNode);
        authorNode.addChild(authorGetNode);
        authorNode.addChild(authorSetNode);
        authorSetNode.addChild(authorSetAuthorNode);

        // ... title ...
        node.addChild(titleNode);
        titleNode.addChild(titleGetNode);
        titleNode.addChild(titleSetNode);
        titleSetNode.addChild(titleSetTitleNode);

        // ... generation ...
        node.addChild(generationNode);
        generationNode.addChild(generationGetNode);
        generationNode.addChild(generationSetNode);
        generationSetNode.addChild(generationSetGenerationNode);

        // ... page ...
        node.addChild(pageNode);
		pageNode.addChild(pageGetNode);
		pageGetNode.addChild(pageGetIndexNode);
		pageNode.addChild(pageSetNode);
		pageSetNode.addChild(pageSetIndexNode);
		pageSetIndexNode.addChild(pageSetIndexPageNode);
		pageNode.addChild(pageRemoveNode);
		pageRemoveNode.addChild(pageRemoveIndexNode);
		pageNode.addChild(pageAddNode);
		pageAddNode.addChild(pageAddPageNode);
		pageNode.addChild(pageInsertNode);
		pageInsertNode.addChild(pageInsertIndexNode);
		pageInsertIndexNode.addChild(pageInsertIndexPageNode);
		pageNode.addChild(pageClearNode);
		pageClearNode.addChild(pageClearBeforeNode);
		pageClearBeforeNode.addChild(pageClearBeforeIndexNode);
		pageClearNode.addChild(pageClearAfterNode);
		pageClearAfterNode.addChild(pageClearAfterIndexNode);
	}
}
