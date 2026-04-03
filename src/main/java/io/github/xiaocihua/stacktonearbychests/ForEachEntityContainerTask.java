package io.github.xiaocihua.stacktonearbychests;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.xiaocihua.stacktonearbychests.MathUtil.getBox;
import static io.github.xiaocihua.stacktonearbychests.MathUtil.getClosestPoint;

public class ForEachEntityContainerTask extends ForEachContainerTask {

    private final MultiPlayerGameMode interactionManager;
    private final Entity cameraEntity;

    private final Iterator<Entity> entities;

    public ForEachEntityContainerTask(Minecraft client,
                                      LocalPlayer player,
                                      Consumer<AbstractContainerMenu> action,
                                      Entity cameraEntity,
                                      Level world,
                                      MultiPlayerGameMode interactionManager,
                                      Collection<String> filter
                                      ) {
        super(client, player, action);
        this.interactionManager = interactionManager;
        this.cameraEntity = cameraEntity;

        Predicate<Entity> entityPredicate = EntitySelector.CONTAINER_ENTITY_SELECTOR
                .or(entity -> entity instanceof HasCustomInventoryScreen)
                .and(entity -> filter.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()))
                .and(entity -> !(entity instanceof AbstractChestedHorse donkey) || donkey.hasChest());

        entities = world.getEntities(cameraEntity, getBox(cameraEntity.getEyePosition(), player.entityInteractionRange()), entityPredicate)
                .iterator();
    }

    @Override
    public void start() {
        if (!entities.hasNext()) {
            super.stop();
            return;
        }
        client.options.keyShift.setDown(true);
        EndWorldTickExecutor.execute(super::start);
    }

    @Override
    protected void stop() {
        client.options.keyShift.setDown(false);
        EndWorldTickExecutor.execute(super::stop);
    }

    @Override
    protected boolean findAndOpenNextContainer() {
        while (entities.hasNext()) {
            Entity entity = entities.next();

            ItemStack mainHandItem = player.getMainHandItem();
            if (entity instanceof Leashable && mainHandItem.getItem() == Items.LEAD){
                continue;
            }

            if (!player.isWithinEntityInteractionRange(entity, 0.0)) {
                continue;
            }

            var entityHit = new EntityHitResult(entity, getClosestPoint(entity, cameraEntity.getEyePosition()));
            interactionManager.interact(player, entity, entityHit, InteractionHand.MAIN_HAND);

            return true;
        }

        return false;
    }
}
