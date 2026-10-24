package com.sakura.companion;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Tongkat pembangun. Klik kanan di tanah untuk membangun.
 * Bangunan membentang ke arah +X (timur) dan +Z (selatan) dari titik yang diklik.
 * Hati-hati: area akan dikosongkan dulu (apa pun di dalamnya terhapus).
 */
public class BuilderItem extends Item {
    public enum Kind { HOSPITAL, TOWN }

    private static final int W = 17, D = 13, H = 5;
    private final Kind kind;

    public BuilderItem(Properties props, Kind kind) {
        super(props);
        this.kind = kind;
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        if (level.isClientSide()) return InteractionResult.SUCCESS;
        Player player = ctx.getPlayer();
        BlockPos origin = ctx.getClickedPos().above();

        if (kind == Kind.HOSPITAL) {
            hospital(level, origin);
            msg(player, "Rumah sakit selesai dibangun (membentang ke timur dan selatan).");
        } else {
            town(level, origin);
            msg(player, "Kota kecil selesai dibangun, dengan rumah sakit di tengah.");
        }
        return InteractionResult.SUCCESS;
    }

    private static void msg(Player p, String s) {
        if (p != null) p.displayClientMessage(Component.literal("§b[Sakura]§r " + s), false);
    }

    // ---------- util ----------
    private static void set(Level l, BlockPos o, int x, int y, int z, BlockState s) {
        l.setBlock(o.offset(x, y, z), s, Block.UPDATE_ALL);
    }

    private static void fill(Level l, BlockPos o, int x1, int y1, int z1, int x2, int y2, int z2, BlockState s) {
        for (int x = x1; x <= x2; x++)
            for (int y = y1; y <= y2; y++)
                for (int z = z1; z <= z2; z++)
                    set(l, o, x, y, z, s);
    }

    private static void bed(Level l, BlockPos o, int x, int footZ, BlockState bedBase) {
        BlockState foot = bedBase.setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.FOOT);
        BlockState head = bedBase.setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD);
        set(l, o, x, 0, footZ, foot);
        set(l, o, x, 0, footZ - 1, head);
    }

    private static void door(Level l, BlockPos o, int x, int z) {
        BlockState d = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.SOUTH);
        set(l, o, x, 0, z, d.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        set(l, o, x, 1, z, d.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
    }

    // ---------- rumah sakit ----------
    private static void hospital(Level l, BlockPos o) {
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState wall = Blocks.WHITE_CONCRETE.defaultBlockState();
        BlockState floor = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState glass = Blocks.GLASS.defaultBlockState();
        BlockState red = Blocks.RED_CONCRETE.defaultBlockState();

        fill(l, o, -1, 0, -1, W, H + 3, D, air);                 // kosongkan area
        fill(l, o, -1, -1, -1, W, -1, D, Blocks.STONE_BRICKS.defaultBlockState()); // halaman
        fill(l, o, 0, -1, 0, W - 1, -1, D - 1, floor);           // lantai
        fill(l, o, 0, 0, 0, W - 1, H - 1, D - 1, wall);          // balok dinding
        fill(l, o, 1, 0, 1, W - 2, H - 1, D - 2, air);           // ronggakan isi
        fill(l, o, 0, H, 0, W - 1, H, D - 1, Blocks.QUARTZ_BLOCK.defaultBlockState()); // atap

        // jendela
        for (int x = 2; x < W - 2; x += 3) {
            if (Math.abs(x - W / 2) <= 1) continue;
            fill(l, o, x, 1, 0, x, 2, 0, glass);
            fill(l, o, x, 1, D - 1, x, 2, D - 1, glass);
        }
        for (int z = 2; z < D - 2; z += 3) {
            fill(l, o, 0, 1, z, 0, 2, z, glass);
            fill(l, o, W - 1, 1, z, W - 1, 2, z, glass);
        }

        // pintu depan (selatan) + tanda palang merah
        int cx = W / 2;
        set(l, o, cx, 0, D - 1, air);
        set(l, o, cx, 1, D - 1, air);
        door(l, o, cx, D - 1);
        set(l, o, cx, 2, D - 1, red);
        set(l, o, cx, 3, D - 1, red);
        set(l, o, cx, 4, D - 1, red);
        set(l, o, cx - 1, 3, D - 1, red);
        set(l, o, cx + 1, 3, D - 1, red);

        // sekat tengah dengan pintu lengkung
        fill(l, o, 1, 0, 6, W - 2, H - 1, 6, wall);
        fill(l, o, cx - 1, 0, 6, cx + 1, 2, 6, air);

        // ruang bersalin (utara): 4 tempat tidur
        BlockState bed = Blocks.RED_BED.defaultBlockState();
        for (int x : new int[]{3, 6, 10, 13}) bed(l, o, x, 3, bed);
        for (int x : new int[]{4, 8, 12}) set(l, o, x, 0, 2, Blocks.OAK_PLANKS.defaultBlockState()); // meja kecil

        // lobi (selatan): meja resepsionis
        fill(l, o, 5, 0, 9, 11, 0, 9, Blocks.OAK_PLANKS.defaultBlockState());
        set(l, o, 5, 1, 9, Blocks.LANTERN.defaultBlockState());
        set(l, o, 11, 1, 9, Blocks.LANTERN.defaultBlockState());
        // bangku tunggu
        for (int x : new int[]{2, 3, 13, 14})
            set(l, o, x, 0, 11, Blocks.OAK_STAIRS.defaultBlockState());

        // lampu
        for (int x : new int[]{4, 12})
            for (int z : new int[]{3, 9})
                set(l, o, x, H - 1, z, Blocks.SEA_LANTERN.defaultBlockState());
        set(l, o, cx, H - 1, 6, Blocks.SEA_LANTERN.defaultBlockState());
    }

    // ---------- rumah warga ----------
    private static void house(Level l, BlockPos o) {
        BlockState air = Blocks.AIR.defaultBlockState();
        fill(l, o, 0, 0, 0, 6, 5, 6, air);
        fill(l, o, 0, -1, 0, 6, -1, 6, Blocks.COBBLESTONE.defaultBlockState());
        fill(l, o, 0, 0, 0, 6, 3, 6, Blocks.OAK_PLANKS.defaultBlockState());
        fill(l, o, 1, 0, 1, 5, 3, 5, air);
        fill(l, o, 0, 4, 0, 6, 4, 6, Blocks.STONE_BRICKS.defaultBlockState());
        // jendela
        for (int i : new int[]{2, 4}) {
            fill(l, o, i, 1, 0, i, 2, 0, Blocks.GLASS.defaultBlockState());
            fill(l, o, 0, 1, i, 0, 2, i, Blocks.GLASS.defaultBlockState());
            fill(l, o, 6, 1, i, 6, 2, i, Blocks.GLASS.defaultBlockState());
        }
        set(l, o, 3, 0, 6, air);
        set(l, o, 3, 1, 6, air);
        door(l, o, 3, 6);
        bed(l, o, 1, 2, Blocks.BLUE_BED.defaultBlockState());
        set(l, o, 5, 0, 1, Blocks.CRAFTING_TABLE.defaultBlockState());
        set(l, o, 3, 3, 3, Blocks.LANTERN.defaultBlockState()); // tergantung di langit-langit? (dibawah atap)
    }

    // ---------- kota ----------
    private static void town(Level l, BlockPos o) {
        // jalan/plaza di depan rumah sakit
        fill(l, o, -10, -1, D, W + 9, -1, D + 3, Blocks.STONE_BRICKS.defaultBlockState());
        fill(l, o, -10, 0, D, W + 9, 4, D + 3, Blocks.AIR.defaultBlockState());
        hospital(l, o);
        // rumah di barat dan timur
        BlockPos[] spots = {
                o.offset(-9, 0, 0), o.offset(-9, 0, 8),
                o.offset(W + 2, 0, 0), o.offset(W + 2, 0, 8)};
        for (BlockPos p : spots) house(l, p);
        // obor jalan
        for (int x = -9; x <= W + 8; x += 6) {
            set(l, o, x, 0, D + 3, Blocks.OAK_FENCE.defaultBlockState());
            set(l, o, x, 1, D + 3, Blocks.OAK_FENCE.defaultBlockState());
            set(l, o, x, 2, D + 3, Blocks.LANTERN.defaultBlockState());
        }
    }
}
