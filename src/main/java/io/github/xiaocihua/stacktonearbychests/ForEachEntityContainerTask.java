package io.github.xiaocihua.stacktonearbychests;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static io.github.xiaocihua.stacktonearbychests.MathUtil.getBox;

public class ForEachEntityContainerTask extends ForEachContainerTask {

    private final ClientPlayerInteractionManager interactionManager;
    private final float squaredReachDistance;
    private final Entity cameraEntity;

    private final Iterator<Entity> entities;

    public ForEachEntityContainerTask(MinecraftClient client,
                                      ClientPlayerEntity player,
                                      Consumer<ScreenHandler> action,
                                      Entity cameraEntity,
                                      World world,
                                      ClientPlayerInteractionManager interactionManager,
                                      Collection<String> filter
                                      ) {
        super(client, player, action);
        this.interactionManager = interactionManager;
        float reachDistance = interactionManager.getReachDistance();
        this.squaredReachDistance = MathHelper.square(reachDistance);
        this.cameraEntity = cameraEntity;

        Predicate<Entity> entityPredicate = EntityPredicates.VALID_INVENTORIES
                .or(entity -> entity instanceof RideableInventory)
                .and(entity -> filter.contains(Registries.ENTITY_TYPE.getId(entity.getType()).toString()))
                .and(entity -> !(entity instanceof AbstractDonkeyEntity donkey) || donkey.hasChest());

        entities = world.getOtherEntities(cameraEntity, getBox(cameraEntity.getCameraPosVec(0), reachDistance), entityPredicate)
                .iterator();
    }

    @Override
    public void start() {
        if (!entities.hasNext()) {
            super.stop();
            return;
        }
        client.options.sneakKey.setPressed(true);
        EndWorldTickExecutor.execute(super::start);
    }

    @Override
    protected void stop() {
        client.options.sneakKey.setPressed(false);
        EndWorldTickExecutor.execute(super::stop);
    }

    @Override
    protected boolean findAndOpenNextContainer() {
        while (entities.hasNext()) {
            Entity entity = entities.next();

            if (entity.squaredDistanceTo(cameraEntity.getCameraPosVec(0)) > squaredReachDistance) {
                continue;
            }

            interactionManager.interactEntity(player, entity, Hand.MAIN_HAND);

            return true;
        }

        return false;
    }
}
