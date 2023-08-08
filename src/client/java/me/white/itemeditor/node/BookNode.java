package me.white.itemeditor.node;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import me.white.itemeditor.argument.EnumArgumentType;
import me.white.itemeditor.argument.TextArgumentType;
import me.white.itemeditor.util.ItemUtil;
import me.white.itemeditor.util.EditorUtil;
import me.white.itemeditor.util.TextUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class BookNode implements Node {
	public static final CommandSyntaxException CANNOT_EDIT_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.cannotedit")).create();
	public static final CommandSyntaxException NO_AUTHOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.noauthor")).create();
	public static final CommandSyntaxException AUTHOR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.authoralreadyis")).create();
	public static final CommandSyntaxException NO_TITLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.notitle")).create();
	public static final CommandSyntaxException TITLE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.titlealreadyis")).create();
	public static final CommandSyntaxException GENERATION_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.generationalreadyis")).create();
	public static final CommandSyntaxException NO_PAGES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.nopages")).create();
	public static final CommandSyntaxException PAGE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.pagealreadyis")).create();
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

	private enum Generation {
		ORIGINAL(0, "commands.edit.book.generationoriginal"),
		COPY(1, "commands.edit.book.generationcopy"),
		COPY_OF_COPY(2, "commands.edit.book.generationcopyofcopy"),
		TATTERED(3, "commands.edit.book.generationtattered");

		final int id;
		final String translationKey;

		Generation(int id, String translationKey) {
			this.id = id;
			this.translationKey = translationKey;
		}

		public static Generation byId(int id) {
			return switch (id) {
				case 0 -> ORIGINAL;
				case 1 -> COPY;
				case 2 -> COPY_OF_COPY;
				case 3 -> TATTERED;
				default -> null;
			};
		}
	}

	private static boolean canEdit(ItemStack stack) {
		Item item = stack.getItem();
		return item == Items.WRITTEN_BOOK;
	}

	public void register(LiteralCommandNode<FabricClientCommandSource> rootNode, CommandRegistryAccess registryAccess) {
		LiteralCommandNode<FabricClientCommandSource> node = ClientCommandManager
				.literal("book")
				.build();

		LiteralCommandNode<FabricClientCommandSource> authorNode = ClientCommandManager
				.literal("author")
				.build();

		LiteralCommandNode<FabricClientCommandSource> authorGetNode = ClientCommandManager
				.literal("get")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource());
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
					if (!ItemUtil.hasBookAuthor(stack)) throw NO_AUTHOR_EXCEPTION;
					String author = ItemUtil.getBookAuthor(stack);

					context.getSource().sendFeedback(Text.translatable(OUTPUT_AUTHOR_GET, TextUtil.copyableTextComponent(author)));
					return 1;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> authorSetNode = ClientCommandManager
				.literal("set")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
					if (!ItemUtil.hasBookAuthor(stack)) throw NO_AUTHOR_EXCEPTION;
					ItemUtil.setBookAuthor(stack, null);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_AUTHOR_RESET));
					return 1;
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, String> authorSetAuthorNode = ClientCommandManager
				.argument("author", StringArgumentType.greedyString())
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
					String oldAuthor = ItemUtil.getBookAuthor(stack);
					String author = StringArgumentType.getString(context, "author");
					if (oldAuthor != null && oldAuthor.equals(author)) throw AUTHOR_ALREADY_IS_EXCEPTION;
					ItemUtil.setBookAuthor(stack, author);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_AUTHOR_SET, TextUtil.copyableTextComponent(author)));
					return 1;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> titleNode = ClientCommandManager
				.literal("title")
				.build();

		LiteralCommandNode<FabricClientCommandSource> titleGetNode = ClientCommandManager
				.literal("get")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource());
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
					if (!ItemUtil.hasBookTitle(stack)) throw NO_TITLE_EXCEPTION;
					String title = ItemUtil.getBookTitle(stack);

					context.getSource().sendFeedback(Text.translatable(OUTPUT_TITLE_GET, TextUtil.copyableTextComponent(title)));
					return 1;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> titleSetNode = ClientCommandManager
				.literal("set")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
					if (!ItemUtil.hasBookTitle(stack)) throw NO_TITLE_EXCEPTION;
					ItemUtil.setBookTitle(stack, null);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_TITLE_RESET));
					return 1;
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, String> titleSetTitleNode = ClientCommandManager
				.argument("title", StringArgumentType.greedyString())
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
					String oldTitle = ItemUtil.getBookTitle(stack);
					String title = StringArgumentType.getString(context, "title");
					if (oldTitle != null && oldTitle.equals(title)) throw TITLE_ALREADY_IS_EXCEPTION;
					ItemUtil.setBookTitle(stack, title);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_TITLE_SET, TextUtil.copyableTextComponent(title)));
					return 1;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> generationNode = ClientCommandManager
				.literal("generation")
				.build();

		LiteralCommandNode<FabricClientCommandSource> generationGetNode = ClientCommandManager
				.literal("get")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource());
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
					int generation = ItemUtil.getBookGeneration(stack);

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
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!canEdit(stack)) throw CANNOT_EDIT_EXCEPTION;
					int oldGeneration = ItemUtil.getBookGeneration(stack);
					Generation generation = EnumArgumentType.getEnum(context, "generation", Generation.class);
					if (oldGeneration == generation.id) throw GENERATION_ALREADY_IS_EXCEPTION;
					ItemUtil.setBookGeneration(stack, generation.id);

					EditorUtil.setStack(context.getSource(), stack);
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
					ItemStack stack = EditorUtil.getStack(context.getSource());
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!ItemUtil.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
					List<Text> pages = ItemUtil.getBookPages(stack);

					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_GET));
					for (int i = 0; i < pages.size(); ++i) {
						context.getSource().sendFeedback(Text.empty()
								.append(Text.literal(String.format("%d. ", i)).setStyle(Style.EMPTY.withColor(Formatting.GRAY)))
								.append(TextUtil.copyableTextComponent(pages.get(i)))
						);
					}
					return pages.size();
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> pageGetIndexNode = ClientCommandManager
				.argument("index", IntegerArgumentType.integer(0))
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource());
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					int index = IntegerArgumentType.getInteger(context, "index");
					if (!ItemUtil.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
					List<Text> pages = ItemUtil.getBookPages(stack);
					if (pages.size() <= index) throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
					Text page = pages.get(index);

					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_GET_PAGE, index, TextUtil.copyableTextComponent(page)));
					return pages.size();
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> pageSetNode = ClientCommandManager
				.literal("set")
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> pageSetIndexNode = ClientCommandManager
				.argument("index", IntegerArgumentType.integer(0, 255))
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					int index = IntegerArgumentType.getInteger(context, "index");
					List<Text> pages = new ArrayList<>(ItemUtil.getBookPages(stack));
					if (pages.size() <= index) {
						int off = index - pages.size() + 1;
						for (int i = 0; i < off; ++i) {
							pages.add(Text.empty());
						}
					} else {
						Text oldPage = pages.get(index);
						if (oldPage.equals(Text.empty())) throw PAGE_ALREADY_IS_EXCEPTION;
					}
					pages.set(index, Text.empty());
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_SET, index, ""));
					return pages.size();
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Text> pageSetIndexPageNode = ClientCommandManager
				.argument("page", TextArgumentType.all())
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					int index = IntegerArgumentType.getInteger(context, "index");
					Text page = TextArgumentType.getText(context, "page");
					List<Text> pages = new ArrayList<>(ItemUtil.getBookPages(stack));
					if (pages.size() <= index) {
						int off = index - pages.size() + 1;
						for (int i = 0; i < off; ++i) {
							pages.add(Text.empty());
						}
					} else {
						Text oldPage = pages.get(index);
						if (oldPage.equals(page)) throw PAGE_ALREADY_IS_EXCEPTION;
					}
					pages.set(index, page);
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_SET, index, TextUtil.copyableTextComponent(page)));
					return pages.size();
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> pageRemoveNode = ClientCommandManager
				.literal("remove")
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> pageRemoveIndexNode = ClientCommandManager
				.argument("index", IntegerArgumentType.integer(0))
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					int index = IntegerArgumentType.getInteger(context, "index");
					if (!ItemUtil.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
					List<Text> pages = new ArrayList<>(ItemUtil.getBookPages(stack));
					if (pages.size() <= index) throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
					pages.remove(index);
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_REMOVE, index));
					return pages.size();
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> pageAddNode = ClientCommandManager
				.literal("add")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					List<Text> pages = new ArrayList<>(ItemUtil.getBookPages(stack));
					pages.add(Text.empty());
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_ADD, ""));
					return pages.size() - 1;
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Text> pageAddPageNode = ClientCommandManager
				.argument("page", TextArgumentType.all())
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					Text page = TextArgumentType.getText(context, "page");
					List<Text> pages = new ArrayList<>(ItemUtil.getBookPages(stack));
					pages.add(page);
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_ADD, TextUtil.copyableTextComponent(page)));
					return pages.size() - 1;
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> pageInsertNode = ClientCommandManager
				.literal("insert")
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Integer> pageInsertIndexNode = ClientCommandManager
				.argument("index", IntegerArgumentType.integer(0, 255))
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					int index = IntegerArgumentType.getInteger(context, "index");
					List<Text> pages = new ArrayList<>(ItemUtil.getBookPages(stack));
					if (pages.size() <= index) {
						int off = index - pages.size();
						for (int i = 0; i < off; ++i) {
							pages.add(Text.empty());
						}
					}
					pages.add(index, Text.empty());
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_INSERT, "", index));
					return pages.size();
				})
				.build();

		ArgumentCommandNode<FabricClientCommandSource, Text> pageInsertIndexPageNode = ClientCommandManager
				.argument("page", TextArgumentType.all())
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					int index = IntegerArgumentType.getInteger(context, "index");
					Text page = TextArgumentType.getText(context, "page");
					List<Text> pages = new ArrayList<>(ItemUtil.getBookPages(stack));
					if (pages.size() <= index) {
						int off = index - pages.size();
						for (int i = 0; i < off; ++i) {
							pages.add(Text.empty());
						}
					}
					pages.add(index, page);
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_INSERT, TextUtil.copyableTextComponent(page), index));
					return pages.size();
				})
				.build();

		LiteralCommandNode<FabricClientCommandSource> pageClearNode = ClientCommandManager
				.literal("clear")
				.executes(context -> {
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!ItemUtil.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
					int old = ItemUtil.getBookPages(stack).size();
					ItemUtil.setBookPages(stack, null);

					EditorUtil.setStack(context.getSource(), stack);
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
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!ItemUtil.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
					int index = IntegerArgumentType.getInteger(context, "index");
					List<Text> pages = ItemUtil.getBookPages(stack);
					if (pages.size() <= index) throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
					pages = pages.subList(index, pages.size());
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
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
					ItemStack stack = EditorUtil.getStack(context.getSource()).copy();
					if (!EditorUtil.hasCreative(context.getSource())) throw EditorUtil.NOT_CREATIVE_EXCEPTION;
					if (!EditorUtil.hasItem(stack)) throw EditorUtil.NO_ITEM_EXCEPTION;
					if (!ItemUtil.hasBookPages(stack)) throw NO_PAGES_EXCEPTION;
					int index = IntegerArgumentType.getInteger(context, "index");
					List<Text> pages = ItemUtil.getBookPages(stack);
					if (pages.size() <= index) throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
					pages = pages.subList(0, index + 1);
					ItemUtil.setBookPages(stack, pages);

					EditorUtil.setStack(context.getSource(), stack);
					context.getSource().sendFeedback(Text.translatable(OUTPUT_PAGE_CLEAR_AFTER, index));
					return pages.size();
				})
				.build();

		rootNode.addChild(node);

		// ... author ...
		node.addChild(authorNode);
		// ... get
		authorNode.addChild(authorGetNode);
		// ... set [<author>]
		authorNode.addChild(authorSetNode);
		authorSetNode.addChild(authorSetAuthorNode);

		// ... title ...
		node.addChild(titleNode);
		// ... get
		titleNode.addChild(titleGetNode);
		// ... set [<title>]
		titleNode.addChild(titleSetNode);
		titleSetNode.addChild(titleSetTitleNode);

		// ... generation ...
		node.addChild(generationNode);
		// ... get
		generationNode.addChild(generationGetNode);
		// ... set <generation>
		generationNode.addChild(generationSetNode);
		generationSetNode.addChild(generationSetGenerationNode);

		// ... page ...
		node.addChild(pageNode);
		// ... get [<index>]
		pageNode.addChild(pageGetNode);
		pageGetNode.addChild(pageGetIndexNode);
		// ... set <index> [<page>]
		pageNode.addChild(pageSetNode);
		pageSetNode.addChild(pageSetIndexNode);
		pageSetIndexNode.addChild(pageSetIndexPageNode);
		// ... remove <index>
		pageNode.addChild(pageRemoveNode);
		pageRemoveNode.addChild(pageRemoveIndexNode);
		// ... add [<page>]
		pageNode.addChild(pageAddNode);
		pageAddNode.addChild(pageAddPageNode);
		// ... insert <index> [<page>]
		pageNode.addChild(pageInsertNode);
		pageInsertNode.addChild(pageInsertIndexNode);
		pageInsertIndexNode.addChild(pageInsertIndexPageNode);
		// ... clear
		pageNode.addChild(pageClearNode);
		// ... before <index>
		pageClearNode.addChild(pageClearBeforeNode);
		pageClearBeforeNode.addChild(pageClearBeforeIndexNode);
		// ... after <index>
		pageClearNode.addChild(pageClearAfterNode);
		pageClearAfterNode.addChild(pageClearAfterIndexNode);
	}
}
