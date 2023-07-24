package me.white.itemeditor.node;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.argument.TextArgumentType;
import me.white.itemeditor.util.EditHelper;
import me.white.itemeditor.util.Util;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
    private static final String OUTPUT_AUTHOR_RESET = "commands.edit.book.authorreset";
    private static final String OUTPUT_TITLE_GET = "commands.edit.book.titleget";
    private static final String OUTPUT_TITLE_SET = "commands.edit.book.titleset";
    private static final String OUTPUT_TITLE_RESET = "commands.edit.book.titlereset";
    private static final String OUTPUT_GENERATION_GET = "commands.edit.book.generationget";
    private static final String OUTPUT_GENERATION_SET = "commands.edit.book.generationset";
    private static final String OUTPUT_PAGE_GET = "commands.edit.book.pageget";
    private static final String OUTPUT_PAGE_GET_PAGE = "commands.edit.book.pagegetpage";
    private static final String OUTPUT_PAGE_ADD = "commands.edit.book.pageadd";
    private static final String OUTPUT_PAGE_SET = "commands.edit.book.pageset";
    private static final String OUTPUT_PAGE_INSERT = "commands.edit.book.pageinsert";
    private static final String OUTPUT_PAGE_REMOVE = "commands.edit.book.pageremove";
    private static final String OUTPUT_PAGE_CLEAR = "commands.edit.book.pageclear";
    private static final String OUTPUT_PAGE_CLEAR_BEFORE = "commands.edit.book.pageclearbefore";
    private static final String OUTPUT_PAGE_CLEAR_AFTER = "commands.edit.book.pageclearafter";

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

    private static boolean canEdit(ItemStack stack) {
		Item item = stack.getItem();
        return item == Items.WRITTEN_BOOK;
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
                ItemStack stack = Util.getItemStack(context.getSource());
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				if (!EditHelper.hasBookAuthor(stack)) throw NO_AUTHOR_EXCEPTION;
				String author = EditHelper.getBookAuthor(stack);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_AUTHOR_GET, author));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> authorSetNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				if (!EditHelper.hasBookAuthor(stack)) throw NO_AUTHOR_EXCEPTION;
				EditHelper.setBookAuthor(stack, null);

				Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_AUTHOR_RESET));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, String> authorSetAuthorNode = ClientCommandManager
            .argument("author", StringArgumentType.greedyString())
            .executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                String author = StringArgumentType.getString(context, "author");
				EditHelper.setBookAuthor(stack, author);

				Util.setItemStack(context.getSource(), stack);
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
                ItemStack stack = Util.getItemStack(context.getSource());
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				if (!EditHelper.hasBookTitle(stack)) throw NO_TITLE_EXCEPTION;
				String title = EditHelper.getBookTitle(stack);

                context.getSource().sendFeedback(Text.translatable(OUTPUT_TITLE_GET, title));
                return 1;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> titleSetNode = ClientCommandManager
            .literal("set")
            .executes(context -> {
                ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				if (!EditHelper.hasBookTitle(stack)) throw NO_TITLE_EXCEPTION;
				EditHelper.setBookTitle(stack, null);

				Util.setItemStack(context.getSource(), stack);
                context.getSource().sendFeedback(Text.translatable(OUTPUT_TITLE_RESET));
                return 1;
            })
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, String> titleSetTitleNode = ClientCommandManager
            .argument("title", StringArgumentType.greedyString())
            .executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
                String title = StringArgumentType.getString(context, "title");
				EditHelper.setBookTitle(stack, title);

				Util.setItemStack(context.getSource(), stack);
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
				ItemStack stack = Util.getItemStack(context.getSource());
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				int generation = EditHelper.getBookGeneration(stack);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_GENERATION_GET, Text.translatable(Generation.byId(generation).translationKey)));
				return generation;
            })
            .build();
        
        LiteralCommandNode<FabricClientCommandSource> generationSetNode = ClientCommandManager
            .literal("set")
            .build();
        
        ArgumentCommandNode<FabricClientCommandSource, Generation> generationSetGenerationNode = ClientCommandManager
			.argument("generation", EnumArgumentType.enumArgument(Generation.class))
            .executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
				Generation generation = EnumArgumentType.getEnum(context, "generation", Generation.class);
				EditHelper.setBookGeneration(stack, generation.id);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_GENERATION_SET, Text.translatable(generation.translationKey)));
				return generation.id;
            })
            .build();
				
		LiteralCommandNode<FabricClientCommandSource> pageNode = ClientCommandManager
			.literal("page")
			.build();

		LiteralCommandNode<FabricClientCommandSource> pageGetNode = ClientCommandManager
			.literal("get")
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource());
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
				List<Text> pages = EditHelper.getBookPages(stack);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_GET));
				for (int i = 0; i < pages.size(); ++i) {
					context.getSource().sendFeedback(Text.empty()
						.append(Text.literal(String.valueOf(i) + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
						.append(pages.get(i))
					);
				}
				return pages.size();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> pageGetIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource());
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				if (!EditHelper.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
				List<Text> pages = EditHelper.getBookPages(stack);
				if (pages.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
				Text page = pages.get(index);

				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_GET_PAGE, page));
				return pages.size();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> pageSetNode = ClientCommandManager
			.literal("set")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageSetIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 255))
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				List<Text> pages = new ArrayList<>(EditHelper.getBookPages(stack));
				if (pages.size() <= index) {
					int off = pages.size() - index + 1;
					for (int i = 0; i < off; ++i) {
						pages.add(Text.empty());
					}
				}
				pages.set(index, Text.empty());
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_SET, index, ""));
				return pages.size();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Text> pageSetIndexPageNode = ClientCommandManager
			.argument("page", TextArgumentType.all())
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				Text page = TextArgumentType.getText(context, "page");
				List<Text> pages = new ArrayList<>(EditHelper.getBookPages(stack));
				if (pages.size() <= index) {
					int off = pages.size() - index + 1;
					for (int i = 0; i < off; ++i) {
						pages.add(Text.empty());
					}
				}
				pages.set(index, page);
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_SET, index, page));
				return pages.size();
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> pageRemoveNode = ClientCommandManager
			.literal("remove")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageRemoveIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				if (!EditHelper.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
				List<Text> pages = new ArrayList<>(EditHelper.getBookPages(stack));
				if (pages.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
				pages.remove(index);
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_REMOVE, index));
				return pages.size();
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> pageAddNode = ClientCommandManager
			.literal("add")
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				List<Text> pages = new ArrayList<>(EditHelper.getBookPages(stack));
				pages.add(Text.empty());
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_ADD, ""));
				return pages.size() - 1;
			})
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Text> pageAddPageNode = ClientCommandManager
			.argument("page", TextArgumentType.all())
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				Text page = TextArgumentType.getText(context, "page");
				List<Text> pages = new ArrayList<>(EditHelper.getBookPages(stack));
				pages.add(page);
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_ADD, page));
				return pages.size() - 1;
			})
			.build();
			
		LiteralCommandNode<FabricClientCommandSource> pageInsertNode = ClientCommandManager
			.literal("insert")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageInsertIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0, 255))
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				List<Text> pages = new ArrayList<>(EditHelper.getBookPages(stack));
				if (pages.size() <= index) {
					int off = pages.size() - index;
					for (int i = 0; i < off; ++i) {
						pages.add(Text.empty());
					}
				}
				pages.add(index, Text.empty());
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_INSERT, index, ""));
				return pages.size();
			})
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Text> pageInsertIndexPageNode = ClientCommandManager
			.argument("page", TextArgumentType.all())
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				Text page = TextArgumentType.getText(context, "page");
				List<Text> pages = new ArrayList<>(EditHelper.getBookPages(stack));
				if (pages.size() <= index) {
					int off = pages.size() - index;
					for (int i = 0; i < off; ++i) {
						pages.add(Text.empty());
					}
				}
				pages.add(index, page);
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_INSERT, index, page));
				return pages.size();
			})
			.build();
		
		LiteralCommandNode<FabricClientCommandSource> pageClearNode = ClientCommandManager
			.literal("clear")
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				if (!EditHelper.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
				int old = EditHelper.getBookPages(stack).size();
				EditHelper.setBookPages(stack, null);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_CLEAR));
				return old;
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> pageClearBeforeNode = ClientCommandManager
			.literal("before")
			.build();
		
		ArgumentCommandNode<FabricClientCommandSource, Integer> pageClearBeforeIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				if (!EditHelper.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
				List<Text> pages = EditHelper.getBookPages(stack);
				if (pages.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
				pages = pages.subList(index, pages.size());
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_CLEAR_BEFORE, index));
				return pages.size();
			})
			.build();

		LiteralCommandNode<FabricClientCommandSource> pageClearAfterNode = ClientCommandManager
			.literal("after")
			.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> pageClearAfterIndexNode = ClientCommandManager
			.argument("index", IntegerArgumentType.integer(0))
			.executes(context -> {
				ItemStack stack = Util.getItemStack(context.getSource()).copy();
				if (!Util.hasCreative(context.getSource())) throw Util.NOT_CREATIVE_EXCEPTION;
				if (!Util.hasItem(stack)) throw Util.NO_ITEM_EXCEPTION;
				int index = IntegerArgumentType.getInteger(context, "index");
				if (!EditHelper.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
				List<Text> pages = EditHelper.getBookPages(stack);
				if (pages.size() <= index) throw OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
				pages = pages.subList(0, index + 1);
				EditHelper.setBookPages(stack, pages);

				Util.setItemStack(context.getSource(), stack);
				context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_CLEAR_AFTER, index));
				return pages.size();
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
