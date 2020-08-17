package com.supermartijn642.wormhole;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Hand;
import net.minecraft.util.concurrent.TickDelayedTask;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;

/**
 * Created 7/21/2020 by SuperMartijn642
 */
public class PortalTile extends PortalGroupTile {

    private static final int TELEPORT_COOLDOWN = 2 * 20; // 2 seconds

    public PortalTile(){
        super(Wormhole.portal_tile);
    }

    public void teleport(Entity entity){
        if(this.group != null && this.group.getTarget() != null){
            PortalTarget target = this.group.getTarget();
            if(!this.world.isRemote)
                this.world.getServer().enqueue(new TickDelayedTask(0, () -> {
                    target.getWorld(this.world.getServer()).filter(world -> world instanceof ServerWorld).map(ServerWorld.class::cast).ifPresent(world -> {
                        if(entity instanceof ServerPlayerEntity){
                            ServerPlayerEntity player = (ServerPlayerEntity)entity;

                            CompoundNBT tag = player.getPersistentData();
                            if(!tag.contains("wormhole:teleported") || player.ticksExisted - tag.getLong("wormhole:teleported") < 0 || player.ticksExisted - tag.getLong("wormhole:teleported") > TELEPORT_COOLDOWN){
                                entity.stopRiding();

                                if(player.isSleeping())
                                    player.func_225652_a_(true, true);

                                if(world == entity.world)
                                    player.connection.setPlayerLocation(target.x + .5, target.y, target.z + .5, target.yaw, 0, Collections.emptySet());
                                else
                                    player.teleport(world, target.x + .5, target.y, target.z + .5, target.yaw, 0);

                                entity.setRotationYawHead(target.yaw);

                                tag.putLong("wormhole:teleported", player.ticksExisted);
                            }
                        }else{
                            if(world == entity.world){
                                entity.setLocationAndAngles(target.x + .5, target.y, target.z + .5, target.yaw, 0);
                                entity.setRotationYawHead(target.yaw);
                            }else{
                                entity.detach();

                                Entity newEntity = entity.getType().create(world);
                                if(newEntity == null)
                                    return;

                                newEntity.copyDataFromOld(entity);
                                newEntity.setLocationAndAngles(target.x + .5, target.y, target.z + .5, target.yaw, 0);
                                newEntity.setRotationYawHead(target.yaw);
                                world.func_217460_e(newEntity);

                                entity.remove();
                            }
                        }

                        if(!(entity instanceof LivingEntity) || !((LivingEntity)entity).isElytraFlying()){
                            entity.setMotion(Vec3d.ZERO);
                            entity.onGround = true;
                        }
                    });
                }));
        }
    }

    public boolean activate(PlayerEntity player, Hand hand){
        if(player.getHeldItem(hand).getItem() instanceof DyeItem){
            DyeColor color = ((DyeItem)player.getHeldItem(hand).getItem()).getDyeColor();
            if(this.group != null && this.group.getTarget() != null){
                for(BlockPos pos : this.group.shape.area){
                    BlockState state = this.world.getBlockState(pos);
                    if(state.getBlock() == Wormhole.portal && state.get(PortalBlock.COLOR_PROPERTY) != color)
                        this.world.setBlockState(pos, state.with(PortalBlock.COLOR_PROPERTY, color));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public void onBreak(){
        if(this.group != null)
            this.group.removeTarget();
    }
}
