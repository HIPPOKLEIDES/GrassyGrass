package com.ardacraft.ardagrass;

import me.pepperbell.continuity.client.util.SpriteCalculator;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class ArdaGrassBakedModel extends ForwardingBakedModel {

    public ArdaGrassBakedModel(BakedModel baseModel) {
        this.wrapped = baseModel;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
        AtomicInteger counter = new AtomicInteger();
        context.pushTransform(quad -> {
            if (quad.nominalFace().getAxis() != Direction.Axis.Y && counter.get() <= 9) {
                Direction face = quad.nominalFace();

                if (canFullyConnect(blockView, state, pos, face)) {
                    spriteBake(quad, state, randomSupplier);
                }
            }
            counter.addAndGet(1);
            return true;
        });
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
        context.popTransform();
    }

    private static boolean canFullyConnect(BlockRenderView world, BlockState self, BlockPos selfPos, Direction direction) {
        return canConnect(world, self, selfPos, selfPos.offset(direction).down());
    }

    private static boolean canConnect(BlockRenderView world, BlockState self, BlockPos selfPos, BlockPos adjacentPos) {
        var adjacent = world.getBlockState(adjacentPos);
        var upPos = adjacentPos.up();
        var up = world.getBlockState(upPos);

        return canConnect(self, adjacent) && (up.isAir() || !up.isSideSolidFullSquare(world, upPos, Direction.DOWN));
    }

    private static boolean canConnect(BlockState self, BlockState adjacent) {
        return self == adjacent;
    }

    private static boolean spriteBake(MutableQuadView quad, BlockState state, Supplier<net.minecraft.util.math.random.Random> randomSupplier) {
        var sprite = SpriteCalculator.calculateSprite(state, Direction.UP, randomSupplier);
        if (sprite != null)
            quad.spriteBake(0, sprite, MutableQuadView.BAKE_LOCK_UV);
        return sprite != null;
    }
}
