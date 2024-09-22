package me.white.simpleitemeditor.argument.enums;

import com.mojang.brigadier.context.CommandContext;
import me.white.simpleitemeditor.argument.EnumArgumentType;
import me.white.simpleitemeditor.node.DataNode;

public class DataSourceArgumentType extends EnumArgumentType<DataNode.DataSource> {
    protected DataSourceArgumentType() {
        super(DataNode.DataSource.class);
    }

    public static DataSourceArgumentType dataSource() {
        return new DataSourceArgumentType();
    }

    public static DataNode.DataSource getDataSource(CommandContext<?> context, String name) {
        return context.getArgument(name, DataNode.DataSource.class);
    }
}
