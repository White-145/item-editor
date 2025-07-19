package me.white.simpleitemeditor.node;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import me.white.simpleitemeditor.Node;
import me.white.simpleitemeditor.argument.LegacyTextArgumentType;
import me.white.simpleitemeditor.util.CommonCommandManager;
import me.white.simpleitemeditor.util.EditorUtil;
import me.white.simpleitemeditor.util.TextUtil;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WritableBookContentComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class BookNode implements Node {
    private static final CommandSyntaxException ISNT_WRITTEN_BOOK_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.isntwrittenbook")).create();
    private static final CommandSyntaxException ISNT_BOOK_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.isntbook")).create();
    private static final CommandSyntaxException NO_TITLE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.notitle")).create();
    private static final CommandSyntaxException NO_AUTHOR_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.noauthor")).create();
    private static final CommandSyntaxException NO_PAGES_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.nopages")).create();
    private static final CommandSyntaxException TITLE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.titlealreadyis")).create();
    private static final CommandSyntaxException AUTHOR_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.authoralreadyis")).create();
    private static final CommandSyntaxException PAGE_ALREADY_IS_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.edit.book.error.pagealreadyis")).create();
    private static final String OUTPUT_GET_TITLE = "commands.edit.book.gettitle";
    private static final String OUTPUT_SET_TITLE = "commands.edit.book.settitle";
    private static final String OUTPUT_GET_AUTHOR = "commands.edit.book.getauthor";
    private static final String OUTPUT_SET_AUTHOR = "commands.edit.book.setauthor";
    private static final String OUTPUT_PAGE_GET = "commands.edit.book.getpages";
    private static final String OUTPUT_PAGE_GET_SINGLE = "commands.edit.book.getpage";
    private static final String OUTPUT_PAGE_SET = "commands.edit.book.setpage";
    private static final String OUTPUT_PAGE_REMOVE = "commands.edit.book.removepage";
    private static final String OUTPUT_PAGE_ADD = "commands.edit.book.addpage";
    private static final String OUTPUT_PAGE_INSERT = "commands.edit.book.insertpage";
    private static final String OUTPUT_PAGE_CLEAR = "commands.edit.book.clearpages";
    private static final String OUTPUT_PAGE_CLEAR_BEFORE = "commands.edit.book.clearpagesbefore";
    private static final String OUTPUT_PAGE_CLEAR_AFTER = "commands.edit.book.clearpagesafter";
    private static final String DEFAULT_AUTHOR = "unknown";
    private static final String DEFAULT_TITLE = "unknown";

    private static boolean isBook(ItemStack stack, boolean isWritten) {
        return !isWritten && stack.getItem() == Items.WRITABLE_BOOK || stack.getItem() == Items.WRITTEN_BOOK;
    }

    private static boolean isWritten(ItemStack stack) {
        return stack.getItem() == Items.WRITTEN_BOOK;
    }

    private static boolean hasTitle(ItemStack stack) {
        return stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT);
    }

    private static boolean hasAuthor(ItemStack stack) {
        return stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT);
    }

    private static boolean hasPages(ItemStack stack) {
        if (stack.getItem() == Items.WRITABLE_BOOK) {
            if (!stack.contains(DataComponentTypes.WRITABLE_BOOK_CONTENT)) {
                return false;
            }
            return !stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT).pages().isEmpty();
        }
        if (stack.getItem() != Items.WRITTEN_BOOK) {
            return false;
        }
        if (!stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            return false;
        }
        return !stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).pages().isEmpty();
    }

    private static String getTitle(ItemStack stack) {
        return stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).title().get(false);
    }

    private static String getAuthor(ItemStack stack) {
        return stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).author();
    }

    private static List<Text> getPages(ItemStack stack) {
        if (stack.getItem() == Items.WRITABLE_BOOK) {
            if (!stack.contains(DataComponentTypes.WRITABLE_BOOK_CONTENT)) {
                return new ArrayList<>();
            }
            return new ArrayList<>(stack.get(DataComponentTypes.WRITABLE_BOOK_CONTENT).pages().stream().map(page -> (Text)Text.literal(page.get(false))).toList());
        }
        if (!stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            return new ArrayList<>();
        }
        return new ArrayList<>(stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).pages().stream().map(page -> page.get(false)).toList());
    }

    private static void setTitle(ItemStack stack, String title) {
        List<RawFilteredPair<Text>> pages = List.of();
        String author = DEFAULT_AUTHOR;
        int generation = 0;
        if (stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            WrittenBookContentComponent component = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
            pages = component.pages();
            author = component.author();
            generation = component.generation();
        }
        stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(RawFilteredPair.of(title), author, generation, pages, false));
    }

    private static void setAuthor(ItemStack stack, String author) {
        List<RawFilteredPair<Text>> pages = List.of();
        RawFilteredPair<String> title = RawFilteredPair.of(DEFAULT_TITLE);
        int generation = 0;
        if (stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
            WrittenBookContentComponent component = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
            pages = component.pages();
            title = component.title();
            generation = component.generation();
        }
        stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(title, author, generation, pages, false));
    }

    private static void setPages(ItemStack stack, List<Text> pages) {
        if (stack.getItem() == Items.WRITABLE_BOOK) {
            stack.set(DataComponentTypes.WRITABLE_BOOK_CONTENT, new WritableBookContentComponent(pages.stream().map(EditorUtil::textToString).map(RawFilteredPair::of).toList()));
        } else {
            RawFilteredPair<String> title = RawFilteredPair.of(DEFAULT_TITLE);
            String author = DEFAULT_AUTHOR;
            int generation = 0;
            if (stack.contains(DataComponentTypes.WRITTEN_BOOK_CONTENT)) {
                WrittenBookContentComponent component = stack.get(DataComponentTypes.WRITTEN_BOOK_CONTENT);
                title = component.title();
                author = component.author();
                generation = component.generation();
            }
            stack.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, new WrittenBookContentComponent(title, author, generation, pages.stream().map(RawFilteredPair::of).toList(), false));
        }
    }

    @Override
    public <S extends CommandSource> CommandNode<S> register(CommonCommandManager<S> commandManager, CommandRegistryAccess registryAccess) {
        CommandNode<S> node = commandManager.literal("book").build();

        CommandNode<S> titleNode = commandManager.literal("title").build();

        CommandNode<S> titleGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBook(stack, true)) {
                throw ISNT_WRITTEN_BOOK_EXCEPTION;
            }
            if (!hasTitle(stack)) {
                throw NO_TITLE_EXCEPTION;
            }
            String title = getTitle(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_TITLE, title));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> titleSetNode = commandManager.literal("set").build();

        CommandNode<S> titleSetTitleNode = commandManager.argument("title", StringArgumentType.greedyString()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, true)) {
                throw ISNT_WRITTEN_BOOK_EXCEPTION;
            }
            String title = StringArgumentType.getString(context, "title");
            if (hasAuthor(stack)) {
                String oldTitle = getTitle(stack);
                if (title.equals(oldTitle)) {
                    throw TITLE_ALREADY_IS_EXCEPTION;
                }
            }
            setTitle(stack, title);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_TITLE, title));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> authorNode = commandManager.literal("author").build();

        CommandNode<S> authorGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBook(stack, true)) {
                throw ISNT_WRITTEN_BOOK_EXCEPTION;
            }
            if (!hasAuthor(stack)) {
                throw NO_TITLE_EXCEPTION;
            }
            String author = getAuthor(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_GET_AUTHOR, author));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> authorSetNode = commandManager.literal("set").build();

        CommandNode<S> authorSetAuthorNode = commandManager.argument("author", StringArgumentType.greedyString()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, true)) {
                throw ISNT_WRITTEN_BOOK_EXCEPTION;
            }
            String author = StringArgumentType.getString(context, "author");
            if (hasAuthor(stack)) {
                String oldAuthor = getAuthor(stack);
                if (author.equals(oldAuthor)) {
                    throw AUTHOR_ALREADY_IS_EXCEPTION;
                }
            }
            setAuthor(stack, author);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_SET_AUTHOR, author));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageNode = commandManager.literal("page").build();

        CommandNode<S> pageGetNode = commandManager.literal("get").executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            if (!hasPages(stack)) {
                throw NO_PAGES_EXCEPTION;
            }
            List<Text> pages = getPages(stack);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_GET));
            for (int i = 0; i < pages.size(); ++i) {
                EditorUtil.sendFeedback(context.getSource(), Text.empty().append(Text.literal(i + ". ").setStyle(Style.EMPTY.withColor(Formatting.GRAY))).append(TextUtil.copyable(pages.get(i))));
            }
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageGetIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 99)).executes(context -> {
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource());
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasPages(stack)) {
                throw NO_PAGES_EXCEPTION;
            }
            List<Text> pages = getPages(stack);
            if (index >= pages.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
            }
            Text page = pages.get(index);

            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_GET_SINGLE, TextUtil.copyable(page)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageSetNode = commandManager.literal("set").build();

        CommandNode<S> pageSetIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 99)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            Text page = Text.empty();
            List<Text> pages = getPages(stack);
            if (index >= pages.size()) {
                int off = index - pages.size() + 1;
                for (int i = 0; i < off; ++i) {
                    pages.add(page);
                }
            } else if (isWritten(stack)) {
                Text oldPage = pages.get(index);
                if (oldPage.equals(page)) {
                    throw PAGE_ALREADY_IS_EXCEPTION;
                }
            } else {
                String oldPage = EditorUtil.textToString(pages.get(index));
                if (oldPage.equals(EditorUtil.textToString(page))) {
                    throw PAGE_ALREADY_IS_EXCEPTION;
                }
            }
            pages.set(index, page);
            setPages(stack, pages);
            if (!isWritten(stack)) {
                page = Text.literal(EditorUtil.textToString(page));
            }

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_SET, TextUtil.copyable(page)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageSetIndexPageNode = commandManager.argument("page", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            Text page = LegacyTextArgumentType.getText(context, "page");
            List<Text> pages = getPages(stack);
            if (index >= pages.size()) {
                int off = index - pages.size() + 1;
                for (int i = 0; i < off; ++i) {
                    pages.add(Text.empty());
                }
            } else if (isWritten(stack)) {
                Text oldPage = pages.get(index);
                if (oldPage.equals(page)) {
                    throw PAGE_ALREADY_IS_EXCEPTION;
                }
            } else {
                String oldPage = EditorUtil.textToString(pages.get(index));
                if (oldPage.equals(EditorUtil.textToString(page))) {
                    throw PAGE_ALREADY_IS_EXCEPTION;
                }
            }
            pages.set(index, page);
            setPages(stack, pages);
            if (!isWritten(stack)) {
                page = Text.literal(EditorUtil.textToString(page));
            }

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_SET, TextUtil.copyable(page)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageRemoveNode = commandManager.literal("remove").build();

        CommandNode<S> pageRemoveIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            if (!hasPages(stack)) {
                throw NO_PAGES_EXCEPTION;
            }
            List<Text> pages = getPages(stack);
            if (index >= pages.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
            }
            pages.remove(index);
            setPages(stack, pages);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_REMOVE));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageAddNode = commandManager.literal("add").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            Text page = Text.empty();
            List<Text> pages = getPages(stack);
            pages.add(page);
            setPages(stack, pages);
            if (!isWritten(stack)) {
                page = Text.literal(EditorUtil.textToString(page));
            }

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_ADD, TextUtil.copyable(page)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageAddPageNode = commandManager.argument("page", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            Text page = LegacyTextArgumentType.getText(context, "page");
            List<Text> pages = getPages(stack);
            pages.add(page);
            setPages(stack, pages);
            if (!isWritten(stack)) {
                page = Text.literal(EditorUtil.textToString(page));
            }

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_ADD, TextUtil.copyable(page)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageInsertNode = commandManager.literal("insert").build();

        CommandNode<S> pageInsertIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0, 99)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            Text page = Text.empty();
            List<Text> pages = getPages(stack);
            if (index > pages.size()) {
                int off = index - pages.size();
                for (int i = 0; i < off; ++i) {
                    pages.add(Text.empty());
                }
            }
            pages.add(index, page);
            setPages(stack, pages);
            if (!isWritten(stack)) {
                page = Text.literal(EditorUtil.textToString(page));
            }

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_INSERT, TextUtil.copyable(page)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageInsertIndexPageNode = commandManager.argument("page", LegacyTextArgumentType.text()).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            Text page = LegacyTextArgumentType.getText(context, "page");
            List<Text> pages = getPages(stack);
            if (index > pages.size()) {
                int off = index - pages.size();
                for (int i = 0; i < off; ++i) {
                    pages.add(Text.empty());
                }
            }
            pages.add(index, page);
            setPages(stack, pages);
            if (!isWritten(stack)) {
                page = Text.literal(EditorUtil.textToString(page));
            }

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_INSERT, TextUtil.copyable(page)));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageClearNode = commandManager.literal("clear").executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            List<Text> pages = getPages(stack);
            if (pages.isEmpty()) {
                throw NO_PAGES_EXCEPTION;
            }
            setPages(stack, List.of());

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_CLEAR));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageClearBeforeNode = commandManager.literal("before").build();

        CommandNode<S> pageClearBeforeIndexNode = commandManager.argument("index", IntegerArgumentType.integer(1)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<Text> pages = getPages(stack);
            if (pages.isEmpty()) {
                throw NO_PAGES_EXCEPTION;
            }
            if (index > pages.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size());
            }
            pages = pages.subList(index, pages.size());
            setPages(stack, pages);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_CLEAR_BEFORE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        CommandNode<S> pageClearAfterNode = commandManager.literal("after").build();

        CommandNode<S> pageClearAfterIndexNode = commandManager.argument("index", IntegerArgumentType.integer(0)).executes(context -> {
            EditorUtil.checkCanEdit(context.getSource());
            ItemStack stack = EditorUtil.getCheckedStack(context.getSource()).copy();
            if (!isBook(stack, false)) {
                throw ISNT_BOOK_EXCEPTION;
            }
            int index = IntegerArgumentType.getInteger(context, "index");
            List<Text> pages = getPages(stack);
            if (pages.isEmpty()) {
                throw NO_PAGES_EXCEPTION;
            }
            if (index >= pages.size()) {
                throw EditorUtil.OUT_OF_BOUNDS_EXCEPTION.create(index, pages.size() - 1);
            }
            pages = pages.subList(0, index + 1);
            setPages(stack, pages);

            EditorUtil.setStack(context.getSource(), stack);
            EditorUtil.sendFeedback(context.getSource(), Text.translatable(OUTPUT_PAGE_CLEAR_BEFORE, index));
            return Command.SINGLE_SUCCESS;
        }).build();

        // ... title
        node.addChild(titleNode);
        // ... get
        titleNode.addChild(titleGetNode);
        // ... set <title>
        titleNode.addChild(titleSetNode);
        titleSetNode.addChild(titleSetTitleNode);

        // ... author
        node.addChild(authorNode);
        // ... get
        authorNode.addChild(authorGetNode);
        // ... set <author>
        authorNode.addChild(authorSetNode);
        authorSetNode.addChild(authorSetAuthorNode);

        // ... page
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
        // ... clear before <index>
        pageClearNode.addChild(pageClearBeforeNode);
        pageClearBeforeNode.addChild(pageClearBeforeIndexNode);
        // ... clear after <index>
        pageClearNode.addChild(pageClearAfterNode);
        pageClearAfterNode.addChild(pageClearAfterIndexNode);

        return node;
    }
}
