package com.supermartijn642.wormhole.portal;

import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTarget {

    public static final int MAX_NAME_LENGTH = 10;

    public final int dimension;
    public final int x, y, z;
    public final float yaw;

    public String name;
    public DyeColor color = null;

    public PortalTarget(int dimension, int x, int y, int z, float yaw, String name){
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.name = name;
    }

    public PortalTarget(World world, BlockPos pos, float yaw, String name){
        this(world.getDimension().getType().getId(), pos.getX(), pos.getY(), pos.getZ(), yaw, name);
    }

    public PortalTarget(CompoundNBT tag){
        this(tag.getInt("dimension"), tag.getInt("x"), tag.getInt("y"), tag.getInt("z"), tag.getFloat("yaw"), tag.contains("name") ? tag.getString("name") : "Target Destination");
        this.color = tag.contains("color") ? DyeColor.byId(tag.getInt("color")) : null;
    }

    public static PortalTarget read(CompoundNBT tag){
        return new PortalTarget(tag);
    }

    public CompoundNBT write(){
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("dimension", this.dimension);
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        tag.putInt("z", this.z);
        tag.putFloat("yaw", this.yaw);
        tag.putString("name", this.name);
        if(this.color != null)
            tag.putInt("color", this.color.getId());
        return tag;
    }

    public Optional<World> getWorld(MinecraftServer server){
        DimensionType type = DimensionType.getById(this.dimension);
        return type == null ? Optional.empty() : Optional.of(server.func_71218_a(type));
    }

    public BlockPos getPos(){
        return new BlockPos(this.x, this.y, this.z);
    }

}
