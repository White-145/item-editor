package me.white.itemeditor.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.math.Vec3d;

public class PositionArgument {
    private static class CoordinateArgument {
        double value;
        boolean isRelative;

        private CoordinateArgument(double value, boolean isRelative) {
            this.value = value;
            this.isRelative = isRelative;
        }

        public static CoordinateArgument parse(StringReader reader) throws CommandSyntaxException {
            if (reader.peek() == '~') {
                reader.skip();
                double value = 0.0;
                if (reader.canRead() && reader.peek() != ' ') value = reader.readDouble();
                return new CoordinateArgument(value, true);
            }
            return new CoordinateArgument(reader.readDouble(), false);
        }

        public double toAbsolute(double initial) {
            return isRelative ? initial + value : value;
        }
    }

    CoordinateArgument x;
    CoordinateArgument y;
    CoordinateArgument z;

    private PositionArgument(CoordinateArgument x, CoordinateArgument y, CoordinateArgument z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static PositionArgument parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        CoordinateArgument x = CoordinateArgument.parse(reader);
        if (!reader.canRead(2) || reader.read() != ' ') {
            reader.setCursor(cursor);
            throw PositionArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
        }
        CoordinateArgument y = CoordinateArgument.parse(reader);
        if (!reader.canRead(2) || reader.read() != ' ') {
            reader.setCursor(cursor);
            throw PositionArgumentType.INCOMPLETE_EXCEPTION.createWithContext(reader);
        }
        CoordinateArgument z = CoordinateArgument.parse(reader);
        return new PositionArgument(x, y, z);
    }

    public Vec3d toAbsolute(Vec3d initial) {
        return new Vec3d(x.toAbsolute(initial.getX()), y.toAbsolute(initial.getY()), z.toAbsolute(initial.getZ()));
    }
}
