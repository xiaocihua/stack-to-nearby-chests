package io.github.xiaocihua.stacktonearbychests;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.animal.equine.AbstractChestedHorse;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;

import static io.github.xiaocihua.stacktonearbychests.MathUtil.getBox;

public class ForEachEntityContainerTask extends ForEachContainerTask {

    private final MultiPlayerGameMode interactionManager;
    private final double squaredReachDistance;
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
        double reachDistance = player.entityInteractionRange();
        this.squaredReachDistance = Mth.square(reachDistance);
        this.cameraEntity = cameraEntity;

        Predicate<Entity> entityPredicate = EntitySelector.CONTAINER_ENTITY_SELECTOR
                .or(entity -> entity instanceof HasCustomInventoryScreen)
                .and(entity -> filter.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString()))
                .and(entity -> !(entity instanceof AbstractChestedHorse donkey) || donkey.hasChest());

        entities = world.getEntities(cameraEntity, getBox(cameraEntity.getEyePosition(0), reachDistance), entityPredicate)
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

            if (entity.distanceToSqr(cameraEntity.getEyePosition(0)) > squaredReachDistance) {
                continue;
            }

            interactionManager.interact(player, entity, InteractionHand.MAIN_HAND);

            return true;
        }

        return false;
    }
}
