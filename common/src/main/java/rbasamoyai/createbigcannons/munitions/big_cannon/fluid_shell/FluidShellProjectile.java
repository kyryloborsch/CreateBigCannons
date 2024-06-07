package rbasamoyai.createbigcannons.munitions.big_cannon.fluid_shell;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import rbasamoyai.createbigcannons.config.CBCConfigs;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCEntityTypes;
import rbasamoyai.createbigcannons.multiloader.IndexPlatform;
import rbasamoyai.createbigcannons.munitions.big_cannon.FuzedBigCannonProjectile;
import rbasamoyai.createbigcannons.munitions.fragment_burst.CBCProjectileBurst;

public class FluidShellProjectile extends FuzedBigCannonProjectile<FluidShellProperties> {

	private EndFluidStack fluidStack;

	public FluidShellProjectile(EntityType<? extends FluidShellProjectile> type, Level level) {
		super(type, level);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.put("Fluid", this.fluidStack.writeTag(new CompoundTag()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.fluidStack = EndFluidStack.readTag(tag.getCompound("Fluid"));
	}

	public void setFluidStack(EndFluidStack fstack) { this.fluidStack = fstack; }

	@Override
	protected void detonate() {
		Vec3 oldDelta = this.getDeltaMovement();
		this.level.explode(null, this.getX(), this.getY(), this.getZ(), 2.0f, CBCConfigs.SERVER.munitions.damageRestriction.get().explosiveInteraction());

		if (!this.fluidStack.isEmpty()) {
			FluidShellProperties properties = this.getProperties();
			int mbPerBlob = properties == null ? 250 : properties.mBPerFluidBlob();
			byte blobSize = (byte)(mbPerBlob / (properties == null ? 50d : (double) properties.mBPerAoeRadius())); // No conversion because ratio would be same
			int convertCount = IndexPlatform.convertFluid(mbPerBlob);
			int count = (int) Math.ceil(this.fluidStack.amount() / (double) convertCount);
			float spread = properties == null ? 1 : properties.fluidBlobSpread();
			FluidBlobBurst burst = CBCProjectileBurst.spawnConeBurst(this.level, CBCEntityTypes.FLUID_BLOB_BURST.get(),
				this.position(), oldDelta, count, spread);
			burst.setFluidStack(this.fluidStack.copy(convertCount));
			burst.setBlobSize(blobSize);
		}
		this.discard();
	}

	@Override
	public BlockState getRenderedBlockState() {
		return CBCBlocks.FLUID_SHELL.getDefaultState().setValue(BlockStateProperties.FACING, Direction.NORTH);
	}

}
